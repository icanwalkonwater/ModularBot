package com.jesus_crie.modularbot.v8support.proxying;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If applied on a class, tell the proxy maker to only map
 * the annotated methods.
 * <p>
 * If applied to a method, tell the proxy maker to map the method.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProxyExport {

    /**
     * If applied to a method, define the name of the mapped JS function.
     *
     * @return The name of the mapped JS function.
     */
    String name() default "";

    /**
     * If applied to a method, define whether or not the method can override
     * another property with the same name.
     *
     * @return Whether are not the method has the priority over an overload.
     */
    boolean override() default true;
}
