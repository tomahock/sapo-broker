package pt.com.broker.ws.providers;

import org.codehaus.jackson.map.ObjectMapper;
import pt.com.broker.ws.models.*;
import pt.com.broker.ws.models.Error;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 27-06-2014.
 */
@Provider()
public class NotFoundMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException e) {

        Error error;


            if(e.getResponse().getStatus() == 404){
                error = Error.RESOURCE_NOT_FOUND;
            }else{
                error = Error.INVALID_REQUEST;
            }


        return Response.status(e.getResponse().getStatus()).
                entity(error)
                .type("application/json")
                .build();
    }
}
