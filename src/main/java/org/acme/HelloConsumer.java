package org.acme;


import org.acme.solaceconnector.SolaceConsume;
import org.acme.solaceconnector.SolaceProduce;

import com.solace.messaging.receiver.InboundMessage;

import io.quarkus.arc.Unremovable;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
@Unremovable
public class HelloConsumer {

    @SolaceConsume("q/couves1")
    @SolaceProduce("t/couves2")
    public String consumeCouves(InboundMessage m) {
        var p = m.getPayloadAsString();
        Log.infof("Received message consumeCouves: %s", p);
        //throw new RuntimeException("ups");
        return p;
    }


    @SolaceProduce("t/couves1")
    public String produceCouves(String in) {
        return in;
    }

}

