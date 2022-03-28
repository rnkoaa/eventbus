package com.richard.eventbus.annotation.processor;

// https://github.com/greenrobot/EventBus/blob/842a4a312c4ea1592bd7067a00495eebe129c078/EventBusAnnotationProcessor/src/org/greenrobot/eventbus/annotationprocessor/EventBusAnnotationProcessor.java#L146

import com.google.auto.service.AutoService;
import com.richard.eventbus.annotation.EventListener;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class EventListenerProcessor extends AbstractAnnotationProcessor {

    private boolean writerRoundDone;
    private int round;
    private EventListenerGroupedClasses eventListenerGroupedClasses = new EventListenerGroupedClasses();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(EventListener.class);

        Set<? extends Element> invalidElements = elementsAnnotatedWith.stream()
            .filter(element -> !element.getKind().equals(ElementKind.METHOD))
            .collect(Collectors.toSet());

        if (invalidElements.size() > 0) {
            invalidElements.forEach(element -> {
                error(element, "Only methods can be annotated with @EventListener");
            });
            return true;
        }

        Set<ErrorElement> errorElements = elementsAnnotatedWith.stream()
            .filter(element -> element.getKind().equals(ElementKind.METHOD))
            .map(element -> (ExecutableElement) element)
            .map(this::checkHasNoErrors)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (errorElements.size() > 0) {
            for (ErrorElement errorElement : errorElements) {
                error(errorElement.element, errorElement.message);
            }
            return false;
        }

//        round++;
//        note("Processing round " + round + ", new annotations: " +
//            !annotations.isEmpty() + ", processingOver: " + roundEnv.processingOver());

        elementsAnnotatedWith.stream()
            .filter(element -> element.getKind().equals(ElementKind.METHOD))
            .map(element -> (ExecutableElement) element)
            .forEach(element -> {
                try {
                    eventListenerGroupedClasses.add(new EventListenerAnnotatedClass(typeUtils, elementUtils, element));
                } catch (ProcessingException ex) {
                    error(ex.element, ex.getMessage());
                }
            });

        if (roundEnv.processingOver()) {
            note("found %d classes to write", eventListenerGroupedClasses.size());
            eventListenerGroupedClasses.writeIndexFile(messager, filer);
            eventListenerGroupedClasses.clear();
        }
        return true;
    }

    private ErrorElement checkHasNoErrors(ExecutableElement element) {
        if (element.getModifiers().contains(Modifier.STATIC)) {
            return new ErrorElement(element, "EventListener method must not be static");
        }

        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            return new ErrorElement(element, "EventListener method must be public");
        }

        List<? extends VariableElement> parameters = element.getParameters();
        if (parameters.size() != 1) {
            return new ErrorElement(element, "EventListener method must have exactly 1 parameter");
        }
        return null;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(EventListener.class.getCanonicalName());
    }

    record ErrorElement(Element element, String message) {}
}
