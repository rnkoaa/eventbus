package com.richard.eventbus.annotation.processor;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

public abstract class AbstractAnnotationProcessor extends AbstractProcessor {

    protected Messager messager;
    protected Filer filer;
    protected Types typeUtils;
    protected Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
    }

    public boolean hasPublicModifier(Set<Modifier> modifiers) {
        return modifiers.stream()
            .anyMatch(m -> m.equals(Modifier.PUBLIC));
    }

    private void error(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    protected void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

    protected void note(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    protected void error(Element e, String msg, Object... args) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            String.format(msg, args),
            e);
    }

    protected void note(String msg, Object... args) {
        messager.printMessage(
            Kind.NOTE,
            String.format(msg, args));
    }

    protected void error(String msg, Object... args) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            String.format(msg, args));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
