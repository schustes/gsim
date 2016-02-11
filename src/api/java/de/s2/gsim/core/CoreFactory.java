package de.s2.gsim.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CoreFactory {

    String name();

    boolean isDefault() default true;

}
