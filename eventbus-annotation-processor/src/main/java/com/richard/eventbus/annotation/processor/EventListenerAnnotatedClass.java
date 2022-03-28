package com.richard.eventbus.annotation.processor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public record EventListenerAnnotatedClass(Types typeUtils,
                                          Elements elementUtils,
                                          ExecutableElement element) {

    public boolean isConstructor() {
        return methodName().equals("<init>");
    }

    public TypeElement eventTypeElement() {
        VariableElement variableElement = element.getParameters().get(0);
        return (TypeElement) typeUtils.asElement(variableElement.asType());
    }

    public String methodName() {
        return element.getSimpleName().toString();
    }

    public String getId() {
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        return classElement.getQualifiedName().toString();
    }
}
