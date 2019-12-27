package com.github.jg513.webpb.core.context;

import com.github.javaparser.ast.expr.Name;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class QualifiedNames {
    private Map<String, Name> qualifiedNames = new ConcurrentHashMap<>();

    public void putAll(Map<String, Name> names) {
        this.qualifiedNames.putAll(names);
    }

    public Optional<Name> get(String identifier) {
        return Optional.ofNullable(qualifiedNames.get(identifier));
    }

    public Optional<Name> get(Name name) {
        if (name.getQualifier().isPresent()) {
            return Optional.of(name.clone());
        }
        return get(name.getIdentifier());
    }
}
