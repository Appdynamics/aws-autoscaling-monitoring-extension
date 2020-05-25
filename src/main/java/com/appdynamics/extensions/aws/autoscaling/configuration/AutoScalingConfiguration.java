package com.appdynamics.extensions.aws.autoscaling.configuration;

import com.appdynamics.extensions.aws.config.Configuration;

import java.util.List;

public class AutoScalingConfiguration extends Configuration {

    private List<String> autoScalingGroupNames;

    public List<String> getAutoScalingGroupNames() {
        return autoScalingGroupNames;
    }

    public void setAutoScalingGroupNames(List<String> autoScalingGroupNames) {
        this.autoScalingGroupNames = autoScalingGroupNames;
    }
}
