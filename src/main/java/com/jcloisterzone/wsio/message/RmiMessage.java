package com.jcloisterzone.wsio.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("RMI")
public class RmiMessage implements WsInGameMessage {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private String gameId;
    private String method;
    private String args; //serialized

    public RmiMessage(String gameId, String method, Object[] args) {
        this.gameId = gameId;
        this.method = method;
        this.args = encode(args);
    }

    public String encode(Object object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();
            return new String(Base64Coder.encode(baos.toByteArray()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Object decode(String object) {
        try {
            byte[] data = Base64Coder.decode(object);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object o = ois.readObject();
            ois.close();
            return o;
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RMI {\"gameID\": \"").append(gameId).append("\", \"method\": \"").append(method).append("\"} args=[");
        Object[] oargs = (Object[]) decode(args);
        if (oargs != null) {
            for (int i = 0; i < oargs.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(oargs[i] == null ? null : oargs[i].toString());
            }
        }
        sb.append("]");
        return sb.toString();
    }

}
