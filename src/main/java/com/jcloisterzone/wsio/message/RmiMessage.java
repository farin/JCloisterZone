package com.jcloisterzone.wsio.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.jcloisterzone.rmi.CallMessage;
import com.jcloisterzone.wsio.Cmd;

@Cmd("RMI")
public class RmiMessage implements WsMessage {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private String gameId;
    private String method; // for debug purposes
    private String call;

    public RmiMessage(String gameId) {
        this.gameId = gameId;
    }

    public void encode(CallMessage callMessage) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(callMessage);
            oos.close();
            call = new String(Base64Coder.encode(baos.toByteArray()));
            method = callMessage.getMethod();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public CallMessage decode() {
        try {
            byte[] data = Base64Coder.decode(call);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object o = ois.readObject();
            ois.close();
            return (CallMessage) o;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getCall() {
        return call;
    }

    public void setCall(String call) {
        this.call = call;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
