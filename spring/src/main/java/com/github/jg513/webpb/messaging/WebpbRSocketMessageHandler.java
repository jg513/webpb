package com.github.jg513.webpb.messaging;

import com.github.jg513.webpb.core.WebpbUtils;
import com.github.jg513.webpb.options.MessageOptions;
import com.squareup.wire.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.messaging.handler.CompositeMessageCondition;
import org.springframework.messaging.handler.DestinationPatternsMessageCondition;
import org.springframework.messaging.rsocket.annotation.support.RSocketFrameTypeMessageCondition;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Objects;

public class WebpbRSocketMessageHandler extends RSocketMessageHandler {
    @NotNull
    @Override
    protected CompositeMessageCondition getCondition(@NotNull AnnotatedElement element) {
        WebpbMessagingMapping annotation =
            AnnotatedElementUtils.findMergedAnnotation(element, WebpbMessagingMapping.class);
        if (annotation != null) {
            Class<?> clazz = null;
            if (annotation.message() != Message.class) {
                clazz = annotation.message();
            } else {
                for (Class<?> parameterType : ((Method) element).getParameterTypes()) {
                    if (Message.class.isAssignableFrom(parameterType)) {
                        clazz = parameterType;
                    }
                }
            }
            if (clazz == null) {
                throw new FatalBeanException(WebpbMessagingMapping.class.getSimpleName()
                    + " message class not specified");
            }
            MessageOptions options = Objects.requireNonNull(WebpbUtils.readMessageOptions(clazz),
                WebpbMessagingMapping.class.getSimpleName() + " 'MESSAGE_OPTIONS' is required");
            if (options.getPath() == null || options.getPath().isEmpty()) {
                throw new NullPointerException("'path' is required");
            }
            return new CompositeMessageCondition(
                RSocketFrameTypeMessageCondition.EMPTY_CONDITION,
                new DestinationPatternsMessageCondition(
                    processDestinations(new String[] { options.getPath() }), obtainRouteMatcher()
                ));
        }
        return super.getCondition(element);
    }
}
