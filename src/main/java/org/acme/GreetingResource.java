package org.acme;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;



@RequestScoped
@Path("/hello")
public class GreetingResource {

    private static final Logger LOG = Logger.getLogger(GreetingResource.class);
    
    
    private ContainerRequestContext containerRequestContext;
    private HelloConsumer helloConsumer;

    public GreetingResource(ContainerRequestContext containerRequestContext, HelloConsumer helloConsumer) {
        this.containerRequestContext = containerRequestContext;
        this.helloConsumer = helloConsumer;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)

    public String hello() {
        
        InterceptorUtils.putExtra(containerRequestContext, "example", "value");

        helloConsumer.produceCouves("hello couves");
        //throw new CouvesException("couves");
        //throw new RuntimeException("couves");
        return "Hello from Quarkus REST 2";
    }


}
