package com.jcloisterzone.wsio;

import com.jcloisterzone.wsio.message.*;

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
            .put(t(SetCapabilityMessage.class))
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
            .put(t(CornCircleRemoveOrDeployMessage.class))
            .put(t(FlockMessage.class))
            .put(t(SyncGameMessage.class));


    }

    private static Tuple2<String, Class<? extends WsMessage>> t(Class<? extends WsMessage> msgType) {
        return new Tuple2<>(
            msgType.getAnnotation(WsMessageCommand.class).value(),
            msgType
        );
    }
}
