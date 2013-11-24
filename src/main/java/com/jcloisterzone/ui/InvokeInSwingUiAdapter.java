package com.jcloisterzone.ui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.jcloisterzone.figure.Figure;


public class InvokeInSwingUiAdapter implements InvocationHandler {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final ClientController controller;

    public InvokeInSwingUiAdapter(ClientController client) {
        this.controller = client;
    }

    @Override
    public Object invoke(Object proxy, final Method method, final Object[] args) {
        freezeArgs(method, args);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    method.invoke(controller, args);
                } catch (InvocationTargetException ie) {
                    logger.error("Cannot invoke method " + method.toString() + " (probably bug in freezeArgs())", ie.getCause());
                } catch (Exception e) {
                    logger.error("Method " + method.toString() + " (probably bug in freezeArgs())", e);
                }
            }
        });
        return null;
    }

    @SuppressWarnings("rawtypes")
    private void freezeArgs(Method method, Object[] args) {
        if (args == null) return;
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Figure) {
                args[i] = ((Figure) args[i]).clone();
            }
            if (args[i] instanceof Collection<?>) {
                if (args[i] instanceof EnumSet) continue; //do not modify Enum set
                //Class paramType = method.getParameterTypes()[i];
                FigureCloningFunction func = new FigureCloningFunction();
                if (args[i] instanceof List) {
                    List<Object> list = new ArrayList<Object>(((List) args[i]).size());
                    for (Object obj : (List) args[i]) {
                        list.add(func.apply(obj));
                    }
                    args[i] = list;
                } else if (args[i] instanceof Set) {
                    Set<Object> set = new HashSet<>(((Set) args[i]).size());
                    for (Object obj : (Set) args[i]) {
                        set.add(func.apply(obj));
                    }
                    args[i] = set;
                }
                //for map do nothing
            }
        }
    }

    private class FigureCloningFunction implements Function<Object, Object> {
        @Override
        public Object apply(Object from) {
            if (from instanceof Figure) {
                return ((Figure) from).clone();
            }
            return from;
        }
    }
}
