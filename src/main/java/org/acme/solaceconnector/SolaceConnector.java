package org.acme.solaceconnector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.solace.messaging.MessagingService;
import com.solace.messaging.config.MissingResourcesCreationConfiguration.MissingResourcesCreationStrategy;
import com.solace.messaging.publisher.DirectMessagePublisher;
import com.solace.messaging.resources.Queue;
import com.solace.messaging.resources.Topic;
import com.solace.messaging.util.LifecycleControl;

import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import jakarta.ws.rs.Priorities;

@ApplicationScoped
public class SolaceConnector {

    private static final Object INIT_MUTEX = new Object();
    private boolean IS_INIT = false;
    private MessagingService solace;
    private BeanManager beanManager;
    private List<LifecycleControl> solaceReceiverProducer = new ArrayList<>();
    private DirectMessagePublisher publisher = null;

    public SolaceConnector(BeanManager beanManager, MessagingService solace) {
        this.beanManager = beanManager;
        this.solace = solace;
    }

    // private PersistentMessageReceiver subscribeConsumeQueue(Queue queue, MessageHandler mh,
    //         MissingResourcesCreationStrategy stategy) {
    //     var receiver = solace.createPersistentMessageReceiverBuilder()
    //             .withMessageAutoAcknowledgement()
    //             .withMissingResourcesCreationStrategy(stategy)
    //             .build(queue)
    //             .start();

    //     queueList.add(receiver);

    //     receiver.receiveAsync(mh);

    //     return receiver;
    // }

    // public PersistentMessageReceiver subscribeQueue(MessageHandler mh, String queue) {
    //     return subscribeQueue(Queue.durableNonExclusiveQueue(queue), mh,
    //             MissingResourcesCreationStrategy.DO_NOT_CREATE);
    // }

    // public PersistentMessageReceiver subscribeQueue(MessageHandler mh, String queue, Function<String, Queue> qb) {
    //     return subscribeQueue(qb.apply(queue), mh, MissingResourcesCreationStrategy.DO_NOT_CREATE);
    // }

    // public PersistentMessageReceiver subscribeNonDurableExclusiveQueue(MessageHandler mh) {
    //     return subscribeQueue(Queue.nonDurableExclusiveQueue(), mh, MissingResourcesCreationStrategy.DO_NOT_CREATE);
    // }

    public static Queue createQueue(SolaceQueueTye type, String name) {
        Queue q = null;
        switch (type) {
            case DURABLE_EXCLUSIVE:
                q = Queue.durableExclusiveQueue(name);

            case NON_DURABLE_EXCLUSIVE:
                if (name != null && !name.isEmpty())
                    q = Queue.nonDurableExclusiveQueue(name);
                else
                    q = Queue.nonDurableExclusiveQueue();

            case DURABLE_NON_EXCLUSIVE:
            default:
                q = Queue.durableNonExclusiveQueue(name);
        }
        return q;
    }

    public static Topic getTopic(Method m) {
        var qc = m.getAnnotation(SolaceProduce.class);
        if( qc != null ) {
            var tName = qc.value();
            return Topic.of(tName);
        }
        return null;
    }

    private void createConsumeQueue(Object o, Method m) {

        var qc = m.getAnnotation(SolaceConsume.class);
        var qName = qc.value();
        var qType = qc.type();
        var qStrategy = qc.autoCreate() ? MissingResourcesCreationStrategy.CREATE_ON_START
                : MissingResourcesCreationStrategy.DO_NOT_CREATE;

        Queue q = createQueue(qType, qName);

        var receiver = solace.createPersistentMessageReceiverBuilder()
                .withMessageAutoAcknowledgement()
                .withMissingResourcesCreationStrategy(qStrategy)
                .build(q)
                .start();

        solaceReceiverProducer.add(receiver);
        receiver.receiveAsync(msg -> {
            try {
                m.invoke(o, msg);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    private void createProducer() {

        this.publisher = solace.createDirectMessagePublisherBuilder()
                .build().start();
        solaceReceiverProducer.add(this.publisher);

    }
 

    @Startup
    @Priority(Priorities.USER + 1)
    public void init() {

        synchronized(INIT_MUTEX) {

            if (IS_INIT)
                return;

            IS_INIT = true;
            boolean hasProducer = false;

            for (final Bean<?> b : beanManager.getBeans(Object.class)) {
                for (var m : b.getBeanClass().getMethods()) {
                    if (m.isAnnotationPresent(SolaceConsume.class)) {

                        var ctx = beanManager.createCreationalContext(b);
                        var ret = beanManager.getReference(b, b.getBeanClass(), ctx);
                        
                        this.createConsumeQueue(ret, m);
                    }
                    if (m.isAnnotationPresent(SolaceProduce.class)) {
                        hasProducer = true;
                    }
                }
            }
            
            if( hasProducer )
                createProducer();
        }
    }

    public void publishMessage(String message, Topic topic) {
        this.publisher.publish(message, topic);
    }

    @Shutdown
    public void stop() {
        solaceReceiverProducer.stream().forEach(r -> {
            try {
                r.terminate(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
