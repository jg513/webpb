package com.github.jg513.webpb.reactive;

import com.github.jg513.webpb.core.WebpbUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

public class WebpbReactiveControllerMapping extends RequestMappingHandlerMapping {

    @NotNull
    @Override
    protected RequestMappingInfo getMappingForMethod(@NotNull Method method,
                                                     @NotNull Class<?> handlerType) {
        WebpbUtils.updateAnnotation(method);
        return super.getMappingForMethod(method, handlerType);
    }
}
