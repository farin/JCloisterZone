package com.jcloisterzone.ui.controls.chat;

import java.awt.Color;

import com.jcloisterzone.event.ChatEvent;
import com.jcloisterzone.online.Channel;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.wsio.message.PostChatMessage;

public class ChannelChatPanel extends ChatPanel {

	private final Channel channel;

	public ChannelChatPanel(Client client, Channel chanel) {
		super(client);
		this.channel = chanel;
	}

	@Override
	protected ReceivedChatMessage createReceivedMessage(ChatEvent ev) {
		return new ReceivedChatMessage(ev, ev.getRemoteClient().getName(), Color.BLACK);
	}

	@Override
	protected PostChatMessage createPostChatMessage(String msg) {
		PostChatMessage pcm = new PostChatMessage(msg);
		pcm.setChannel(channel.getName());
		return pcm;
	}

}
