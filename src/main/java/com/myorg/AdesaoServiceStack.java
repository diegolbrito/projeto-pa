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
	private final String containerImage = "siecola/curso_aws_project01:1.7.0";
	private final int servicePort = 3000;
	
    public AdesaoServiceStack(final Construct scope, final String id, Cluster cluster, Queue adesaoQueue, 
    		final String awsRegion) {
        super(scope, id, null);

        Map<String, String> env = new HashMap<>();
        env.put("AWS_REGION", awsRegion);
        env.put("QUEUE_NAME", adesaoQueue.getQueueName());

        var adesaoService = ApplicationLoadBalancedFargateService.Builder.create(this, "AlbPa01")
                .serviceName(serviceName)
                .cluster(cluster)
                .cpu(512)
                .memoryLimitMiB(256)
                .desiredCount(2)
                .listenerPort(servicePort)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName(serviceName)
                                .image(ContainerImage.fromRegistry(containerImage))
                                .containerPort(3000)
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
                .build();

        adesaoService.getTargetGroup().configureHealthCheck(new HealthCheck.Builder()
                .path("/actuator/health")
                .port(Integer.toString(servicePort))
                .healthyHttpCodes("200")
                .build());

        ScalableTaskCount scalableTaskCount = adesaoService.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(2)
                .maxCapacity(4)
                .build());

        scalableTaskCount.scaleOnCpuUtilization(serviceName + "AutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50)
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build());

        adesaoQueue.grantSendMessages(adesaoService.getTaskDefinition().getTaskRole());
    }
}




















