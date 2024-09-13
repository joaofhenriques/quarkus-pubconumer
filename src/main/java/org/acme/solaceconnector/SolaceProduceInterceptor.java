package org.acme.solaceconnector;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.Priorities;

@SolaceProduce
@Priority(Priorities.USER + 10)
@Interceptor
public class SolaceProduceInterceptor {

    private SolaceConnector solaceConnector;

    public SolaceProduceInterceptor(SolaceConnector solaceConnector) {
        this.solaceConnector = solaceConnector;
    }


    @AroundInvoke
    public Object invoke(InvocationContext ctx) throws Exception {
        var ret = ctx.proceed();
        if ( ctx.getMethod().isAnnotationPresent(SolaceProduce.class) ) {
            if ( ret instanceof String )
            {
                solaceConnector.publishMessage((String)ret, SolaceConnector.getTopic(ctx.getMethod()));
            }
        }
        return ret;   
    }
}
