package com.richard.eventbus.annotation.processor;

import com.richard.eventbus.framework.EventHandlerClassInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;

import java.util.List;
import java.util.Map;

public class JavaPoetHelpers {

    public static ParameterizedTypeName classOfAny() {
        TypeName wildcard = WildcardTypeName.subtypeOf(Object.class);

        return ParameterizedTypeName.get(
            ClassName.get(Class.class), wildcard);
    }

    public static ParameterizedTypeName mapOfClassEventHandlerInfo() {
        return ParameterizedTypeName.get(
            ClassName.get(Map.class),
            classOfAny(),
            ParameterizedTypeName.get(
                ClassName.get(List.class),
                ClassName.get(EventHandlerClassInfo.class)
            )
        );
    }
}