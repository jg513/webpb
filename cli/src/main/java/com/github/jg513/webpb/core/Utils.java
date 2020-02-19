package com.github.jg513.webpb.core;

import java.util.Collection;

public class Utils {

    public static boolean containsAny(Collection<?> objects, Collection<?> elements) {
        for (Object element : elements) {
            if (objects.contains(element)) {
                return true;
            }
        }
        return false;
    }
}
