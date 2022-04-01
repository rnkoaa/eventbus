package com.richard.product.events;

import org.immutables.value.Value;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.PACKAGE, ElementType.TYPE})
@Value.Style(
        get = {"is*", "get*"},
        forceJacksonPropertyNames = false,
        typeAbstract = "*",
        typeImmutable = "*Impl",
        visibility = ImplementationVisibility.PUBLIC,
        overshadowImplementation = true
)
//@JsonSerialize
public @interface ApiStyle {
}
