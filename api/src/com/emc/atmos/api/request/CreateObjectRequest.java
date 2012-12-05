package com.emc.atmos.api.request;

import com.emc.atmos.api.ChecksumAlgorithm;
import com.emc.atmos.api.RestUtil;

import java.util.List;
import java.util.Map;

public class CreateObjectRequest extends PutObjectRequest<CreateObjectRequest> {
    private ChecksumAlgorithm serverGeneratedChecksumAlgorithm;

    @Override
    public String getServiceRelativePath() {
        if ( identifier == null ) return "objects";
        else return identifier.getRelativeResourcePath();
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public Map<String, List<Object>> generateHeaders() {
        Map<String, List<Object>> headers = super.generateHeaders();

        if ( serverGeneratedChecksumAlgorithm != null )
            RestUtil.addValue( headers, RestUtil.XHEADER_GENERATE_CHECKSUM, serverGeneratedChecksumAlgorithm );

        return headers;
    }

    @Override
    protected CreateObjectRequest me() {
        return this;
    }

    public CreateObjectRequest serverGeneratedChecksumAlgorithm( ChecksumAlgorithm serverGeneratedChecksumAlgorithm ) {
        setServerGeneratedChecksumAlgorithm( serverGeneratedChecksumAlgorithm );
        return this;
    }

    public ChecksumAlgorithm getServerGeneratedChecksumAlgorithm() {
        return serverGeneratedChecksumAlgorithm;
    }

    public void setServerGeneratedChecksumAlgorithm( ChecksumAlgorithm serverGeneratedChecksumAlgorithm ) {
        this.serverGeneratedChecksumAlgorithm = serverGeneratedChecksumAlgorithm;
    }
}
