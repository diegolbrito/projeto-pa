package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.QueueProcessingFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.sqs.Queue;

import java.util.HashMap;
import java.util.List;

public class DatabaseWorkerStack extends Stack {
	private final String serviceName = "databaseWorker";
	private final String logGroupName = serviceName + "LogGroup";
	private final String containerImage = "nginx:latest";
	
    public DatabaseWorkerStack(final Construct scope, final String id, Cluster cluster, Queue databaseQueue,
    		final String awsRegion) {
        super(scope, id, null);

        var env = new HashMap<String, String>();
        env.put("AWS_REGION", awsRegion);
        env.put("QUEUE_NAME", databaseQueue.getQueueName());
                
        var databaseService = QueueProcessingFargateService.Builder.create(this, serviceName)
                .cluster(cluster)
                .cpu(256)
                .memoryLimitMiB(512)
                .image(ContainerImage.fromRegistry(containerImage))
                .containerName(serviceName)
                .minScalingCapacity(0)
                .maxScalingCapacity(2)
                .visibilityTimeout(Duration.seconds(30))
                .capacityProviderStrategies(List.of(CapacityProviderStrategy.builder()
                        .capacityProvider("FARGATE_SPOT")
                        .weight(2)
                        .build(), CapacityProviderStrategy.builder()
                        .capacityProvider("FARGATE")
                        .weight(1)
                        .build()))
                .queue(databaseQueue)
                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(this, logGroupName)
                                .logGroupName(logGroupName)
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .build())
                        .streamPrefix(serviceName)
                        .build()))
                .environment(env)
                .assignPublicIp(true)
                .build();

        databaseQueue.grantConsumeMessages(databaseService.getTaskDefinition().getTaskRole());
    }
}























