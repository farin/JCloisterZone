package com.jcloisterzone.wsio.message;

public interface WsChainedMessage {

    String getParentId();
    void setParentId(String parentId);
}
