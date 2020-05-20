package com.github.jg513.webpb.messaging;

import com.squareup.wire.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@MessageMapping
public @interface WebpbMessagingMapping {
    Class<? extends Message> message() default Message.class;
}
