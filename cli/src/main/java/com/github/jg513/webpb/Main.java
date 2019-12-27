package com.github.jg513.webpb;

import com.github.jg513.webpb.writers.wire.WireArgs;
import com.github.jg513.webpb.writers.wire.WireCompiler;
import com.squareup.javapoet.JavaFile;
import com.squareup.kotlinpoet.FileSpec;
import com.squareup.wire.WireLogger;
import com.squareup.wire.schema.ProtoType;
import com.squareup.wire.schema.PruningRules;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

@Command(name = "webpb", mixinStandardHelpOptions = true, version = "Picocli example 4.0")
public class Main implements Runnable {
    @Option(names = "--proto_path", arity = "1..*", description = "Paths to resolving proto files.", required = true)
    private String[] protoPaths;

    @Option(names = "--type", arity = "1", description = "TS, JAVA", required = true)
    private String type;

    @Option(names = "--out", arity = "1", description = "Generated code output directory.", required = true)
    private String out;

    @Option(names = "--quiet", arity = "1", description = "Hide logs.")
    private boolean quiet;

    @Option(names = "--includes", arity = "1..*", description = "Included paths.")
    private String[] includes;

    @Option(names = "--excludes", arity = "1..*", description = "Excluded paths.")
    private String[] excludes;

    @Option(names = "--files", arity = "1..*", description = "Source files.")
    private String[] files;

    @Option(names = "--tags", arity = "0..*", description = "Generation tags.")
    private String[] tags;

    private static CommandLine commandLine;

    public void run() {
        WireArgs wireArgs = new WireArgs();
        wireArgs
            .setFs(Paths.get("").getFileSystem())
            .setLog(new WireLogger() {
                @Override
                public void setQuiet(boolean b) {
                }

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
            })
            .setProtoPaths(Collections.singletonList("../example/proto"))
            .setJavaOut("../example/out")
//            .setJavaOut("src/main/java")
            .setSourceFileNames(Collections.singletonList("Store.proto"))
            .setPruningRules(new PruningRules.Builder()
                .build()
            );
        WireCompiler compiler = new WireCompiler(
            wireArgs.getFs(),
            wireArgs.getLog(),
            wireArgs.getProtoPaths(),
            wireArgs.getJavaOut(),
            wireArgs.getKotlinOut(),
            wireArgs.getSourceFileNames(),
            wireArgs.getPruningRules(),
            wireArgs.isDryRun(),
            wireArgs.isNamedFilesOnly(),
            wireArgs.isEmitAndroid(),
            wireArgs.isEmitAndroidAnnotations(),
            wireArgs.isEmitCompact(),
            wireArgs.isJavaInterop()
        );
        try {
            compiler.compile();
        } catch (Exception e) {
            commandLine.getErr().printf(e.getMessage());
        }
    }

    public static void main(String[] args) {
        commandLine = new CommandLine(new Main());
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
