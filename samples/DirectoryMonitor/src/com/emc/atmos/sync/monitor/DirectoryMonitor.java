package com.emc.atmos.sync.monitor;

import com.emc.atmos.sync.AtmosSync2;
import com.emc.atmos.sync.plugins.AtmosDestination;
import com.emc.atmos.sync.plugins.FilesystemSource;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;

public class DirectoryMonitor {
    private static final Logger log = Logger.getLogger( DirectoryMonitor.class );

    private int interval = 2000; // 2 seconds
    private DirectoryMonitorBean monitorBean;

    private boolean monitoring = false;
    private AtmosSync2 sync;
    private DirectoryHash hash;
    private SyncWorker worker;

    public int getInterval() {
        return interval;
    }

    public void setInterval( int interval ) {
        this.interval = interval;
    }

    public DirectoryMonitorBean getMonitorBean() {
        return monitorBean;
    }

    public void setMonitorBean( DirectoryMonitorBean monitorBean ) {
        this.monitorBean = monitorBean;
    }

    public synchronized void startMonitor() throws InterruptedException {
        if ( monitoring ) return;

        // make sure we don't spawn multiple threads
        if ( worker != null && worker.isAlive() ) worker.join();

        // sanity check
        if ( monitorBean.getLocalDirectory() == null )
            throw new UnsupportedOperationException( "No localDirectory to monitor" );
        File localDirectory = new File( monitorBean.getLocalDirectory() );
        if ( !localDirectory.exists() || !localDirectory.isDirectory() || !localDirectory.canRead() )
            throw new UnsupportedOperationException( localDirectory.getPath() + " is not a valid directory (exists: "
                    + localDirectory.exists() + ", isDirectory: " + localDirectory.isDirectory() + ", readable: " + localDirectory.canRead() );
        if ( monitorBean.getAtmosHost() == null || monitorBean.getAtmosUid() == null
                || monitorBean.getAtmosSecret() == null || monitorBean.getAtmosDirectory() == null )
            throw new UnsupportedOperationException( "Missing Atmos information" );

        monitoring = true;
        sync = createSync();
        hash = new DirectoryHash();
        worker = new SyncWorker();
        worker.start();
    }

    public synchronized void stopMonitor() {
        monitoring = false;
    }

    protected AtmosSync2 createSync() {
        FilesystemSource source = new FilesystemSource();
        source.setSource( new File( monitorBean.getLocalDirectory() ) );
        source.setRecursive( monitorBean.isRecursive() );

        AtmosDestination destination = new AtmosDestination();
        destination.setHosts( Arrays.asList( monitorBean.getAtmosHost() ) );
        destination.setPort( monitorBean.getAtmosPort() );
        destination.setUid( monitorBean.getAtmosUid() );
        destination.setSecret( monitorBean.getAtmosSecret() );
        destination.setDestNamespace( monitorBean.getAtmosDirectory() );
        destination.afterPropertiesSet();

        AtmosSync2 sync = new AtmosSync2();
        sync.setSource( source );
        sync.setDestination( destination );
        sync.afterPropertiesSet();
        return sync;
    }

    public class SyncWorker extends Thread {
        @Override
        public void run() {
            try {
                while ( monitoring ) {
                    if ( hash.update( new File( monitorBean.getLocalDirectory() ) ) ) {
                        log.info( "Hash changed for directory" );
                        sync.run();
                    } else {
                        log.info( "No changes in directory" );
                    }
                    Thread.sleep( interval );
                }
            } catch ( InterruptedException e ) {
                log.error( "interrupted", e );
            }
        }
    }
}
