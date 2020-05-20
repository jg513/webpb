package com.github.jg513.webpb.core;

import com.github.jg513.webpb.messaging.WebpbMessagingMapping;
import com.github.jg513.webpb.mvc.WebpbRequestMapping;
import com.github.jg513.webpb.options.MessageOptions;
import com.squareup.wire.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;

public class WebpbUtils {
    @SuppressWarnings("unchecked")
    public static void updateAnnotation(Method method) {
        if (!method.isAnnotationPresent(WebpbRequestMapping.class)) {
            return;
        }
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
        WebpbRequestMapping webpbRequestMapping = method.getAnnotation(WebpbRequestMapping.class);

        Class<?> clazz = webpbRequestMapping.message();
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            for (Class<?> parameterType : method.getParameterTypes()) {
                if (Message.class.isAssignableFrom(parameterType)) {
                    clazz = parameterType;
                }
            }
        }
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            throw new FatalBeanException(WebpbRequestMapping.class.getSimpleName()
                + " message is not specified");
        }
        MessageOptions options = Objects.requireNonNull(WebpbUtils.readMessageOptions(clazz),
            WebpbMessagingMapping.class.getSimpleName() + " 'MESSAGE_OPTIONS' is required");

        String methodName = Objects.requireNonNull(options.getMethod(),
            WebpbRequestMapping.class.getSimpleName() + " 'METHOD' is required");
        String path = Objects.requireNonNull(options.getPath(),
            WebpbRequestMapping.class.getSimpleName() + " 'PATH' is required");

        Map<Class<? extends Annotation>, Annotation> annotations;
        try {
            Field field = Executable.class.getDeclaredField("declaredAnnotations");
            field.setAccessible(true);
            annotations = (Map<Class<? extends Annotation>, Annotation>) field.get(method);
        } catch (Exception ignored) {
            throw new FatalBeanException("Read annotations error");
        }
        AnnotationUtils.clearCache();
        annotations.remove(WebpbRequestMapping.class);
        Class<? extends Annotation> annotationType = requestMapping.annotationType();
        String mappingName = requestMapping.name();
        String[] mappingValue = requestMapping.value();
        String[] mappingPath = new String[] { path.split("\\?")[0] };
        RequestMethod[] mappingMethod = new RequestMethod[] {
            RequestMethod.valueOf(methodName.toUpperCase())
        };
        String[] mappingParams = requestMapping.params();
        String[] mappingHeaders = requestMapping.headers();
        String[] mappingConsumes = requestMapping.consumes();
        String[] mappingProduces = requestMapping.produces();
        annotations.put(RequestMapping.class, new RequestMapping() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return annotationType;
            }

            @NotNull
            @Override
            public String name() {
                return mappingName;
            }

            @NotNull
            @Override
            public String[] value() {
                return mappingValue;
            }

            @NotNull
            @Override
            public String[] path() {
                return mappingPath;
            }

            @NotNull
            @Override
            public RequestMethod[] method() {
                return mappingMethod;
            }

            @NotNull
            @Override
            public String[] params() {
                return mappingParams;
            }

            @NotNull
            @Override
            public String[] headers() {
                return mappingHeaders;
            }

            @NotNull
            @Override
            public String[] consumes() {
                return mappingConsumes;
            }

            @NotNull
            @Override
            public String[] produces() {
                return mappingProduces;
            }
        });
    }

    public static MessageOptions readMessageOptions(Class<?> type) {
        try {
            Field field = type.getDeclaredField("MESSAGE_OPTIONS");
            return (MessageOptions) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }
}
