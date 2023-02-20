package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.Vpc;

public class VpcStack extends Stack {
    private final Vpc vpc;
    private final String vpcName = "vpc-pa";

    public VpcStack(final Construct scope, final String id) {
        super(scope, id, null);

        vpc = Vpc.Builder.create(this, vpcName)
        		.vpcName(vpcName)
                .maxAzs(3)
                .natGateways(0)
                .build();
    }

    public Vpc getVpc() {
        return vpc;
    }
}
