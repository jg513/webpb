package com.github.jg513.webpb.mvc;

import com.github.jg513.webpb.core.WebpbUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

public class WebpbRequestControllerMapping extends RequestMappingHandlerMapping {

    @Nullable
    @Override
    protected RequestMappingInfo getMappingForMethod(@NotNull Method method,
                                                     @NotNull Class<?> handlerType) {
        WebpbUtils.updateAnnotation(method);
        return super.getMappingForMethod(method, handlerType);
    }
}
