package com.jcloisterzone.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.jcloisterzone.EventBusExceptionHandler;
import com.jcloisterzone.online.Channel;
import com.jcloisterzone.ui.panel.ChannelPanel;
import com.jcloisterzone.wsio.Connection;

public class ChannelController {

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Client client;
    private final Channel channel;

    private final EventBus eventBus;

    private ChannelPanel channelPanel;

    public ChannelController(Client client, Channel channel) {
        this.client = client;
        this.channel = channel;

        eventBus = new EventBus(new EventBusExceptionHandler("channel UI event bus"));
        eventBus.register(this);
        InvokeInSwingUiAdapter uiAdapter = new InvokeInSwingUiAdapter(eventBus);
        channel.getEventBus().register(uiAdapter);
    }

    public void register(Object subscriber) {
        eventBus.register(subscriber);
    }

    public Connection getConnection() {
        return client.getConnection();
    }

    public Channel getChannel() {
		return channel;
	}

	public ChannelPanel getChannelPanel() {
		return channelPanel;
	}

	public void setChannelPanel(ChannelPanel channelPanel) {
		this.channelPanel = channelPanel;
	}
}
