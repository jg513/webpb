package com.github.jg513.webpb.writers.wire;

import com.squareup.wire.WireLogger;
import com.squareup.wire.schema.PruningRules;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.nio.file.FileSystem;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class WireArgs {

    private FileSystem fs;

    private WireLogger log;

    private List<String> protoPaths;

    private String javaOut;

    private String kotlinOut;

    private List<String> sourceFileNames;

    private PruningRules pruningRules;

    private boolean dryRun;

    private boolean namedFilesOnly;

    private boolean emitAndroid;

    private boolean emitAndroidAnnotations;

    private boolean emitCompact;

    private boolean javaInterop;
}
