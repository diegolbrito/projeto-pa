package com.myorg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.FilterCriteria;
import software.amazon.awscdk.services.lambda.FilterRule;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.StartingPosition;
import software.amazon.awscdk.services.lambda.eventsources.DynamoEventSource;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

public class DatabaseFunctionStack extends Stack {
	private final Function function;
	private final String functionName = "databaseFunction";
	
	public DatabaseFunctionStack(final Construct scope, final String id, Queue databaseQueue, Vpc vpc, Table adesaoTable, final String awsRegion) {
        super(scope, id, null);
        
        var env = new HashMap<String, String>();
        env.put("QUEUE_NAME", databaseQueue.getQueueName());
        
        function = Function.Builder.create(this, functionName)
        		.functionName(functionName)
        		.environment(env)
        		.vpc(vpc)
                .memorySize(256)
                .runtime(Runtime.NODEJS_18_X)
                .code(Code.fromAsset("src\\main\\resources"))
                .handler("function.main")
                .maxEventAge(Duration.seconds(60))
                .environment(env)
                .logRetention(RetentionDays.ONE_DAY)
        		.build();
        
        databaseQueue.grantSendMessages(function.getRole());
        adesaoTable.grantFullAccess(function.getRole());
        
        function.addEventSource(DynamoEventSource.Builder.create(adesaoTable)
                .startingPosition(StartingPosition.LATEST)
                .filters(List.of(FilterCriteria.filter(Map.of("eventName", FilterRule.isEqual("INSERT")))))
                .build());
	}
	
	public Function getFunction() {
		return function;
	}
}
