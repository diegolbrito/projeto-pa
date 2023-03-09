package com.myorg;

import software.amazon.awscdk.*;
import software.constructs.Construct;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.sqs.Queue;

import java.util.HashMap;
import java.util.Map;

public class AdesaoServiceStack extends Stack {
	private final String serviceName = "adesaoService";
	private final String logGroupName = serviceName + "LogGroup";
	private final String containerImage = "nginx:latest";
	private final int servicePort = 80;
	
    public AdesaoServiceStack(final Construct scope, final String id, Cluster cluster, Queue adesaoQueue, 
    		final String awsRegion) {
        super(scope, id, null);

        Map<String, String> env = new HashMap<>();
        env.put("AWS_REGION", awsRegion);
        env.put("QUEUE_NAME", adesaoQueue.getQueueName());

        var adesaoService = ApplicationLoadBalancedFargateService.Builder.create(this, "AlbPa01")
                .serviceName(serviceName)
                .cluster(cluster)
                .cpu(256)
                .memoryLimitMiB(512)
                .desiredCount(1)
                .listenerPort(servicePort)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName(serviceName)
                                .image(ContainerImage.fromRegistry(containerImage))
                                .containerPort(servicePort)
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                        .logGroup(LogGroup.Builder.create(this, logGroupName)
                                                .logGroupName(logGroupName)
                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                .build())
                                        .streamPrefix(serviceName)
                                        .build()))
                                .environment(env)
                                .build())
                .publicLoadBalancer(true)
                .assignPublicIp(true)
                .build();

        adesaoService.getTargetGroup().configureHealthCheck(new HealthCheck.Builder()
                .path("/")
                .port(Integer.toString(servicePort))
                .healthyHttpCodes("200")
                .build());

        ScalableTaskCount scalableTaskCount = adesaoService.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(1)
                .maxCapacity(2)
                .build());

        scalableTaskCount.scaleOnCpuUtilization(serviceName + "AutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50)
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build());

        adesaoQueue.grantSendMessages(adesaoService.getTaskDefinition().getTaskRole());
    }
}




















