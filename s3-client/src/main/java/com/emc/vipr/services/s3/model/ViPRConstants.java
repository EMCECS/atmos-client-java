package com.emc.vipr.services.s3.model;

public interface ViPRConstants {

    // Header names
    static final String EMC_PREFIX = "x-emc-";

    static final String NAMESPACE_HEADER = "x-emc-namespace";
    static final String APPEND_OFFSET_HEADER = "x-emc-append-offset";
    static final String FILE_ACCESS_MODE_HEADER = "x-emc-file-access-mode";
    static final String FILE_ACCESS_DURATION_HEADER = "x-emc-file-access-duration";
    static final String FILE_ACCESS_HOST_LIST_HEADER = "x-emc-file-access-host-list";
    static final String FILE_ACCESS_UID_HEADER = "x-emc-file-access-uid";
    static final String FILE_ACCESS_TOKEN_HEADER = "x-emc-file-access-token";

    // Parameter names
    static final String ACCESS_MODE_PARAMETER = "accessmode";
    static final String FILE_ACCESS_PARAMETER = "fileaccess";
    static final String MARKER_PARAMETER = "marker";
    static final String MAX_KEYS_PARAMETER = "max-keys";

    enum FileAccessMode {
        disabled(false, null),
        readOnly(false, null),
        readWrite(false, null),
        switchingToDisabled(true, disabled),
        switchingToReadOnly(true, readOnly),
        switchingToReadWrite(true, readWrite);

        private boolean transitionState;
        private FileAccessMode targetState;

        private FileAccessMode(boolean transitionState, FileAccessMode targetState) {
            this.transitionState = transitionState;
            this.targetState = targetState;
        }

        public boolean isTransitionState() {
            return transitionState;
        }

        public boolean transitionsToTarget(FileAccessMode targetState) {
            return targetState == this.targetState;
        }
    }

    enum FileAccessProtocol {NFS, CIFS}
}
