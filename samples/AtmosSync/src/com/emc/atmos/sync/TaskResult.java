package com.emc.atmos.sync;

public class TaskResult {
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
