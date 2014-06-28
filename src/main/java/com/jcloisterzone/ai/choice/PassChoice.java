package com.jcloisterzone.ai.choice;

import com.jcloisterzone.ai.SavePoint;
import com.jcloisterzone.rmi.Client2ClientIF;

public class PassChoice extends AiChoice {

    public PassChoice(AiChoice previous, SavePoint savePoint) {
        super(previous, savePoint);

    }

    @Override
    public void perform(Client2ClientIF server) {
        server.pass();
    }

    @Override
    public String toString() {
        return "pass";
    }

}
