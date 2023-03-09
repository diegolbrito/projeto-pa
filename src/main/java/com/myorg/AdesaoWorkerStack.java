package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.QueueProcessingFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.sqs.Queue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdesaoWorkerStack extends Stack {
	private final String workerName = "adesaoWorker";
	private final String logGroupName = workerName + "LogGroup";
	private final String containerImage = "nginx:latest";
	
    public AdesaoWorkerStack(final Construct scope, final String id, Cluster cluster, Queue adesaoQueue, 
    		Table adesaoTable, final String awsRegion) {
        super(scope, id, null);
        
        Map<String, String> env = new HashMap<>();
        env.put("AWS_REGION", awsRegion);
        env.put("QUEUE_NAME", adesaoQueue.getQueueName());
        env.put("TABLE_NAME", adesaoTable.getTableName());

        var adesaoWorker = QueueProcessingFargateService.Builder.create(this, workerName)
                .cluster(cluster)
                .cpu(256)
                .memoryLimitMiB(512)
                .image(ContainerImage.fromRegistry(containerImage))
                .containerName(workerName)
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
                .queue(adesaoQueue)
                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(this, logGroupName)
                                .logGroupName(logGroupName)
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .build())
                        .streamPrefix(workerName)
                        .build()))
                .environment(env)
                .assignPublicIp(true)
                .build();

        adesaoQueue.grantConsumeMessages(adesaoWorker.getTaskDefinition().getTaskRole());
        adesaoTable.grantWriteData(adesaoWorker.getTaskDefinition().getTaskRole());
    }
}























