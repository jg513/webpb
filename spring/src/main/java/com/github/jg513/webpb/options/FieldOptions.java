package com.github.jg513.webpb.options;

import lombok.Getter;

import java.util.List;

@Getter
public class FieldOptions {
    private boolean omitted;

    private List<String> javaAnnotations;

    public static class Builder {
        private boolean omitted;

        private List<String> javaAnnotations;

        public Builder omitted(boolean omitted) {
            this.omitted = omitted;
            return this;
        }

        public Builder javaAnnotations(List<String> javaAnnotations) {
            this.javaAnnotations = javaAnnotations;
            return this;
        }

        public FieldOptions build() {
            FieldOptions v = new FieldOptions();
            v.omitted = this.omitted;
            v.javaAnnotations = this.javaAnnotations;
            return v;
        }
    }
}
