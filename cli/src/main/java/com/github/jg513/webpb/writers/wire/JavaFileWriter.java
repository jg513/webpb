package com.github.jg513.webpb.writers.wire;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.jg513.webpb.core.context.SchemaContext;
import com.github.jg513.webpb.core.specs.PendingTypeSpec;
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
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
public class JavaFileWriter implements Callable<Unit> {
    private final SchemaContext context;

    private final String destination;

    private final JavaGenerator javaGenerator;

    private final ConcurrentLinkedQueue<PendingTypeSpec> queue;

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
            PendingTypeSpec spec = queue.poll();
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
                CompilationUnit unit = this.context.fileContext(spec.getProtoFile())
                    .flatMap(fileContext -> fileContext.typeContext(type))
                    .flatMap(typeContext -> Optional.of(
                        javaParserFilter.filter(typeContext, javaFile.toString())
                    ))
                    .get();
                if (unit.getPackageDeclaration().isPresent()) {
                    path = createDirectories(path, unit.getPackageDeclaration().get());
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

    private Path createDirectories(Path path, PackageDeclaration declaration) throws IOException {
        String packageName = declaration.getName().asString();
        if (!packageName.isEmpty()) {
            for (String packageComponent : packageName.split("\\.")) {
                path = path.resolve(packageComponent);
            }
            Files.createDirectories(path);
        }
        return path;
    }
}
