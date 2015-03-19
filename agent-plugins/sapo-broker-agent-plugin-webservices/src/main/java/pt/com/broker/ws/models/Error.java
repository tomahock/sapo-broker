package pt.com.broker.ws.models;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *

 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 24-06-2014.
 */
public class Error {


    public final static Error INVALID_REQUEST = new Error(99999, "Invalid Request");

    public final static Error RESOURCE_NOT_FOUND= new Error(10004, "Resource not found");

    private int code;

    private String message;
    
    @JsonCreator
    public Error(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
