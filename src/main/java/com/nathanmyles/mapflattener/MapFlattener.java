package com.nathanmyles.mapflattener;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Utility for flattening and expanding {@link Map}s
 */
public class MapFlattener {

    private String separator = ".";

    /**
     * Set the separator to be used when flattening and expanding
     *
     * @param separator A character(s) to use when concatenating and splitting keys
     * @return this so the method call can be chained
     */
    public MapFlattener withSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    /**
     * flatten a {@link Map}
     *
     * turn a {@link Map} structure like: { "one": 1, "two": { "three": 2 } }
     * into a {@link Map} structure like: { "one": 1, "two.three": 2 }
     *
     * @param map the {@link Map} to flatten
     * @return the flattened {@link Map}
     */
    public Map<String, Object> flattenMap(Map<String, Object> map) {
        return flattenMap("", map, new TreeMap<String, Object>());
    }

    private Map<String, Object> flattenMap(String key, Map<String, Object> sourceMap, Map<String, Object> flatMap) {
        for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
            String flatKey;
            if (!key.isEmpty()) {
                flatKey = key + separator + entry.getKey();
            } else {
                flatKey = entry.getKey();
            }
            if (entry.getValue() instanceof Map) {
                Map<String, Object> valueMap = (Map<String, Object>) entry.getValue();
                flatMap = flattenMap(flatKey, valueMap, flatMap);
            } else {

                flatMap.put(flatKey, entry.getValue());
            }
        }
        return flatMap;
    }

    /**
     * expand a flattened {@link Map}
     *
     * turn a {@link Map} structure like: { "one": 1, "two.three": 2 }
     * into a {@link Map} structure like: { "one": 1, "two": { "three": 2 } }
     *
     * @param map the {@link Map} to expand
     * @return the expanded {@link Map}
     */
    public Map<String, Object> expandMap(Map<String, Object> map) {
        return expandMap(map, new TreeMap<String, Object>());
    }

    private Map<String, Object> expandMap(Map<String, Object> sourceMap, Map<String, Object> expandedMap) {
        for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
            if (entry.getKey().contains(separator)) {
                expand(expandedMap, entry);
            } else {
                expandedMap.put(entry.getKey(), entry.getValue());
            }
        }
        return expandedMap;
    }

    private void expand(Map<String, Object> expandedMap, Map.Entry<String, Object> entry) {
        List<String> keys = new LinkedList<String>(Arrays.asList(entry.getKey().split(Pattern.quote(separator))));
        String key = keys.remove(0);
        Map<String, Object> valueMap;
        if (expandedMap.containsKey(key)) {
            valueMap = (Map<String, Object>) expandedMap.get(key);
        } else {
            valueMap = new TreeMap<String, Object>();
        }
        expandedMap.put(key, buildValueMap(valueMap, keys, entry.getValue()));
    }

    private Map<String, Object> buildValueMap(Map<String, Object> valueMap, List<String> keys, Object value) {
        populateValueMap(valueMap, keys, value);
        return valueMap;
    }

    private void populateValueMap(Map<String, Object> valueMap, List<String> keys, Object value) {
        String key = keys.remove(0);
        if (keys.isEmpty()) {
            valueMap.put(key, value);
            return;
        }
        Map<String, Object> nestedMap;
        if (valueMap.containsKey(key)) {
            nestedMap = (Map<String, Object>) valueMap.get(key);
        } else {
            nestedMap = new TreeMap<String, Object>();
        }
        valueMap.put(key, nestedMap);
        populateValueMap(nestedMap, keys, value);
    }

}
