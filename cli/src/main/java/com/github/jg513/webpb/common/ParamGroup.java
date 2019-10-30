package com.github.jg513.webpb.common;

import com.github.jg513.webpb.exception.ConsoleErrorException;
import com.squareup.wire.schema.Field;
import com.squareup.wire.schema.MessageType;
import com.squareup.wire.schema.Schema;
import com.squareup.wire.schema.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class ParamGroup {
    @Getter
    @AllArgsConstructor
    public static class Param {
        private String prefix;

        private String key;

        private String accessor;
    }

    public static final String QUERY_KEY = "key";

    public static final String QUERY_ACCESSOR = "accessor";

    public static final String QUERY_PATTERN = "((?<key>\\w+)=)?\\{(?<accessor>[\\w.]+)}&?";

    private List<Param> params = new ArrayList<>();

    private String suffix = "";

    public static ParamGroup of(String path) {
        ParamGroup group = new ParamGroup();
        Pattern pattern = Pattern.compile(QUERY_PATTERN);
        Matcher matcher = pattern.matcher(path);

        int index = 0;
        while (matcher.find()) {
            Param param = new Param(
                path.substring(index, matcher.start()),
                matcher.group(QUERY_KEY),
                matcher.group(QUERY_ACCESSOR)
            );
            group.params.add(param);
            index = matcher.end();
        }
        group.suffix = path.substring(index);
        return group;
    }

    public ParamGroup validation(Schema schema, MessageType type) {
        for (Param param : params) {
            if (!validate(param.accessor, schema, type)) {
                throw new ConsoleErrorException("Invalid accessor %s", param.accessor);
            }
        }
        return this;
    }

    private boolean validate(String accessor, Schema schema, Type type) {
        for (String name : accessor.split("\\.")) {
            if (!(type instanceof MessageType)) {
                return false;
            }
            Field field = ((MessageType) type).field(name);
            if (field == null) {
                return false;
            }
            type = schema.getType(field.type());
        }
        return true;
    }
}
