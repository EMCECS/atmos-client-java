package com.emc.atmos.sync;

public class TaskResult {
	public static TaskResult SUCCESS = new TaskResult(true);
	public static TaskResult FAILURE = new TaskResult(false);
	
	private boolean successful;
	
	public TaskResult( boolean successful ) {
		this.setSuccessful(successful);
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public boolean isSuccessful() {
		return successful;
	}
}
