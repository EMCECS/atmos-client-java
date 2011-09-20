package com.emc.atmos.cleanup;

import java.util.Set;

public class SimpleTaskNode extends TaskNode {
	public String message;
	
	public SimpleTaskNode( String message, Set<TaskNode> parents ) {
		super( parents );
		this.message = message;
	}
	
	public SimpleTaskNode( String message ) {
		this( message, null );
	}

	@Override
	public TaskResult execute() throws Exception {
		System.out.println( Thread.currentThread() + ": " + message );
		
		return new TaskResult(true);
	}

	@Override
	public String toString() {
		return "SimpleTaskNode: " + message;
	}
	
	

}
