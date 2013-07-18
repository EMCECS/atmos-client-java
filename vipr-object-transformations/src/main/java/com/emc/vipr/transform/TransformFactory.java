package com.emc.vipr.transform;

import java.util.Map;

public abstract class TransformFactory {
    private Integer priority;

    public abstract TransformConfig getTransformConfig();

    public abstract void getTransformConfig(Map<String, String> metadata);

    public abstract String getTransformType();

    public Integer getPriority() {
        return priority;
    }

}