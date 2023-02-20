package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.Cluster;

public class ClusterStack extends Stack {
    private final Cluster cluster;
    private final String clusterName = "cluster-pa";

    public ClusterStack(final Construct scope, final String id, Vpc vpc) {
        super(scope, id, null);

        cluster = Cluster.Builder.create(this, clusterName)
                .clusterName(clusterName)
                .vpc(vpc)
                .enableFargateCapacityProviders(true)
                .build();
    }

    public Cluster getCluster() {
        return cluster;
    }
}
