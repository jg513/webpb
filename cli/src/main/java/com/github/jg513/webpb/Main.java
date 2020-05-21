package com.github.jg513.webpb;

import com.github.jg513.webpb.core.WebpbCompiler;
import com.github.jg513.webpb.log.Logger;
import com.github.jg513.webpb.log.LoggerImpl;
import com.github.jg513.webpb.writers.wire.WireArgs;
import com.github.jg513.webpb.writers.wire.WireCompiler;
import com.github.jg513.webpb.writers.wire.WireLoggerImpl;
import com.squareup.wire.schema.PruningRules;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Command(name = "webpb", mixinStandardHelpOptions = true, version = "Picocli example 4.0")
public class Main implements Runnable {

    @Option(names = "--proto_path", arity = "1..*", description = "Paths to resolving proto files.", required = true)
    private String[] protoPaths;

    @Option(names = "--type", arity = "1", description = "TS, JAVA, WIRE", required = true)
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
        try {
            if ("WIRE".equals(type)) {
                useWireCompiler();
            } else {
                useWebpbCompiler();
            }
        } catch (Exception e) {
            e.printStackTrace();
            commandLine.getErr().printf(e.getMessage());
        }
    }

    private void useWebpbCompiler() throws Exception {
        Logger log = LoggerImpl.of(commandLine.getOut(), commandLine.getErr(), quiet);
        PruningRules pruningRules = new PruningRules.Builder()
            .addRoot(includes == null ? Collections.emptyList() : Arrays.asList(includes))
            .prune(excludes == null ? Collections.emptyList() : Arrays.asList(excludes))
            .build();
        new WebpbCompiler(log, protoPaths, files, tags, type, out, pruningRules).compile();
    }

    private void useWireCompiler() throws Exception {
        WireArgs wireArgs = new WireArgs();
        wireArgs
            .setFs(Paths.get("").getFileSystem())
            .setLog(new WireLoggerImpl())
            .setProtoPaths(Arrays.asList(protoPaths))
            .setJavaOut(out)
            .setSourceFileNames(WebpbCompiler.resolveFiles(
                Arrays.stream(protoPaths).map(v -> Paths.get(v)).collect(Collectors.toList())
            ))
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
        compiler.compile();
    }

    public static void main(String[] args) {
        commandLine = new CommandLine(new Main());
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
