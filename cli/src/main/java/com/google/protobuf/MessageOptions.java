package com.google.protobuf;

import lombok.Getter;

@Getter
public class MessageOptions {
    private boolean omitted;

    private String method;

    private String path;

    private String javaAnnotation;

    public static class Builder {
        private boolean omitted;

        private String method;

        private String path;

        private String javaAnnotation;

        public Builder omitted(boolean omitted) {
            this.omitted = omitted;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder java_anno(String javaAnnotation) {
            this.javaAnnotation = javaAnnotation;
            return this;
        }

        public MessageOptions build() {
            MessageOptions v = new MessageOptions();
            v.omitted = this.omitted;
            v.method = this.method;
            v.path = this.path;
            v.javaAnnotation = this.javaAnnotation;
            return v;
        }
    }
}
