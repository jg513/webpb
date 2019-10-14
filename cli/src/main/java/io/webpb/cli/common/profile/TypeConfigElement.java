package io.webpb.cli.common.profile;

import com.squareup.wire.schema.Location;
import com.squareup.wire.schema.internal.parser.OptionElement;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class TypeConfigElement {
    private Location location;

    private String type;

    private String documentation;

    private List<OptionElement> with = Collections.emptyList();

    private String target;

    private String adapter;
}
