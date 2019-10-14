package io.webpb.cli.common;

import com.squareup.wire.schema.IdentifierSet;
import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.Schema;
import com.squareup.wire.schema.SchemaLoader;
import io.webpb.cli.common.specs.PendingFileSpec;
import io.webpb.cli.common.specs.PendingServiceSpec;
import io.webpb.cli.common.specs.PendingTypeSpec;
import io.webpb.cli.log.Logger;
import io.webpb.cli.writers.java.JavaWriter;
import io.webpb.cli.writers.typescript.TypescriptWriter;
import lombok.RequiredArgsConstructor;

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

    private final String type;

    private final String out;

    private final IdentifierSet identifierSet;

    public void compile() throws IOException {
        String typeUppercase = type.toUpperCase();
        if (!writerMap.containsKey(typeUppercase)) {
            log.error("Code type %s is not supported.", type);
            return;
        }
        Schema schema = createSchema();

        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<Void>> futures = new ArrayList<>(MAX_WRITE_CONCURRENCY);
        CodeWriterContext context = CodeWriterContext.builder()
            .log(log)
            .out(out)
            .schema(schema)
            .specs(createSpecs(schema))
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
            for (Future future : futures) {
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
        if (!identifierSet.isEmpty()) {
            log.info("Analyzing dependencies of root types.");
            schema = schema.prune(identifierSet);
            for (String rule : identifierSet.unusedIncludes()) {
                log.info("Unused include: %s", rule);
            }
            for (String rule : identifierSet.unusedExcludes()) {
                log.info("Unused exclude: %s", rule);
            }
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
        for (ProtoFile file : schema.protoFiles()) {
            if (file.location().getPath().equals(DESCRIPTOR_PROTO)) {
                continue;
            }
            specs.add(new PendingFileSpec(file));
            specs.addAll(file.types().stream()
                .map(PendingTypeSpec::new)
                .collect(Collectors.toList())
            );
            specs.addAll(file.services().stream()
                .map(PendingServiceSpec::new)
                .collect(Collectors.toList())
            );
        }
        return specs;
    }
}
