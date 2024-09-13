package org.acme;

import java.io.IOException;
import java.util.Map;

import org.jboss.logging.Logger;

import com.google.gson.Gson;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Provider
@Priority(9999)
public class ResponseInterceptor implements ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(ResponseInterceptor.class);
    private static final Gson GSON = new Gson();


    @Builder
    @Getter
    @Setter
    private static class LogLine {
        String path;
        int status;
        long time;
        String traceId;
        Map<String, String> extras;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {

        try {
            var traceId = (String)containerRequestContext.getProperty(InterceptorUtils.PROPERTY_TRACE_ID);
            var tt = (Long)containerRequestContext.getProperty(InterceptorUtils.PROPERTY_ST);
            tt = tt == null ? 0L : System.currentTimeMillis() - tt;


            var logLine = LogLine.builder()
                .path(containerRequestContext.getUriInfo().getPath())
                .status(containerResponseContext.getStatusInfo().getStatusCode())
                .traceId(traceId)
                .extras(InterceptorUtils.getExtras(containerRequestContext))
                .time(tt)
                .build();

            if ( traceId != null )
                containerResponseContext.getHeaders().add(InterceptorUtils.HEADER_TRACE_ID, traceId);
            
            LOG.info(GSON.toJson(logLine));
            
        } catch(Exception e) {
            e.printStackTrace();
        }



    }


    
}
