package com.github.jg513.webpb.options;

import lombok.Getter;

import java.util.List;

@Getter
public class FileOptions {
    private List<String> javaImports;

    private List<String> javaMessageAnnotations;

    private boolean javaSetter;

    private boolean javaGetter;

    public static class Builder {
        private List<String> javaImports;

        private List<String> javaMessageAnnotations;

        private boolean javaSetter;

        private boolean javaGetter;

        public Builder javaImports(List<String> javaImports) {
            this.javaImports = javaImports;
            return this;
        }

        public Builder javaMessageAnnotations(List<String> javaMessageAnnotations) {
            this.javaMessageAnnotations = javaMessageAnnotations;
            return this;
        }

        public Builder javaSetter(boolean javaSetter) {
            this.javaSetter = javaSetter;
            return this;
        }

        public Builder javaGetter(boolean javaGetter) {
            this.javaGetter = javaGetter;
            return this;
        }

        public FileOptions build() {
            FileOptions v = new FileOptions();
            v.javaImports = this.javaImports;
            v.javaMessageAnnotations = this.javaMessageAnnotations;
            v.javaSetter = this.javaSetter;
            v.javaGetter = this.javaGetter;
            return v;
        }
    }
}
