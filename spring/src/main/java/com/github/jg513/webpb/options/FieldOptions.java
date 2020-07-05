package com.github.jg513.webpb.options;

import java.util.List;

public class FieldOptions {

    private boolean omitted;

    private List<String> javaAnnotations;

    private boolean tsString;

    public boolean isOmitted() {
        return omitted;
    }

    public List<String> getJavaAnnotations() {
        return javaAnnotations;
    }

    public boolean isTsString() {
        return tsString;
    }

    public static class Builder {

        private boolean omitted;

        private List<String> javaAnnotations;

        private boolean tsString;

        public Builder omitted(boolean omitted) {
            this.omitted = omitted;
            return this;
        }

        public Builder javaAnnotations(List<String> javaAnnotations) {
            this.javaAnnotations = javaAnnotations;
            return this;
        }

        public Builder tsString(boolean tsString) {
            this.tsString = tsString;
            return this;
        }

        public FieldOptions build() {
            FieldOptions v = new FieldOptions();
            v.omitted = this.omitted;
            v.javaAnnotations = this.javaAnnotations;
            v.tsString = this.tsString;
            return v;
        }
    }
}
