package org.acme;


import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

public class CustomExceptionMapper {

    private CustomExceptionMapper() {}


    @Provider
    public static class CouvesMapper implements ExceptionMapper<CouvesException> {
        @Override
        public Response toResponse(CouvesException x) {

            return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity("CouvesException: " + x.getMessage()).build();
        }
    }

    @Provider
    public static class MyExceptionMapper implements ExceptionMapper<Exception> {

        @Override
        public Response toResponse(Exception x) {

            return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity("Exception: " + x.getMessage()).build();
        }
    }


}
