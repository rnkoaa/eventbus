package com.richard.eventbus.annotation.processor;

// https://github.com/greenrobot/EventBus/blob/842a4a312c4ea1592bd7067a00495eebe129c078/EventBusAnnotationProcessor/src/org/greenrobot/eventbus/annotationprocessor/EventBusAnnotationProcessor.java#L146
// https://github.com/amoakoagyei/excalibur/blob/f90e6875f86dd4016b5a18c83e730bb13b0d597d/annotation-processor/src/main/java/com/excalibur/annotation/model/AggregateEventGroupedClasses.java#L59

import com.richard.eventbus.annotation.EventListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

public class EventListenerGroupedClasses {

    private final Map<String, EventListenerAnnotatedClass> itemsMap = new LinkedHashMap<>();

    /**
     * Adds an annotated class to this factory.
     *
     * @throws ProcessingException if another annotated class with the same id is already present.
     */
    public void add(EventListenerAnnotatedClass toInsert) throws ProcessingException {

        EventListenerAnnotatedClass existing = itemsMap.get(toInsert.getId());
        if (existing != null) {

            // Already existing
            throw new ProcessingException(
                toInsert.eventTypeElement(),
                "Conflict: The class %s is annotated with @%s with id ='%s' but %s already uses the same id",
                toInsert.eventTypeElement().getQualifiedName().toString(),
                EventListener.class.getSimpleName(),
                toInsert.getId(), existing.eventTypeElement().getQualifiedName().toString());
        }

        itemsMap.put(toInsert.getId(), toInsert);
    }

    public int size() {
        return itemsMap.size();
    }

    public void clear() {
        itemsMap.clear();
    }

    //Event class,EventClassListener
    public void writeIndexFile(Messager messager, Filer filer) {
        itemsMap.forEach((key, value) -> {

            TypeElement eventTypeElement = value.eventTypeElement();
            String eventTypeName = eventTypeElement.getQualifiedName().toString();

            messager.printMessage(Kind.NOTE, "processing " + key + " event " + eventTypeName);
        });

    }
}
