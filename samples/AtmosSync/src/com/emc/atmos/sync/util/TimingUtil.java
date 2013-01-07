package com.emc.atmos.sync.util;

import com.emc.atmos.sync.AtmosSync2;
import com.emc.atmos.sync.plugins.SourcePlugin;
import com.emc.atmos.sync.plugins.SyncPlugin;
import org.apache.log4j.Logger;

import java.util.*;

public final class TimingUtil {
    private static final Logger log = Logger.getLogger( TimingUtil.class );

    private static Map<SyncPlugin, Timings> registry = new Hashtable<SyncPlugin, Timings>();

    /**
     * registers all plug-ins of the given sync instance so that they are all associated with the same timing group.
     */
    public static synchronized void register( AtmosSync2 sync, int timingWindow ) {
        Timings timings = new WindowedTimings( timingWindow );
        registry.put( sync.getSource(), timings );
        for ( SyncPlugin plugin : sync.getPluginChain() ) {
            registry.put( plugin, timings );
        }
        registry.put( sync.getDestination(), timings );
    }

    public static void startOperation( SyncPlugin plugin, String name ) {
        getTimings( plugin ).startOperation( plugin.getName() + "::" + name );
    }

    public static void completeOperation( SyncPlugin plugin, String name ) {
        getTimings( plugin ).completeOperation( plugin.getName() + "::" + name );
    }

    public static void failOperation( SyncPlugin plugin, String name ) {
        getTimings( plugin ).failOperation( plugin.getName() + "::" + name );
    }

    public static void logTimings( SourcePlugin source ) {
        getTimings( source ).dump();
    }

    private static Timings getTimings( SyncPlugin plugin ) {
        Timings timings = registry.get( plugin );
        if ( timings == null ) timings = NULL_TIMINGS;
        return timings;
    }

    private TimingUtil() {
    }

    private static interface Timings {
        public void startOperation( String name );

        public void completeOperation( String name );

        public void failOperation( String name );

        public void dump();
    }

    private static class WindowedTimings implements Timings {
        private ThreadLocal<Map<String, Long>> operationStartTimes = new ThreadLocal<Map<String, Long>>();
        private final Map<String, Long> operationGrossTimes = new Hashtable<String, Long>();
        private final Map<String, Long> operationCompleteCounts = new Hashtable<String, Long>();
        private final Map<String, Long> operationFailedCounts = new Hashtable<String, Long>();

        private int statsWindow;
        private boolean dumpPending;

        public WindowedTimings( int statsWindow ) {
            this.statsWindow = statsWindow;
        }

        public void startOperation( String name ) {
            getOperationStartTimes().put( name, System.currentTimeMillis() );
        }

        public void completeOperation( String name ) {
            long time = endAndTimeOperation( name ), complete, count;
            boolean dump = false;
            synchronized ( this ) {
                complete = getOperationCompleteCount( name );
                count = complete + getOperationFailedCount( name );
                operationGrossTimes.put( name, getOperationGrossTime( name ) + time );
                operationCompleteCounts.put( name, complete + 1 );
                if ( count >= statsWindow && !dumpPending ) {
                    dumpPending = true;
                    dump = true;
                }
            }
            if ( dump ) dump();
        }

        public void failOperation( String name ) {
            long time = endAndTimeOperation( name ), failed, count;
            boolean dump = false;
            synchronized ( this ) {
                failed = getOperationFailedCount( name );
                count = failed + getOperationCompleteCount( name );
                operationGrossTimes.put( name, getOperationGrossTime( name ) + time );
                operationFailedCounts.put( name, failed + 1 );
                if ( count >= statsWindow && !dumpPending ) {
                    dumpPending = true;
                    dump = true;
                }
            }
            if ( dump ) dump();
        }

        public void dump() {
            List<TimingStats> stats = new ArrayList<TimingStats>();
            long totalMillis = 0, grossTime;
            synchronized ( this ) {
                for ( String name : operationGrossTimes.keySet() ) {
                    grossTime = getOperationGrossTime( name );
                    stats.add( new TimingStats( name,
                                                getOperationCompleteCount( name ),
                                                getOperationFailedCount( name ),
                                                grossTime ) );
                    totalMillis += grossTime;
                }
                operationGrossTimes.clear();
                operationCompleteCounts.clear();
                operationFailedCounts.clear();
                dumpPending = false;
            }
            log.info( "Start timings dump\n######################################################################" );
            Collections.sort( stats );
            for ( TimingStats stat : stats ) {
                log.info( stat );
            }
            long ms = totalMillis % 1000;
            long seconds = (totalMillis / 1000) % 60;
            long minutes = (totalMillis / (1000 * 60)) % 60;
            long hours = (totalMillis / (1000 * 60 * 60));
            log.info( String.format( "Total time collected: %dms  (%d:%02d:%02d.%03d)",
                                     totalMillis, hours, minutes, seconds, ms ) );
        }

        private Map<String, Long> getOperationStartTimes() {
            Map<String, Long> map = operationStartTimes.get();
            if ( map == null ) {
                map = new HashMap<String, Long>();
                operationStartTimes.set( map );
            }
            return map;
        }

        private long getOperationGrossTime( String name ) {
            Long time = operationGrossTimes.get( name );
            if ( time == null ) time = 0L;
            return time;
        }

        private long getOperationCompleteCount( String name ) {
            Long count = operationCompleteCounts.get( name );
            if ( count == null ) count = 0L;
            return count;
        }

        private long getOperationFailedCount( String name ) {
            Long count = operationFailedCounts.get( name );
            if ( count == null ) count = 0L;
            return count;
        }

        private long endAndTimeOperation( String name ) {
            Long startTime = getOperationStartTimes().get( name );
            if ( startTime == null )
                throw new IllegalStateException( "no start time exists for operation " + name );
            return System.currentTimeMillis() - startTime;
        }
    }

    private static class TimingStats implements Comparable<TimingStats> {
        private String name;
        private long completeCount;
        private long failedCount;
        private long grossTime;

        public TimingStats( String name, long completeCount, long failedCount, long grossTime ) {
            this.name = name;
            this.completeCount = completeCount;
            this.failedCount = failedCount;
            this.grossTime = grossTime;
        }

        public String getName() {
            return name;
        }

        public long getCompleteCount() {
            return completeCount;
        }

        public long getFailedCount() {
            return failedCount;
        }

        public long getGrossTime() {
            return grossTime;
        }

        @Override
        public int compareTo( TimingStats o ) {
            return name.compareTo( o.getName() );
        }

        @Override
        public String toString() {
            long totalCount = completeCount + failedCount;
            return name + '\n'
                   + "    Completed:" + rAlign( Long.toString( completeCount ), 6 )
                   + "    Failed:" + rAlign( Long.toString( failedCount ), 6 )
                   + "    Avg. Time:" + rAlign( Long.toString( grossTime / (totalCount == 0 ? 1 : totalCount) ), 10 )
                   + "ms";
        }

        private String rAlign( String string, int length ) {
            return String.format( "%1$" + length + "s", string );
        }
    }

    /**
     * Used when timings are disabled
     */
    private static Timings NULL_TIMINGS = new Timings() {
        @Override
        public void startOperation( String name ) {
        }

        @Override
        public void completeOperation( String name ) {
        }

        @Override
        public void failOperation( String name ) {
        }

        @Override
        public void dump() {
        }
    };
}
