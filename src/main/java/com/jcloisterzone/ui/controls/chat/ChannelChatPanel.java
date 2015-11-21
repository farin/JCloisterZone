package com.jcloisterzone.ui.controls.chat;

import com.jcloisterzone.event.ChatEvent;
import com.jcloisterzone.ui.ChannelController;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.wsio.message.PostChatMessage;

public class ChannelChatPanel extends ChatPanel {

	private final ChannelController cc;

	public ChannelChatPanel(Client client, ChannelController cc) {
		super(client);
		this.cc = cc;
	}

	@Override
	protected ReceivedChatMessage createReceivedMessage(ChatEvent ev) {
		if (ev.getRemoteClient() == null) {
			return new ReceivedChatMessage(ev, "* play.jcz *", client.getTheme().getChatSystemColor());
		} else {
			boolean isMe = cc.getConnection().getSessionId().equals(ev.getRemoteClient().getSessionId());
			return new ReceivedChatMessage(ev, ev.getRemoteClient().getName(), isMe ?
					client.getTheme().getChatMyColor() :
					client.getTheme().getChatNeutralColor());
		}
	}

	@Override
	protected PostChatMessage createPostChatMessage(String msg) {
		PostChatMessage pcm = new PostChatMessage(msg);
		pcm.setChannel(cc.getChannel().getName());
		return pcm;
	}

}
