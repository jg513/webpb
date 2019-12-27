package com.github.jg513.webpb.writers.java;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.jg513.webpb.core.CodeWriter;
import com.github.jg513.webpb.core.CodeWriterContext;
import com.github.jg513.webpb.core.specs.PendingSpec;
import com.github.jg513.webpb.core.Utils;
import com.github.jg513.webpb.core.options.MessageOptions;
import com.github.jg513.webpb.core.specs.PendingTypeSpec;
import com.github.jg513.webpb.exception.ConsoleErrorException;
import com.squareup.wire.schema.MessageType;
import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class JavaWriter extends CodeWriter {
    public JavaWriter(CodeWriterContext options) {
        super(options);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Void call() {
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
            ProtoFile file = ((PendingTypeSpec) spec).getProtoFile();
            Type type = ((PendingTypeSpec) spec).getType();
            if (type instanceof MessageType && !context.getTags().isEmpty()) {
                List<String> tags = (List<String>) type.getOptions().get(MessageOptions.TAG);
                if (tags != null && !Utils.containsAny(context.getTags(), tags)) {
                    continue;
                }
            }

            try {
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
                path = path.resolve(Objects.requireNonNull(type.getType()).getSimpleName() + ".java");
                String content = unit.toString()
                    .replace("// https://github.com/jg513/webpb", "// https://github.com/jg513/webpb\n")
                    .replace("switch(", "switch (");
                Files.write(path, content.getBytes());
            } catch (ConsoleErrorException e) {
                context.getLog().error(e.getMessage());
            } catch (IOException e) {
                context.getLog().error("Error emitting %s, %s, %s", spec.toString(), context.getOut(), e.getMessage());
            }
        }
    }
}
