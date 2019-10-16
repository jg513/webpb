package com.github.jg513.webpb.writers.java;

import com.github.jg513.webpb.common.Const;
import com.github.jg513.webpb.common.specs.PendingTypeSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.squareup.wire.schema.Type;
import com.github.jg513.webpb.common.CodeWriter;
import com.github.jg513.webpb.common.CodeWriterContext;
import com.github.jg513.webpb.common.PendingSpec;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JavaWriter extends CodeWriter {
    public JavaWriter(CodeWriterContext options) {
        super(options);
    }

    @Override
    public Void call() throws Exception {
        JavaGenerator generator = JavaGenerator.create(context.getSchema());
        while (true) {
            PendingSpec spec = context.getSpecs().poll();
            if (spec == null) {
                return null;
            }
            if (!(spec instanceof PendingTypeSpec)) {
                continue;
            }
            Type type = ((PendingTypeSpec) spec).getType();
            TypeSpec typeSpec = generator.generateType(type);
            ClassName javaTypeName = generator.generatedTypeName(type);
            JavaFile.Builder builder = JavaFile.builder(javaTypeName.packageName(), typeSpec)
                .indent("    ")
                .addFileComment("$L\n", Const.HEADER)
                .addFileComment("$L", Const.GIT_URL);

            JavaFile javaFile = builder.build();
            Path path = Paths.get(context.getOut());
            try {
                javaFile.writeTo(path);
            } catch (IOException e) {
                throw new IOException("Error emitting " + javaFile.packageName
                    + '.' + javaFile.typeSpec.name + " to " + context.getOut(), e);
            }
        }
    }
}
