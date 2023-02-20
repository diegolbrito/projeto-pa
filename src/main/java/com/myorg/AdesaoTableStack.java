package com.myorg;

import software.amazon.awscdk.RemovalPolicy;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.dynamodb.*;

public class AdesaoTableStack extends Stack {
    private final Table table;
    private final String tableName = "adesoes";

    public AdesaoTableStack(final Construct scope, final String id) {
        super(scope, id, null);

        table = Table.Builder.create(this, tableName)
                .tableName(tableName)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .stream(StreamViewType.NEW_IMAGE)
                .partitionKey(Attribute.builder()
                        .name("pk")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("sk")
                        .type(AttributeType.STRING)
                        .build())
                .timeToLiveAttribute("ttl")
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    public Table getTable() {
        return table;
    }
}
















