package com.emc.atmos.sync.monitor;

import java.awt.event.ActionEvent;

public class SyncEvent extends ActionEvent {
    public static final int EVENT_ID = 0;

    private Command command;
    private Exception exception;

    public SyncEvent( Object source, Command command ) {
        this( source, command, null );
    }

    public SyncEvent( Object source, Command command, Exception exception ) {
        super( source, EVENT_ID, command.toString() );
        this.command = command;
        this.exception = exception;
    }

    public Command getCommand() {
        return this.command;
    }

    public Exception getException() {
        return this.exception;
    }

    public enum Command {
        START_SYNC, SYNC_COMPLETE, ERROR
    }
}
