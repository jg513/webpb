package com.github.jg513.webpb;

import org.springframework.beans.FatalBeanException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class WebpbRequestControllerMapping extends RequestMappingHandlerMapping {
    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
        if (method.isAnnotationPresent(WebpbMapping.class)) {
            info = updateMappingInfo(info, method);
        }
        return info;
    }

    private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        RequestCondition<?> condition = (element instanceof Class ?
            getCustomTypeCondition((Class<?>) element) : getCustomMethodCondition((Method) element));
        return (requestMapping != null ? createRequestMappingInfo(requestMapping, condition) : null);
    }

    private RequestMappingInfo updateMappingInfo(RequestMappingInfo info, Method method) {
        Class<?> clazz = null;
        WebpbMapping webpbMapping = method.getAnnotation(WebpbMapping.class);
        if (!webpbMapping.request().isInterface()) {
            clazz = webpbMapping.request();
        }
        if (clazz == null) {
            for (Class<?> parameterType : method.getParameterTypes()) {
                if (WebpbMessage.class.isAssignableFrom(parameterType)) {
                    clazz = parameterType;
                }
            }
        }
        if (clazz == null) {
            throw new FatalBeanException("`WebpbMapping` should have `request` class or `WebpbMessage` in method parameters.");
        }
        String methodName = readValue(clazz, "METHOD");
        String path = readValue(clazz, "PATH");
        RequestMethod httpMethod = RequestMethod.valueOf(methodName.toUpperCase());
        return new RequestMappingInfo(
            info.getName(),
            new PatternsRequestCondition(path),
            new RequestMethodsRequestCondition(httpMethod),
            info.getParamsCondition(),
            info.getHeadersCondition(),
            info.getConsumesCondition(),
            info.getProducesCondition(),
            info.getCustomCondition()
        );
    }

    private String readValue(Class<?> type, String fieldName) {
        try {
            Field field = type.getDeclaredField(fieldName);
            return (String) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new FatalBeanException("Object implements `WebpbMessage` should have static `String` 'METHOD' and 'PATH' fields.");
        }
    }
}
