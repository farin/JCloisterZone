package com.jcloisterzone.wsio;

import com.jcloisterzone.wsio.message.AbandonGameMessage;
import com.jcloisterzone.wsio.message.BazaarBidMessage;
import com.jcloisterzone.wsio.message.BazaarBuyOrSellMessage;
import com.jcloisterzone.wsio.message.CaptureFollowerMessage;
import com.jcloisterzone.wsio.message.ChannelMessage;
import com.jcloisterzone.wsio.message.ChatMessage;
import com.jcloisterzone.wsio.message.ClientUpdateMessage;
import com.jcloisterzone.wsio.message.ClockMessage;
import com.jcloisterzone.wsio.message.CommitMessage;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage;
import com.jcloisterzone.wsio.message.CreateGameMessage;
import com.jcloisterzone.wsio.message.DeployFlierMessage;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;
import com.jcloisterzone.wsio.message.ErrorMessage;
import com.jcloisterzone.wsio.message.ExchangeFollowerChoiceMessage;
import com.jcloisterzone.wsio.message.GameMessage;
import com.jcloisterzone.wsio.message.GameOverMessage;
import com.jcloisterzone.wsio.message.GameSetupMessage;
import com.jcloisterzone.wsio.message.GameUpdateMessage;
import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.message.LeaveGameMessage;
import com.jcloisterzone.wsio.message.LeaveSlotMessage;
import com.jcloisterzone.wsio.message.MoveNeutralFigureMessage;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.PayRansomMessage;
import com.jcloisterzone.wsio.message.PingMessage;
import com.jcloisterzone.wsio.message.PlaceTileMessage;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;
import com.jcloisterzone.wsio.message.PongMessage;
import com.jcloisterzone.wsio.message.PostChatMessage;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage;
import com.jcloisterzone.wsio.message.SetExpansionMessage;
import com.jcloisterzone.wsio.message.SetRuleMessage;
import com.jcloisterzone.wsio.message.SlotMessage;
import com.jcloisterzone.wsio.message.StartGameMessage;
import com.jcloisterzone.wsio.message.TakeSlotMessage;
import com.jcloisterzone.wsio.message.ToggleClockMessage;
import com.jcloisterzone.wsio.message.UndoMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;
import com.jcloisterzone.wsio.message.WsMessage;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

public class WsCommandRegistry {

    public static final Map<String, Class<? extends WsMessage>> TYPES;

    static {
        TYPES = HashMap.<String, Class<? extends WsMessage>>empty()
            .put(t(ErrorMessage.class))
            .put(t(HelloMessage.class))
            .put(t(WelcomeMessage.class))
            .put(t(CreateGameMessage.class))
            .put(t(JoinGameMessage.class))
            .put(t(LeaveGameMessage.class))
            .put(t(AbandonGameMessage.class))
            .put(t(GameMessage.class))
            .put(t(GameSetupMessage.class))
            .put(t(TakeSlotMessage.class))
            .put(t(LeaveSlotMessage.class))
            .put(t(SlotMessage.class))
            .put(t(SetExpansionMessage.class))
            .put(t(SetRuleMessage.class))
            .put(t(StartGameMessage.class))
            .put(t(DeployFlierMessage.class))
            .put(t(UndoMessage.class))
            .put(t(ClientUpdateMessage.class))
            .put(t(GameUpdateMessage.class))
            .put(t(PostChatMessage.class))
            .put(t(ChatMessage.class))
            .put(t(ChannelMessage.class))
            .put(t(GameOverMessage.class))
            .put(t(PingMessage.class))
            .put(t(PongMessage.class))
            .put(t(ToggleClockMessage.class))
            .put(t(ClockMessage.class))
            .put(t(CommitMessage.class))
            .put(t(PassMessage.class))
            .put(t(PlaceTileMessage.class))
            .put(t(DeployMeepleMessage.class))
            .put(t(ReturnMeepleMessage.class))
            .put(t(MoveNeutralFigureMessage.class))
            .put(t(PlaceTokenMessage.class))
            .put(t(CaptureFollowerMessage.class))
            .put(t(PayRansomMessage.class))
            .put(t(ExchangeFollowerChoiceMessage.class))
            .put(t(BazaarBidMessage.class))
            .put(t(BazaarBuyOrSellMessage.class))
            .put(t(CornCircleRemoveOrDeployMessage.class));
    }

    private static Tuple2<String, Class<? extends WsMessage>> t(Class<? extends WsMessage> msgType) {
        return new Tuple2<>(
            msgType.getAnnotation(WsMessageCommand.class).value(),
            msgType
        );
    }
}
