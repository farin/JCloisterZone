package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("ERR")
public class ErrorMessage extends AbstractWsMessage {

    public static final String BAD_VERSION = "badVersion";
    public static final String INVALID_PASSWORD = "invalidPassword";
    public static final String NOT_ALLOWED = "notAllowed";

    private String code;
    private String message;
    private String arg;

    public ErrorMessage() {
    }

    public ErrorMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getArg() {
        return arg;
    }


    public void setArg(String arg) {
        this.arg = arg;
    }



}
