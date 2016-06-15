package com.nathanmyles.mapflattener;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for flattening and expanding {@link Map}s
 */
public class MapFlattener {

    private String separator = ".";

    private Pattern getListKeyPattern = Pattern.compile("(.+)\\[\\d+\\].*");
    private Pattern getListIndexPattern = Pattern.compile(".+\\[(\\d+)\\].*");

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
            } else if (entry.getValue() instanceof List) {
                List<Object> valueList = (List<Object>) entry.getValue();
                flatMap = flattenList(flatKey, valueList, flatMap);
            } else {
                flatMap.put(flatKey, entry.getValue());
            }
        }
        return flatMap;
    }

    private Map<String, Object> flattenList(String flatKey, List<Object> valueList, Map<String, Object> flatMap) {
        for (int i = 0; i < valueList.size(); i += 1) {
            String listFlatKey = flatKey + "[" + i + "]";
            Object object = valueList.get(i);
            if (object instanceof Map) {
                Map<String, Object> valueMap = (Map<String, Object>) object;
                flatMap = flattenMap(listFlatKey, valueMap, flatMap);
            } else {
                flatMap.put(listFlatKey, object);
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
            if (entry.getKey().contains(separator) || containsListElement(entry.getKey())) {
                expand(expandedMap, entry);
            } else {
                expandedMap.put(entry.getKey(), entry.getValue());
            }
        }
        return expandedMap;
    }

    private void expand(Map<String, Object> expandedMap, Map.Entry<String, Object> entry) {
        List<String> keys = new LinkedList<String>(Arrays.asList(entry.getKey().split(Pattern.quote(separator))));
        Object value = entry.getValue();
        Map<String, Object> currentMap = expandedMap;
        for (int i = 0; i < keys.size(); i += 1) {
            String key = keys.get(i);
            if (isLastKey(keys, i)) {
                setValue(currentMap, key, value);
            } else {
                currentMap = ensureNestedCollectionsExist(currentMap, key);
            }
        }
    }

    private void setValue(Map<String, Object> currentMap, String key, Object value) {
        if (containsListElement(key)) {
            List<Object> valueList = buildValueList(currentMap, key);
            int listIndex = getListIndex(key);
            valueList.set(listIndex, value);
        } else {
            currentMap.put(key, value);
        }
    }

    private Map<String, Object> ensureNestedCollectionsExist(Map<String, Object> currentMap, String key) {
        Map<String, Object> nestedMap;
        if (containsListElement(key)) {
            List<Object> valueList = buildValueList(currentMap, key);
            int listIndex = getListIndex(key);
            nestedMap = ensureNestedMapExists(valueList, listIndex);
        } else {
            nestedMap = ensureNestedMapExists(currentMap, key);
        }
        return nestedMap;
    }

    private List<Object> buildValueList(Map<String, Object> currentMap, String key) {
        int listIndex = getListIndex(key);
        String listKey = getListKey(key);
        List<Object> valueList = ensureNestedListExists(currentMap, listKey);
        createListElements(valueList, listIndex);
        return valueList;
    }

    private List<Object> ensureNestedListExists(Map<String, Object> currentMap, String listKey) {
        List<Object> valueList;
        if (currentMap.containsKey(listKey)) {
            valueList = (List<Object>) currentMap.get(listKey);
        } else {
            valueList = new ArrayList<Object>();
            currentMap.put(listKey, valueList);
        }
        return valueList;
    }

    private Map<String, Object> ensureNestedMapExists(List<Object> valueList, int listIndex) {
        Map<String, Object> nestedMap;
        if (valueList.get(listIndex) != null) {
            nestedMap = (Map<String, Object>) valueList.get(listIndex);
        } else {
            nestedMap = new TreeMap<String, Object>();
            valueList.set(listIndex, nestedMap);
        }
        return nestedMap;
    }

    private Map<String, Object> ensureNestedMapExists(Map<String, Object> currentMap, String key) {
        Map<String, Object> nestedMap;
        if (currentMap.containsKey(key)) {
            nestedMap = (Map<String, Object>) currentMap.get(key);
        } else {
            nestedMap = new TreeMap<String, Object>();
            currentMap.put(key, nestedMap);

        }
        return nestedMap;
    }

    private void createListElements(List<Object> valueList, int desiredListIndex) {
        int currentLastIndex = valueList.size() - 1;
        if (currentLastIndex < desiredListIndex) {
            for (int i = currentLastIndex + 1; i <= desiredListIndex; i += 1) {
                valueList.add(i, null);
            }
        }
    }

    private boolean containsListElement(String key) {
        return Pattern.matches(".+\\[\\d+\\].*", key);
    }

    private int getListIndex(String key) {
        Matcher matcher = getListIndexPattern.matcher(key);
        matcher.find();
        return Integer.parseInt(matcher.group(1));
    }

    private String getListKey(String key) {
        Matcher matcher = getListKeyPattern.matcher(key);
        matcher.find();
        return matcher.group(1);
    }

    private boolean isLastKey(List<String> keys, int i) {
        return i == keys.size() - 1;
    }

}
