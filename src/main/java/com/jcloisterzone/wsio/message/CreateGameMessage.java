package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("CREATE_GAME")
public class CreateGameMessage extends AbstractWsMessage implements WsInChannelMessage {

    private String name;
    private String channel;
    private String password;

    public CreateGameMessage() {
    }

    public CreateGameMessage(String name, String channel, String password) {
        this.name = name;
        this.channel = channel;
        this.password = password;
    }

    @Override
    public String getChannel() {
        return channel;
    }

    @Override
    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
