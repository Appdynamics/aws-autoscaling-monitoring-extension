package com.appdynamics.extensions.aws.autoscaling.processors;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.DimensionFilter;
import com.appdynamics.extensions.aws.autoscaling.AutoScalingGroupsNamePredicate;
import com.appdynamics.extensions.aws.autoscaling.configuration.AutoScalingConfiguration;
import com.appdynamics.extensions.aws.config.IncludeMetric;
import com.appdynamics.extensions.aws.dto.AWSMetric;
import com.appdynamics.extensions.aws.metric.*;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessorHelper;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

public class AutoScalingMetricsProcessor implements MetricsProcessor {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(AutoScalingMetricsProcessor.class);
    private static final String NAMESPACE = "AWS/AutoScaling";
    private static final String AUTO_SCALING_GROUP_NAME = "AutoScalingGroupName";
    private AutoScalingConfiguration autoScalingConfiguration;
    private List<IncludeMetric> includeMetrics;
    private List<String> autoScalingGroupNamesList;

    public AutoScalingMetricsProcessor (AutoScalingConfiguration autoScalingConfiguration){
        this.autoScalingConfiguration = autoScalingConfiguration;
        this.includeMetrics = autoScalingConfiguration.getMetricsConfig().getIncludeMetrics();
        this.autoScalingGroupNamesList = autoScalingConfiguration.getAutoScalingGroupNames();
    }

    public List<AWSMetric> getMetrics(AmazonCloudWatch awsCloudWatch, String accountName, LongAdder awsRequestCounter) {
        List<DimensionFilter> dimensionFilters = getDimensionFilters();
        AutoScalingGroupsNamePredicate autoScalingGroupsNamePredicate = new AutoScalingGroupsNamePredicate(autoScalingGroupNamesList);
        return MetricsProcessorHelper.getFilteredMetrics(awsCloudWatch, awsRequestCounter, NAMESPACE, includeMetrics, dimensionFilters, autoScalingGroupsNamePredicate);
    }

    private List<DimensionFilter> getDimensionFilters() {
        List<DimensionFilter> dimensionFilters = Lists.newArrayList();

        DimensionFilter serviceNameDimensionFilter = new DimensionFilter();
        serviceNameDimensionFilter.withName(AUTO_SCALING_GROUP_NAME);
        dimensionFilters.add(serviceNameDimensionFilter);

        return dimensionFilters;
    }

    @Override
    public StatisticType getStatisticType(AWSMetric metric) {
        return MetricsProcessorHelper.getStatisticType(metric.getIncludeMetric(), includeMetrics);
    }

    public List<Metric> createMetricStatsMapForUpload(NamespaceMetricStatistics namespaceMetricStats) {
        List<Metric> stats = Lists.newArrayList();
        if(namespaceMetricStats != null) {
            for (AccountMetricStatistics accountMetricStatistics : namespaceMetricStats.getAccountMetricStatisticsList()) {
                for (RegionMetricStatistics regionMetricStatistics : accountMetricStatistics.getRegionMetricStatisticsList()) {
                    for (MetricStatistic metricStatistic : regionMetricStatistics.getMetricStatisticsList()) {
                        String metricPath = createMetricPath(accountMetricStatistics.getAccountName(), regionMetricStatistics.getRegion(), metricStatistic);
                        if (metricStatistic.getValue() != null) {
                            Map<String, Object> metricProperties = Maps.newHashMap();
                            AWSMetric awsMetric = metricStatistic.getMetric();
                            IncludeMetric includeMetric = awsMetric.getIncludeMetric();
                            metricProperties.put("alias", includeMetric.getAlias());
                            metricProperties.put("multiplier", includeMetric.getMultiplier());
                            metricProperties.put("aggregationType", includeMetric.getAggregationType());
                            metricProperties.put("timeRollUpType", includeMetric.getTimeRollUpType());
                            metricProperties.put("clusterRollUpType ", includeMetric.getClusterRollUpType());
                            metricProperties.put("delta", includeMetric.isDelta());
                            Metric metric = new Metric(includeMetric.getName(), Double.toString(metricStatistic.getValue()), metricStatistic.getMetricPrefix() + metricPath, metricProperties);
                            stats.add(metric);
                        } else {
                            logger.debug(String.format("Ignoring metric [ %s ] which has null value", metricPath));
                        }
                    }
                }
            }
        }
        return stats;
    }

    private String createMetricPath(String accountName, String region, MetricStatistic metricStatistic){
        AWSMetric awsMetric = metricStatistic.getMetric();
        IncludeMetric includeMetric = awsMetric.getIncludeMetric();
        com.amazonaws.services.cloudwatch.model.Metric metric = awsMetric.getMetric();
        String autoScalingGroupName = null;

        for(Dimension dimension : metric.getDimensions()) {
            if(dimension.getName().equalsIgnoreCase(AUTO_SCALING_GROUP_NAME)) {
                autoScalingGroupName = dimension.getValue();
            }
        }

        StringBuilder stringBuilder = new StringBuilder(accountName)
                .append("|")
                .append(region)
                .append("|");
        if(autoScalingGroupName != null) {
            stringBuilder.append(autoScalingGroupName)
                    .append("|");
        }

        stringBuilder.append(includeMetric.getName());
        return stringBuilder.toString();
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }
}
