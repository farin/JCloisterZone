package com.jcloisterzone.ai;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jcloisterzone.game.Capability;

/**
 * Third-party capabilities can force AI support by this annotation.
 * All AI players which supports target capability can be played also
 * with annotated one.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface ForceSupportIfSupports {
    Class<? extends Capability<?>> value();
}
