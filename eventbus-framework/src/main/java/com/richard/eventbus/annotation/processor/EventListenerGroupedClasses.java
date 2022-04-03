package com.richard.eventbus.annotation.processor;

// https://github.com/greenrobot/EventBus/blob/842a4a312c4ea1592bd7067a00495eebe129c078/EventBusAnnotationProcessor/src/org/greenrobot/eventbus/annotationprocessor/EventBusAnnotationProcessor.java#L146

import static com.richard.eventbus.annotation.processor.JavaPoetHelpers.classOfAny;
import static javax.lang.model.element.Modifier.PUBLIC;

import com.richard.eventbus.annotation.EventListener;
import com.richard.eventbus.framework.EventBusIndex;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private static final String EVENT_HANDLERS_FIELD_NAME = "eventHandlers";
    private static final ClassName EVENT_HANDLER_INFO_CLASS_NAME = ClassName.get(EventHandlerClassInfo.class);
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
    public void writeIndexFile(String indexFilePath, Filer filer) throws IOException {
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
        IndexFileUtil.writeSimpleNameIndexFile(filer, entries, metaINFPath);
    }

    public void generateEventBusIndexClass(String packageName, Filer filer) throws IOException {
        String className = "EventBusIndexGeneratedImpl";
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(className)
            .addModifiers(PUBLIC, Modifier.FINAL)
            .addSuperinterface(EventBusIndex.class)
            .addField(generateEventsApplierMapField())
            .addStaticBlock(generateStaticInitializer()
            )
            .addMethod(MethodSpec.methodBuilder("getEventHandlerClass")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(classOfAny(), "eventClass")
                .returns(ParameterizedTypeName.get(
                    ClassName.get(Collection.class),
                    EVENT_HANDLER_INFO_CLASS_NAME
                ))
                .addStatement("\treturn $L.getOrDefault(eventClass, $T.of())", EVENT_HANDLERS_FIELD_NAME, Set.class)
                .build())
            .addMethod(MethodSpec.methodBuilder("getAllEventHandlerClassInfos")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(ParameterizedTypeName.get(
                    ClassName.get(Collection.class),
                    EVENT_HANDLER_INFO_CLASS_NAME
                ))
                .addStatement(
                    "\treturn $L.values().stream().flatMap($T::stream).collect($T.toSet())",
                    EVENT_HANDLERS_FIELD_NAME, Collection.class, Collectors.class)
                .build())
            .addMethod(generatePutMethod());

        JavaFile javaFile = JavaFile.builder(packageName, typeSpecBuilder.build())
            .build();
        javaFile.writeTo(filer);
    }

    //  public static Map<Class<?>, Set<EventHandlerClassInfo>> eventHandlers;
    private static FieldSpec generateEventsApplierMapField() {
        return FieldSpec.builder(
                ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    classOfAny(),
                    ParameterizedTypeName.get(
                        ClassName.get(Set.class),
                        EVENT_HANDLER_INFO_CLASS_NAME
                    )
                ),
                EVENT_HANDLERS_FIELD_NAME
            )
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .build();
    }

    private CodeBlock generateStaticInitializer() {
        CodeBlock.Builder entryBuilder = CodeBlock.builder()
            .add("$L = new $T<>(", EVENT_HANDLERS_FIELD_NAME, ConcurrentHashMap.class)
            .add("\n\t$T.ofEntries(", Map.class);
        Map<String, List<EventListenerAnnotatedClass>> itemGroupByEvent = itemsMap.values()
            .stream()
            .collect(Collectors.groupingBy(it -> it.eventTypeElement().getQualifiedName().toString()));

        List<String> keys = new ArrayList<>(itemGroupByEvent.keySet());

        for (int index = 0; index < keys.size(); index++) {
            String handlerKey = keys.get(index);
            List<EventListenerAnnotatedClass> eventListenerClasses = itemGroupByEvent.get(handlerKey);
            if (eventListenerClasses != null && eventListenerClasses.size() > 0) {
                TypeElement eventTypeElement = eventListenerClasses.get(0).eventTypeElement();
                TypeMirror typeMirror = eventTypeElement.asType();
                if (typeMirror != null) {
                    if (index != keys.size() - 1) {
                        entryBuilder.add(CodeBlock.builder()
                            .add("\n\t\t$T.entry($T.class, $T.of(\n",
                                Map.class, eventTypeElement, Set.class)
                            .add(buildListOfEventHandlerInfos(eventListenerClasses))
                            .add("\n\t\t)\n\t),")
                            .build());
                    } else {
                        entryBuilder.add(CodeBlock.builder()
                            .add("\n\t\t$T.entry($T.class, $T.of(\n",
                                Map.class, eventTypeElement, Set.class)
                            .add(buildListOfEventHandlerInfos(eventListenerClasses))
                            .add("\n\t\t)\n\t)")
                            .build());
                    }
                }
            }
        }
        entryBuilder
            .add("\n\t)\n);\n");
        return entryBuilder.build();
    }

    private CodeBlock buildListOfEventHandlerInfos(List<EventListenerAnnotatedClass> eventListenerClasses) {
        String format = "\t\t\tnew $T($T.class, $T.class, $S)";
        var codeBlockBuilder = CodeBlock.builder();
        for (int index = 0; index < eventListenerClasses.size(); index++) {
            var eventListenerAnnotatedClass = eventListenerClasses.get(index);
            String codeFormat = format;
            if (index != eventListenerClasses.size() - 1) {
                codeFormat = codeFormat + ",\n";
            }
            codeBlockBuilder.add(
                codeFormat,
                EventHandlerClassInfo.class,
                eventListenerAnnotatedClass.eventTypeElement(),
                eventListenerAnnotatedClass.listenerElement(),
                eventListenerAnnotatedClass.methodName()
            );
        }
        return codeBlockBuilder.build();
    }

    private MethodSpec generatePutMethod() {
        String eventClassInfo = "eventClassInfo";
        String eventHandlerClassInfos = "eventHandlerClassInfos";


        return MethodSpec.methodBuilder("put")
            .addModifiers(PUBLIC)
            .addParameter(
                EVENT_HANDLER_INFO_CLASS_NAME,
                eventClassInfo
            )
            .addAnnotation(Override.class)
            .addCode(CodeBlock.builder()
                .addStatement(
                    "$T.requireNonNull($L, $S)",
                    ClassName.get(Objects.class),
                    eventClassInfo,
                    "Event class info cannot be null"
                )
                .addStatement("var $L = $L.get($L.eventClass())", eventHandlerClassInfos, EVENT_HANDLERS_FIELD_NAME, eventClassInfo)
                .beginControlFlow("if ($L == null)", eventHandlerClassInfos)
                .addStatement("$L = $T.of($L)", eventHandlerClassInfos, Set.class, eventClassInfo)
                .nextControlFlow("else")
                .addStatement("$L = new $T<>($L)", eventHandlerClassInfos, HashSet.class, eventHandlerClassInfos)
                .addStatement("$L.remove($L)", eventHandlerClassInfos, eventClassInfo)
                .addStatement("$L.add($L)", eventHandlerClassInfos, eventClassInfo)
                .endControlFlow()
                .addStatement("$L.put($L.eventClass(), $L)", EVENT_HANDLERS_FIELD_NAME, eventClassInfo, eventHandlerClassInfos)
                .build())
            .build();
    }
}
