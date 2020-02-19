package com.github.jg513.webpb.writers.wire;

import com.squareup.javapoet.JavaFile;
import com.squareup.kotlinpoet.FileSpec;
import com.squareup.wire.WireLogger;
import com.squareup.wire.schema.ProtoType;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class WireLoggerImpl implements WireLogger {

    @Override
    public void artifact(@NotNull Path path, @NotNull JavaFile javaFile) {
    }

    @Override
    public void artifact(@NotNull Path path, @NotNull FileSpec fileSpec) {
    }

    @Override
    public void artifactSkipped(@NotNull ProtoType protoType) {
    }

    @Override
    public void info(@NotNull String s) {
    }

    @Override
    public void setQuiet(boolean b) {
    }
}
