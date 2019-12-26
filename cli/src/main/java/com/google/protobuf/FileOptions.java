package com.google.protobuf;

import lombok.Getter;

@Getter
public class FileOptions {
    private String javaImport;

    private String javaMessageAnnotation;

    private boolean javaSetter;

    private boolean javaGetter;

    public static class Builder {
        private String javaImport;

        private String javaMessageAnnotation;

        private boolean javaSetter;

        private boolean javaGetter;

        public Builder java_import(String javaImport) {
            this.javaImport = javaImport;
            return this;
        }

        public Builder java_message_anno(String javaMessageAnnotation) {
            this.javaMessageAnnotation = javaMessageAnnotation;
            return this;
        }

        public Builder java_setter(boolean javaSetter) {
            this.javaSetter = javaSetter;
            return this;
        }

        public Builder java_getter(boolean javaGetter) {
            this.javaGetter = javaGetter;
            return this;
        }

        public FileOptions build() {
            FileOptions v = new FileOptions();
            v.javaImport = this.javaImport;
            v.javaMessageAnnotation = this.javaMessageAnnotation;
            v.javaSetter = this.javaSetter;
            v.javaGetter = this.javaGetter;
            return v;
        }
    }
}
