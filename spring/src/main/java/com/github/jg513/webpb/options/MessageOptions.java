package com.github.jg513.webpb.options;

import java.util.Arrays;
import java.util.List;

public class MessageOptions {

    private String method;

    private String path;

    private List<String> tags;

    private List<String> javaAnnotations;

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getJavaAnnotations() {
        return javaAnnotations;
    }

    public static class Builder {

        private String method;

        private String path;

        private List<String> tags;

        private List<String> javaAnnotations;

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder tags(String... tags) {
            this.tags = Arrays.asList(tags);
            return this;
        }

        public Builder javaAnnotations(String... javaAnnotations) {
            this.javaAnnotations = Arrays.asList(javaAnnotations);
            return this;
        }

        public MessageOptions build() {
            MessageOptions v = new MessageOptions();
            v.method = this.method;
            v.path = this.path;
            v.tags = this.tags;
            v.javaAnnotations = this.javaAnnotations;
            return v;
        }
    }
}
