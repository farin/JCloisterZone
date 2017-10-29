package com.jcloisterzone.ui.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jcloisterzone.ui.grid.ActionLayer;

@Inherited
@Retention(RUNTIME)
@Target(TYPE)
public @interface LinkedGridLayer {
    Class<? extends ActionLayer> value();
}
