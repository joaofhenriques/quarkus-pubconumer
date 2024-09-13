package org.acme;

import java.util.HashMap;
import java.util.Map;

import jakarta.ws.rs.container.ContainerRequestContext;

public class InterceptorUtils {


    public static final String HEADER_TRACE_ID = "x-trace-id";

    public static final String PROPERTY_EXTRAS = "REQUEST_EXTRAS";
    public static final String PROPERTY_TRACE_ID = "REQUEST_TRACE_ID";
    public static final String PROPERTY_ST = "REQUEST_ST";

    private InterceptorUtils() {}


    public static Map<String, String> getExtras(ContainerRequestContext containerRequestContext) {
        try {
            if ( containerRequestContext != null ) {
                @SuppressWarnings("unchecked")
                var extras = (HashMap<String, String>) containerRequestContext.getProperty(PROPERTY_EXTRAS);
                if ( extras == null ) {
                    extras = new HashMap<>();
                    containerRequestContext.setProperty(PROPERTY_EXTRAS, extras);
                }
                return extras;
            }
        } catch(Exception e) {}

        return new HashMap<>();
    }

    public static String putExtra(ContainerRequestContext containerRequestContext, String name, String value) {
        return getExtras(containerRequestContext).put(name, value);
    }

    public static String getExtra(ContainerRequestContext containerRequestContext, String name) {
        return getExtras(containerRequestContext).get(name);
    }
    
}
