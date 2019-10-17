package com.github.jg513.webpb.writers.java;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.jg513.webpb.common.CodeWriter;
import com.github.jg513.webpb.common.CodeWriterContext;
import com.github.jg513.webpb.common.PendingSpec;
import com.github.jg513.webpb.common.specs.PendingTypeSpec;
import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JavaWriter extends CodeWriter {
    public JavaWriter(CodeWriterContext options) {
        super(options);
    }

    @Override
    public Void call() throws Exception {
        JavaOptions options = new JavaOptions(context.getSchema());
        JavaGenerator generator = JavaGenerator.create(context.getSchema(), options);
        while (true) {
            PendingSpec spec = context.getSpecs().poll();
            if (spec == null) {
                return null;
            }
            if (!(spec instanceof PendingTypeSpec)) {
                continue;
            }
            ProtoFile file = ((PendingTypeSpec) spec).getFile();
            Type type = ((PendingTypeSpec) spec).getType();

            CompilationUnit unit = generator.generate(file, type);
            Path path = Paths.get(context.getOut());
            if (unit.getPackageDeclaration().isPresent()) {
                PackageDeclaration declaration = unit.getPackageDeclaration().get();
                String packageName = declaration.getName().asString();
                if (!packageName.isEmpty()) {
                    for (String packageComponent : packageName.split("\\.")) {
                        path = path.resolve(packageComponent);
                    }
                    try {
                        Files.createDirectories(path);
                    } catch (Exception e) {
                        context.getLog().error("Cannot create directory %s", path);
                    }
                }
            }
            path = path.resolve(type.type().simpleName() + ".java");
            try {
                Files.write(path, unit.toString().getBytes());
            } catch (IOException e) {
                throw new IOException("Error emitting " + spec.toString() + context.getOut(), e);
            }
        }
    }
}
