package com.github.jg513.webpb;

import com.github.jg513.webpb.common.WebpbCompiler;
import com.github.jg513.webpb.log.Logger;
import com.github.jg513.webpb.log.LoggerImpl;
import com.squareup.wire.schema.IdentifierSet;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Arrays;
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

    private static CommandLine commandLine;

    public void run() {
        Logger log = LoggerImpl.of(commandLine.getOut(), commandLine.getErr(), quiet);
        IdentifierSet identifierSet = new IdentifierSet.Builder()
            .include(includes == null ? Collections.emptyList() : Arrays.asList(includes))
            .exclude(excludes == null ? Collections.emptyList() : Arrays.asList(excludes))
            .build();
        try {
            new WebpbCompiler(log, protoPaths, files, type, out, identifierSet).compile();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    public static void main(String[] args) {
        commandLine = new CommandLine(new Main());
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
