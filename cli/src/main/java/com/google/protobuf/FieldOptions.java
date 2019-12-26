package com.google.protobuf;

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

        public Builder java_anno(List<String> java_anno) {
            this.javaAnnotations = java_anno;
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
