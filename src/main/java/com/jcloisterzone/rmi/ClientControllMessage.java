package com.jcloisterzone.rmi;

import java.io.Serializable;

//temporaty mesage, before protocol will be revisited, allows reconnection
public class ClientControllMessage implements Serializable {

    private final Long clientId;

    public ClientControllMessage(Long clientId) {
        this.clientId = clientId;
    }

    public Long getClientId() {
        return clientId;
    }
}
