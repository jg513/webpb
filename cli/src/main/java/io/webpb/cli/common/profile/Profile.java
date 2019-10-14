package io.webpb.cli.common.profile;

import com.google.common.collect.ImmutableList;
import com.squareup.wire.schema.ProtoType;

import javax.annotation.Nullable;

public final class Profile {
    private final ImmutableList<ProfileFileElement> profileFiles;

    public Profile(ImmutableList<ProfileFileElement> profileFiles) {
        this.profileFiles = profileFiles;
    }

    public Profile() {
        this(ImmutableList.of());
    }

    @Nullable
    public String getTargetName(ProtoType type) {
        TypeConfigElement typeConfig = typeConfig(type);
        return typeConfig != null ? typeConfig.getTarget() : null;
    }

    @Nullable
    private TypeConfigElement typeConfig(ProtoType type) {
        for (ProfileFileElement element : profileFiles) {
            for (TypeConfigElement typeConfig : element.getTypeConfigs()) {
                if (typeConfig.getType().equals(type.toString())) return typeConfig;
            }
        }
        return null;
    }
}
