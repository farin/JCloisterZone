package com.jcloisterzone.wsio;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.wsio.message.WsMessage;

public class MessageDispatcher {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public void dispatch(WsMessage msg, Object context, Object... targets) {
        if (targets.length == 0) {
            throw new IllegalArgumentException("No targets");
        }
        for (Object target : targets) {
            dispatchOn(msg, context, target);
        }
    }

    //TODO what about cache targets
    private boolean dispatchOn(WsMessage msg, Object context, Object target) {
        Class<?> type = target.getClass();
        for (Method m : type.getMethods()) {
            WsSubscribe handler = m.getAnnotation(WsSubscribe.class);
            if (handler != null) {
                Class<?> params[] = m.getParameterTypes();
                @SuppressWarnings("unchecked")
                Class<? extends WsMessage> cls = (Class<? extends WsMessage>) params[params.length-1];
                if (cls.equals(msg.getClass())) {
                    try {
                        if (params.length == 1) {
                            m.invoke(target, msg);
                        } else {
                            m.invoke(target, context, msg);
                        }
                    } catch (InvocationTargetException e) {
                        if (e.getCause() instanceof RuntimeException) {
                            throw (RuntimeException) e.getCause();
                        } else {
                            logger.error(e.getMessage(), e);
                        }
                    } catch (IllegalAccessException e) {
                        logger.error(e.getMessage(), e);
                    }
                    return true;
                }
            }
        }
        return false;
    }

}
