package com.richard.eventbus.annotation.processor;

// https://github.com/greenrobot/EventBus/blob/842a4a312c4ea1592bd7067a00495eebe129c078/EventBusAnnotationProcessor/src/org/greenrobot/eventbus/annotationprocessor/EventBusAnnotationProcessor.java#L146
// https://github.com/amoakoagyei/excalibur/blob/f90e6875f86dd4016b5a18c83e730bb13b0d597d/annotation-processor/src/main/java/com/excalibur/annotation/model/AggregateEventGroupedClasses.java#L59

import static com.richard.eventbus.annotation.processor.JavaPoetHelpers.classOfAny;
import static javax.lang.model.element.Modifier.PUBLIC;

import com.richard.eventbus.annotation.EventListener;
import com.richard.eventbus.framework.EventBusIndex;
import com.richard.eventbus.framework.EventHandlerClassInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
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
    public void writeIndexFile(String indexFilePath, Messager messager, Filer filer) throws IOException {
        Set<String> entries = itemsMap.entrySet()
            .stream()
            .map(entry -> {
                String key = entry.getKey();
                EventListenerAnnotatedClass value = entry.getValue();
                String eventTypeName = value.eventTypeElement().getQualifiedName().toString();
                return "%s,%s".formatted(key, eventTypeName);
            })
            .collect(Collectors.toSet());
        String metaINFPath = "META-INF/" + indexFilePath;
        messager.printMessage(Kind.NOTE, String.format("writing %d entries to path %s", entries.size(), metaINFPath));
        IndexFileUtil.writeSimpleNameIndexFile(filer, entries, metaINFPath);
    }

    public void generateEventBusIndexClass(String packageName, Messager messager, Filer filer) throws IOException {
        messager.printMessage(Kind.NOTE, "Package: " + packageName);
        String className = "EventBusIndexGeneratedImpl";
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(className)
            .addModifiers(PUBLIC, Modifier.FINAL)
            .addSuperinterface(EventBusIndex.class)
            .addMethod(MethodSpec.methodBuilder("getEventHandlerClass")
                .addAnnotation(Override.class)
                .addParameter(classOfAny(), "eventClass")
                .returns(ParameterizedTypeName.get(
                    ClassName.get(Optional.class),
                    ClassName.get(EventHandlerClassInfo.class)
                ))
                .addStatement("\treturn null;")
                .build())
            .addMethod(MethodSpec.methodBuilder("getEventHandlers")
                .addAnnotation(Override.class)
                .returns(JavaPoetHelpers.mapOfClassEventHandlerInfo())
                .addStatement("\treturn null;")
                .build())
            .addMethod(MethodSpec.methodBuilder("put")
                .addParameter(
                    ClassName.get(EventHandlerClassInfo.class),
                    "eventClassInfo"
                )
                .addAnnotation(Override.class)
                .build());

        JavaFile javaFile = JavaFile.builder(packageName, typeSpecBuilder.build())
            .build();
        javaFile.writeTo(filer);
    }

//    https://github.com/amoakoagyei/excalibur/blob/main/annotation-processor/src/main/java/com/excalibur/annotation/model/EventSourcingApplierGroupedClasses.java
    // https://github.com/amoakoagyei/excalibur/blob/main/annotation-processor/src/main/java/com/excalibur/annotation/model/CommandHandlerGroupedClasses.java
}
