package com.github.jg513.webpb.core;

import com.github.jg513.webpb.core.specs.PendingFileSpec;
import com.github.jg513.webpb.core.specs.PendingServiceSpec;
import com.github.jg513.webpb.core.specs.PendingSpec;
import com.github.jg513.webpb.core.specs.PendingTypeSpec;
import com.github.jg513.webpb.log.Logger;
import com.github.jg513.webpb.writers.java.JavaWriter;
import com.github.jg513.webpb.writers.typescript.TypescriptWriter;
import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.PruningRules;
import com.squareup.wire.schema.Schema;
import com.squareup.wire.schema.SchemaLoader;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class WebpbCompiler {
    private static final int MAX_WRITE_CONCURRENCY = 8;

    private static final String DESCRIPTOR_PROTO = "google/protobuf/descriptor.proto";

    private static Map<String, Class<? extends CodeWriter>> writerMap =
        new HashMap<String, Class<? extends CodeWriter>>() {{
            put("JAVA", JavaWriter.class);
            put("TS", TypescriptWriter.class);
        }};

    private final Logger log;

    private final String[] protoPaths;

    private final String[] files;

    private final String[] tags;

    private final String type;

    private final String out;

    @NotNull
    private final PruningRules pruningRules;

    public void compile() throws IOException {
        String typeUppercase = type.toUpperCase();
        if (!writerMap.containsKey(typeUppercase)) {
            log.error("Code type %s is not supported.", type);
            return;
        }
        Schema schema = createSchema();

        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<Void>> futures = new ArrayList<>(MAX_WRITE_CONCURRENCY);
        Path path = Paths.get(out);
        if (Files.exists(path) && !Files.isDirectory(path)) {
            throw new IllegalArgumentException(String.format("Path %s exists but is not a directory.", path));
        }
        File dir = path.toFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalArgumentException(String.format("Failed create %s directory.", path));
        }
        CodeWriterContext context = CodeWriterContext.builder()
            .log(log)
            .out(out)
            .schema(schema)
            .specs(createSpecs(schema))
            .tags(this.tags == null ? Collections.emptyList() : Arrays.asList(this.tags))
            .build();
        for (int i = 0; i < MAX_WRITE_CONCURRENCY; i++) {
            try {
                Class<? extends CodeWriter> clazz = writerMap.get(typeUppercase);
                Callable<Void> writer = clazz.getConstructor(CodeWriterContext.class).newInstance(context);
                futures.add(executor.submit(writer));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        executor.shutdown();

        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (ExecutionException e) {
            throw new IOException(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Schema createSchema() throws IOException {
        SchemaLoader loader = new SchemaLoader();
        for (String path : protoPaths) {
            loader.addSource(Paths.get(path));
        }
        if (files == null) {
            List<Path> directories = Arrays.stream(protoPaths)
                .map(Paths::get)
                .collect(Collectors.toList());
            List<String> names = resolveFiles(directories);
            for (String filename : names) {
                loader.addProto(filename);
            }
        } else {
            for (String filename : files) {
                loader.addProto(filename);
            }
        }
        Schema schema = loader.load();
        if (!pruningRules.isEmpty()) {
            log.info("Analyzing dependencies of root types.");
            schema = schema.prune(pruningRules);
            pruningRules.unusedIncludes().forEach(v -> log.info("Unused include: " + v));
            pruningRules.unusedExcludes().forEach(v -> log.info("Unused exclude: " + v));
        }
        return schema;
    }

    private List<String> resolveFiles(List<Path> directories) throws IOException {
        List<String> files = new ArrayList<>();
        for (Path directory : directories) {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.getFileName().toString().equals("WebpbExtend.proto")) {
                        return FileVisitResult.CONTINUE;
                    }
                    if (file.getFileName().toString().endsWith(".proto")) {
                        files.add(directory.relativize(file).toString());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        return files;
    }

    private AbstractQueue<PendingSpec> createSpecs(Schema schema) {
        AbstractQueue<PendingSpec> specs = new ConcurrentLinkedQueue<>();
        for (ProtoFile file : schema.getProtoFiles()) {
            if (file.getLocation().getPath().equals(DESCRIPTOR_PROTO)) {
                continue;
            }
            specs.add(new PendingFileSpec(file));
            specs.addAll(file.getTypes().stream()
                .map(t -> new PendingTypeSpec(file, t))
                .collect(Collectors.toList())
            );
            specs.addAll(file.getServices().stream()
                .map(s -> new PendingServiceSpec(file, s))
                .collect(Collectors.toList())
            );
        }
        return specs;
    }
}
