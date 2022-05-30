package org.esupportail.activbo;

import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    
    public static String removePrefixOrNull(String s, String prefix) {
        return s.startsWith(prefix) ? s.substring(prefix.length()) : null;
    }
    public static String removeSuffixOrNull(final String s, final String suffix) {
        return s.endsWith(suffix) ? s.substring(0, s.length() - suffix.length()) : null;
    }
    public static String removeSuffix(final String s, final String suffix) {
        return s.endsWith(suffix) ? s.substring(0, s.length() - suffix.length()) : s;
    }

    public static String[] toArray(List<String> l) {
        return l.toArray(new String[0]);
    }
    public static String[] toArray(Set<String> l) {
        return l.toArray(new String[0]);
    }
    public static Set<String> toSet(String[] l) {
        var r = new HashSet<String>();
        for (var e: l) r.add(e);
        return r;
    }

    public static <V> MapBuilder<V> asMap(String key, V value) {
        return new MapBuilder<V>().add(key, value);
    }

    public static class MapBuilder<V> extends HashMap<String, V> {
        public MapBuilder<V> add(String key, V value) {
            this.put(key, value);
            return this;
        }
    }

    public static <T,V> Set<V> mapSet(final Set<T> in, final Function<T, V> function) {
        return in == null ? null : in.stream().map(function).collect(Collectors.toSet());
    }

    public static List<String> encodeBase64s(List<? extends Object> vals) {
        var r = new LinkedList<String>();
        for (var val: vals) {
            r.add(new String(Base64.getEncoder().encode((byte[]) val)));
        }
        return r;
    }

    public static List<byte[]> decodeBase64s(List<String> vals) {
        var r = new LinkedList<byte[]>();
        for (var val: vals) {
            r.add(Base64.getDecoder().decode(val));
        }
        return r;
    }
    
}
