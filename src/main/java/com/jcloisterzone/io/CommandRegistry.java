package com.jcloisterzone.io;

import com.jcloisterzone.io.message.*;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

public class CommandRegistry {

    public static final Map<String, Class<? extends Message>> TYPES;

    static {
        TYPES = HashMap.<String, Class<? extends Message>>empty()
            .put(t(GameSetupMessage.class))
            .put(t(DeployFlierMessage.class))
            .put(t(UndoMessage.class))
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
            .put(t(FlockMessage.class));
    }

    private static Tuple2<String, Class<? extends Message>> t(Class<? extends Message> msgType) {
        return new Tuple2<>(
            msgType.getAnnotation(MessageCommand.class).value(),
            msgType
        );
    }
}
