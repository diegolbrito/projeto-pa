package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class ProjetoPaStack extends Stack {
	private final String awsRegion = "us-east-1";
	
    public ProjetoPaStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public ProjetoPaStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        var vpc = new VpcStack(scope, "vpcStack");
                       
        var cluster = new ClusterStack(scope, "clusterStack", vpc.getVpc());
        cluster.addDependency(vpc);
        
		var databaseQueue = new DatabaseQueueStack(scope, "databaseQueueStack");
		
		var adesaoTable = new AdesaoTableStack(scope, "adesaoTableStack");
		
		var databaseFunction = new DatabaseFunctionStack(scope, "databaseFunctionStack", databaseQueue.getQueue(), 
				vpc.getVpc(), adesaoTable.getTable(), awsRegion);
		databaseFunction.addDependency(vpc);
		databaseFunction.addDependency(databaseQueue);	 
		databaseFunction.addDependency(adesaoTable);
		
		var adesaoQueue = new AdesaoQueueStack(scope, "adesaoQueueStack");
					
		var adesaoWorker = new AdesaoWorkerStack(scope, "adesaoWorkerStack", cluster.getCluster(), adesaoQueue.getQueue(), 
				adesaoTable.getTable(), awsRegion);
		adesaoWorker.addDependency(cluster);
		adesaoWorker.addDependency(adesaoQueue);
		adesaoWorker.addDependency(adesaoTable);
		
		var databaseWorker = new DatabaseWorkerStack(scope, "databaseWorkerStack", cluster.getCluster(), 
				databaseQueue.getQueue(), awsRegion);
		databaseWorker.addDependency(cluster);
		databaseWorker.addDependency(databaseQueue);
		
		var adesaoService = new AdesaoServiceStack(scope, "adesaoServiceStack",
				cluster.getCluster(), adesaoQueue.getQueue(), awsRegion);
		adesaoService.addDependency(cluster);
		adesaoService.addDependency(adesaoQueue);
    }
}
