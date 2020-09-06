package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.Capability;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
public @interface RequiredCapability {
    Class<? extends Capability<?>> value();
}
