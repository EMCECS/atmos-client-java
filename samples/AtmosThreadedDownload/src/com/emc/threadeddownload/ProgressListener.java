package com.emc.threadeddownload;

public interface ProgressListener {
	void complete(DownloadBlock block);
	void error(Exception ex);
}
