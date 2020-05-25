/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.autoscaling;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.Metric;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
public class AutoScalingGroupNamesPredicateTest {

	@Mock
	private Metric metric;

	@Mock
	private Dimension dimension;

	@Test
	public void matchedApiNameMetricShouldReturnTrue(){
		List<String> autoScalingGroupNamesList = Lists.newArrayList("sampleName");
		AutoScalingGroupsNamePredicate serviceNamesPredicate = new AutoScalingGroupsNamePredicate(autoScalingGroupNamesList);
		when(metric.getDimensions()).thenReturn(Lists.newArrayList(dimension));
		when(dimension.getValue()).thenReturn("sampleName");
		Assert.assertTrue(serviceNamesPredicate.apply(metric));

	}

	@Test
	public void unMatchedApiNameMetricShouldReturnFalse(){
		List<String> autoScalingGroupNamesList = Lists.newArrayList("sampleName1", "sampleName2");
		AutoScalingGroupsNamePredicate serviceNamesPredicate = new AutoScalingGroupsNamePredicate(autoScalingGroupNamesList);
		when(metric.getDimensions()).thenReturn(Lists.newArrayList(dimension));
		when(dimension.getValue()).thenReturn("sampleName");
		Assert.assertFalse(serviceNamesPredicate.apply(metric));

	}

	@Test
	public void emptyPredicateShouldReturnTrue(){
		List<String> autoScalingGroupNamesList = Lists.newArrayList();
		AutoScalingGroupsNamePredicate serviceNamesPredicate = new AutoScalingGroupsNamePredicate(autoScalingGroupNamesList);
		when(metric.getDimensions()).thenReturn(Lists.newArrayList(dimension));
		when(dimension.getValue()).thenReturn("sampleName");
		Assert.assertTrue(serviceNamesPredicate.apply(metric));

	}

	@Test
	public void nullPredicateShouldReturnTrue(){
		List<String> autoScalingGroupNamesList = null;
		AutoScalingGroupsNamePredicate serviceNamesPredicate = new AutoScalingGroupsNamePredicate(autoScalingGroupNamesList);
		when(metric.getDimensions()).thenReturn(Lists.newArrayList(dimension));
		when(dimension.getValue()).thenReturn("sampleName");
		Assert.assertTrue(serviceNamesPredicate.apply(metric));

	}

	@Test
	public void emptyApiNamesInListShouldReturnTrue(){
		List<String> autoScalingGroupNamesList = Lists.newArrayList("", "");
		AutoScalingGroupsNamePredicate serviceNamesPredicate = new AutoScalingGroupsNamePredicate(autoScalingGroupNamesList);
		when(metric.getDimensions()).thenReturn(Lists.newArrayList(dimension));
		when(dimension.getValue()).thenReturn("sampleName");
		Assert.assertTrue(serviceNamesPredicate.apply(metric));

	}

	@Test
	public void emptyApiNamesAndNonEmtyApiNamesInListShouldReturnTrueIfMatched(){
		List<String> autoScalingGroupNamesList = Lists.newArrayList("sampleName", "");
		AutoScalingGroupsNamePredicate serviceNamesPredicate = new AutoScalingGroupsNamePredicate(autoScalingGroupNamesList);
		when(metric.getDimensions()).thenReturn(Lists.newArrayList(dimension));
		when(dimension.getValue()).thenReturn("sampleName");
		Assert.assertTrue(serviceNamesPredicate.apply(metric));

	}

	@Test
	public void emptyApiNamesAndNonEmtyApiNamesInListShouldReturnFalseIfNotMatched(){
		List<String> autoScalingGroupNamesList = Lists.newArrayList("sampleName$", "");
		AutoScalingGroupsNamePredicate serviceNamesPredicate = new AutoScalingGroupsNamePredicate(autoScalingGroupNamesList);
		when(metric.getDimensions()).thenReturn(Lists.newArrayList(dimension));
		when(dimension.getValue()).thenReturn("sampleName1");
		Assert.assertFalse(serviceNamesPredicate.apply(metric));

	}
}

