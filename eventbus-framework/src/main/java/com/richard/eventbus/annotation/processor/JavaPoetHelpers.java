package com.richard.eventbus.annotation.processor;

import com.richard.eventbus.framework.EventHandlerClassInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
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
            ClassName.get(EventHandlerClassInfo.class)
        );
    }

//    public static ParameterizedTypeName getParameterizedAggregateRoot() {
//        TypeName aggregateIdWildCard = WildcardTypeName.subtypeOf(AggregateId.class);
//        return ParameterizedTypeName.get(
//            ClassName.get(AbstractAggregateRoot.class), aggregateIdWildCard,
//            ClassName.get(VersionedEvent.class));
//    }
//
//    public static FieldSpec generateEventsApplierMapField(String fieldName) {
//        return FieldSpec.builder(
//                ParameterizedTypeName.get(
//                    ClassName.get(Map.class),
//                    classOfAny(),
//                    eventApplierBeanName
//                ),
//                fieldName
//            )
//            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//            .build();
//    }
}