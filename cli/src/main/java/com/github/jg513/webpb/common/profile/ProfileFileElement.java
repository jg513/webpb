package com.github.jg513.webpb.common.profile;

import com.squareup.wire.schema.Location;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class ProfileFileElement {
    private Location location;

    private String packageName;

    private List<String> imports = Collections.emptyList();

    private List<TypeConfigElement> typeConfigs = Collections.emptyList();
}
