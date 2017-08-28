package com.jcloisterzone.ui.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jcloisterzone.ui.grid.actionpanel.ActionInteractionPanel;

@Inherited
@Retention(RUNTIME)
@Target(TYPE)
public @interface LinkedPanel {
    Class<? extends ActionInteractionPanel<?>> value();
}
