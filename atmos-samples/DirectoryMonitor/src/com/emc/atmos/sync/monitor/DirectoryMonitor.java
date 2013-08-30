// Copyright (c) 2012, EMC Corporation.
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//     + Redistributions of source code must retain the above copyright notice,
//       this list of conditions and the following disclaimer.
//     + Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     + The name of EMC Corporation may not be used to endorse or promote
//       products derived from this software without specific prior written
//       permission.
//
//      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//      "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
//      TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//      PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
//      BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
//      CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
//      SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
//      INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//      CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//      ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
//      POSSIBILITY OF SUCH DAMAGE.
package com.emc.atmos.sync.monitor;

import com.emc.atmos.sync.AtmosSync2;
import com.emc.atmos.sync.plugins.AtmosDestination;
import com.emc.atmos.sync.plugins.FilesystemSource;
import org.apache.log4j.Logger;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DirectoryMonitor {
    private static final Logger log = Logger.getLogger( DirectoryMonitor.class );

    private int interval = 2000; // 2 seconds
    private DirectoryMonitorBean monitorBean;

    private boolean monitoring = false;
    private AtmosSync2 sync;
    private DirectoryHash hash;
    private SyncWorker worker;
    private List<ActionListener> listeners = new ArrayList<ActionListener>();

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

    public synchronized void addActionListener( ActionListener listener ) {
        listeners.add( listener );
    }

    public synchronized void removeActionListener( ActionListener listener ) {
        listeners.remove( listener );
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
                        try {
                            fireEvent( SyncEvent.Command.START_SYNC, null );
                            sync.run();
                            fireEvent( SyncEvent.Command.SYNC_COMPLETE, null );
                        } catch ( Exception e ) {
                            fireEvent( SyncEvent.Command.ERROR, e );
                            log.error( "Error in sync run", e );
                        }
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

    private synchronized void fireEvent( SyncEvent.Command command, Exception e ) {
        SyncEvent event = new SyncEvent( this, command, e );
        for ( ActionListener listener : listeners ) {
            listener.actionPerformed( event );
        }
    }
}
