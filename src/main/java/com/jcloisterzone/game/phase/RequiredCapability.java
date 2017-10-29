package com.jcloisterzone.game.phase;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jcloisterzone.game.Capability;

@Retention(RUNTIME)
@Target(TYPE)
public @interface RequiredCapability {
    Class<? extends Capability<?>> value();
}
