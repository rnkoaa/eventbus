package com.richard.eventbus.annotation.processor;

// https://github.com/greenrobot/EventBus/blob/842a4a312c4ea1592bd7067a00495eebe129c078/EventBusAnnotationProcessor/src/org/greenrobot/eventbus/annotationprocessor/EventBusAnnotationProcessor.java#L146
// https://github.com/amoakoagyei/excalibur/blob/f90e6875f86dd4016b5a18c83e730bb13b0d597d/annotation-processor/src/main/java/com/excalibur/annotation/model/AggregateEventGroupedClasses.java#L59

import static com.richard.eventbus.annotation.processor.JavaPoetHelpers.classOfAny;
import static javax.lang.model.element.Modifier.PUBLIC;

import com.richard.eventbus.annotation.EventListener;
import com.richard.eventbus.framework.EventHandlerClassInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
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

    public void generateEventBusIndexClass(String packageName, Types typeUtils, Messager messager, Filer filer) throws IOException {
        ClassName eventHandlerInfoClassName = ClassName.get(EventHandlerClassInfo.class);
        messager.printMessage(Kind.NOTE, "Package: " + packageName);
        String className = "EventBusIndexGeneratedImpl";
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(className)
                .addModifiers(PUBLIC, Modifier.FINAL)
                .addSuperinterface(EventBusIndex.class)
                .addField(generateEventsApplierMapField("eventHandlers", eventHandlerInfoClassName))
                .addStaticBlock(generateStaticInitializer(
                                typeUtils,
                                eventHandlerInfoClassName,
                                itemsMap
                        )
                )
                .addMethod(MethodSpec.methodBuilder("getEventHandlerClass")
                        .addModifiers(PUBLIC)
                        .addAnnotation(Override.class)
                        .addParameter(classOfAny(), "eventClass")
                        .returns(ParameterizedTypeName.get(
                                ClassName.get(Optional.class),
                                eventHandlerInfoClassName
                        ))
                        .addStatement("\treturn null")
                        .build())
                .addMethod(MethodSpec.methodBuilder("getEventHandlers")
                        .addModifiers(PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(JavaPoetHelpers.mapOfClassEventHandlerInfo())
                        .addStatement("\treturn null")
                        .build())
                .addMethod(MethodSpec.methodBuilder("put")
                        .addModifiers(PUBLIC)
                        .addParameter(
                                eventHandlerInfoClassName,
                                "eventClassInfo"
                        )
                        .addAnnotation(Override.class)
                        .addStatement(
                                "$T.requireNonNull($L, $S)",
                                ClassName.get(Objects.class),
                                "eventClassInfo",
                                "Event class info cannot be null"
                        )
                        .build());

        JavaFile javaFile = JavaFile.builder(packageName, typeSpecBuilder.build())
                .build();
        javaFile.writeTo(filer);
    }

    private static FieldSpec generateEventsApplierMapField(String fieldName, ClassName eventHandlerInfoClassName) {
        return FieldSpec.builder(
                        ParameterizedTypeName.get(
                                ClassName.get(Map.class),
                                classOfAny(),
                                eventHandlerInfoClassName
                        ),
                        fieldName
                )
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//                .initializer(
//
//                )
                .build();
    }

    private CodeBlock generateStaticInitializer(Types typeUtils,
                                                ClassName eventHandlerInfoClassName,
                                                Map<String, EventListenerAnnotatedClass> handlerInfos) {
        CodeBlock.Builder entryBuilder = CodeBlock.builder()
                .add("$L = new $T(", "eventHandlers", ConcurrentHashMap.class)
                .add("\n\t$T.ofEntries(", Map.class);
        String baseFormat = "\n\t\t$T.entry($T.class, new $T($T.class, $T.class, $S))";
        List<String> keys = new ArrayList<>(itemsMap.keySet());
        for (int index = 0; index < keys.size(); index++) {
            String handlerKey = keys.get(index);
            var eventHandlerInfo = itemsMap.get(handlerKey);
            var eventElement = eventHandlerInfo.eventTypeElement();
            TypeMirror eventElementTypeMirror = eventElement.asType();
            if (eventElementTypeMirror != null) {
                ClassName listenerClass = ClassName.get(eventHandlerInfo.listenerElement());
                ClassName eventClassName = ClassName.get(eventElement);
                if (index != keys.size() - 1) {
                    entryBuilder.add(baseFormat + ",",
                            Map.class,
                            eventClassName,
                            eventHandlerInfoClassName,
                            eventClassName,
                            listenerClass,
                            "on"
                    );
                } else {
                    entryBuilder.add(baseFormat,
                            Map.class,
                            eventClassName,
                            EventHandlerClassInfo.class,
                            eventClassName,
                            eventClassName,
                            "on"
                    );
                }
            }
        }
        entryBuilder
                .add("\n\t)\n);\n");
        return entryBuilder.build();
    }

//    https://github.com/amoakoagyei/excalibur/blob/main/annotation-processor/src/main/java/com/excalibur/annotation/model/EventSourcingApplierGroupedClasses.java
    // https://github.com/amoakoagyei/excalibur/blob/main/annotation-processor/src/main/java/com/excalibur/annotation/model/CommandHandlerGroupedClasses.java
}
