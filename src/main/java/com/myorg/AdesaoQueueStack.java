package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;

public class AdesaoQueueStack extends Stack {
    private final Queue queue;
    private final String queueName = "adesaoQueue";

    public AdesaoQueueStack(final Construct scope, final String id) {
        super(scope, id, null);
        
        queue = Queue.Builder.create(this, queueName)
                .queueName(queueName)
                .build();
        
        DeadLetterQueue.builder()
                .queue(queue)
                .maxReceiveCount(3)
                .build();
    }

    public Queue getQueue() {
        return queue;
    }
}
