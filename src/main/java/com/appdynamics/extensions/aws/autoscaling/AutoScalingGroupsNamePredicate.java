package com.appdynamics.extensions.aws.autoscaling;

import com.amazonaws.services.cloudwatch.model.Metric;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;

import java.util.List;

public class AutoScalingGroupsNamePredicate implements Predicate<Metric> {

    private List<String> autoScalingGroupNamesList;
    private Predicate<CharSequence> patternPredicate;

    public AutoScalingGroupsNamePredicate(List<String> autoScalingGroupNamesList){
        this.autoScalingGroupNamesList = autoScalingGroupNamesList;
        buildPattern();
    }

    private void buildPattern(){
        if (autoScalingGroupNamesList != null && !autoScalingGroupNamesList.isEmpty()) {
            for(String autoScalingGroupName : autoScalingGroupNamesList) {
                if(!Strings.isNullOrEmpty(autoScalingGroupName)) {
                    Predicate<CharSequence> autoScalingGroupNamePatternPredicate = Predicates.containsPattern(autoScalingGroupName);
                    if (patternPredicate == null) {
                        patternPredicate = autoScalingGroupNamePatternPredicate;
                    } else {
                        patternPredicate = Predicates.or(patternPredicate, autoScalingGroupNamePatternPredicate);
                    }
                }
            }
        }
    }

    @Override
    public boolean apply(Metric metric) {
        if(patternPredicate == null){
            return true;
        }
        else{
            String autoScalingGroupName = metric.getDimensions().get(0).getValue();
            return patternPredicate.apply(autoScalingGroupName);
        }
    }
}
