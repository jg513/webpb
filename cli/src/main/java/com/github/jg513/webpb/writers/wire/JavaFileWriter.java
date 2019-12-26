package com.github.jg513.webpb.writers.wire;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.jg513.webpb.writers.wire.specs.PendingTypeFileSpec;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.squareup.wire.WireLogger;
import com.squareup.wire.java.JavaGenerator;
import com.squareup.wire.schema.Type;
import kotlin.Unit;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
public class JavaFileWriter implements Callable<Unit> {
    private final String destination;

    private final JavaGenerator javaGenerator;

    private final ConcurrentLinkedQueue<PendingTypeFileSpec> queue;

    private final boolean dryRun;

    private final FileSystem fs;

    private final WireLogger log;

    private JavaParserFilter javaParserFilter = new JavaParserFilter();

    private Formatter formatter = new Formatter(JavaFormatterOptions.builder()
        .style(JavaFormatterOptions.Style.AOSP)
        .build());

    @Override
    public Unit call() throws Exception {
        while (true) {
            PendingTypeFileSpec spec = queue.poll();
            if (spec == null || spec.getType() == null) {
                return null;
            }
            Type type = spec.getType();
            TypeSpec typeSpec = javaGenerator.generateType(type);
            ClassName javaTypeName = javaGenerator.generatedTypeName(type);
            JavaFile javaFile = JavaFile.builder(javaTypeName.packageName(), typeSpec)
                .addFileComment("$L", WireCompiler.CODE_GENERATED_BY_WIRE)
                .addFileComment("\nSource file: $L", type.getLocation().withPathOnly())
                .build();

            Path path = fs.getPath(destination);
            log.artifact(path, javaFile);
            if (dryRun) {
                return null;
            }
            try {
                CompilationUnit unit = javaParserFilter.filter(javaFile.toString());
                if (unit.getPackageDeclaration().isPresent()) {
                    PackageDeclaration declaration = unit.getPackageDeclaration().get();
                    String packageName = declaration.getName().asString();
                    if (!packageName.isEmpty()) {
                        for (String packageComponent : packageName.split("\\.")) {
                            path = path.resolve(packageComponent);
                        }
                        Files.createDirectories(path);
                    }
                }
                Path out = path.resolve(typeSpec.name + ".java");
                String content = formatter.formatSource(unit.toString());
                Files.write(out, content.getBytes());
            } catch (IOException e) {
                throw new IOException(
                    String.format("Error emitting %s.%s to %s",
                        javaFile.packageName, javaFile.typeSpec.name, destination), e);
            }
        }
    }
}
