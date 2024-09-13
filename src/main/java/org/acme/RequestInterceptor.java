package org.acme;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(1)
public class RequestInterceptor implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        containerRequestContext.setProperty(InterceptorUtils.PROPERTY_TRACE_ID,
                containerRequestContext.getHeaderString(InterceptorUtils.HEADER_TRACE_ID));
        containerRequestContext.setProperty(InterceptorUtils.PROPERTY_ST, System.currentTimeMillis());
    }

}
