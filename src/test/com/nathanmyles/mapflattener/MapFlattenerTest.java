package com.nathanmyles.mapflattener;

import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class MapFlattenerTest {

    @Test
    public void testFlattenMap() throws Exception {
        MapFlattener mapFlattener = new MapFlattener();

        Map<String, Object> nestedMap = new TreeMap<String, Object>();
        nestedMap.put("three", 2);
        Map<String, Object> nestedTwiceMap = new TreeMap<String, Object>();
        nestedTwiceMap.put("five", 5);
        nestedMap.put("four", nestedTwiceMap);
        Map<String, Object> map = new TreeMap<String, Object>();
        map.put("one", 1);
        map.put("two", nestedMap);

        Map<String, Object> flattenedMap = mapFlattener.flattenMap(map);

        assertTrue(flattenedMap.containsKey("one"));
        assertTrue(flattenedMap.containsKey("two.three"));
        assertTrue(flattenedMap.containsKey("two.four.five"));
        assertEquals(flattenedMap.get("one"), 1);
        assertEquals(flattenedMap.get("two.three"), 2);
        assertEquals(flattenedMap.get("two.four.five"), 5);

    }

    @Test
    public void testExpandMap() throws Exception {
        MapFlattener mapFlattener = new MapFlattener();

        Map<String, Object> flattenedMap = new TreeMap<String, Object>();
        flattenedMap.put("one", 1);
        flattenedMap.put("two.three", 2);
        flattenedMap.put("two.four.five", 5);
        flattenedMap.put("two.four.six", 6);

        Map<String, Object> map = mapFlattener.expandMap(flattenedMap);

        assertTrue(map.containsKey("one"));
        assertTrue(map.containsKey("two"));
        assertEquals(map.get("one"), 1);
        assertTrue(map.get("two") instanceof Map);
        assertTrue(((Map<String, Object>) map.get("two")).containsKey("three"));
        assertTrue(((Map<String, Object>) map.get("two")).containsKey("four"));
        assertEquals(((Map<String, Object>) map.get("two")).get("three"), 2);
        assertTrue(((Map<String, Object>) map.get("two")).get("four") instanceof Map);
        assertTrue(((Map<String, Object>) ((Map<String, Object>) map.get("two")).get("four")).containsKey("five"));
        assertTrue(((Map<String, Object>) ((Map<String, Object>) map.get("two")).get("four")).containsKey("six"));
        assertEquals(((Map<String, Object>) ((Map<String, Object>) map.get("two")).get("four")).get("five"), 5);
        assertEquals(((Map<String, Object>) ((Map<String, Object>) map.get("two")).get("four")).get("six"), 6);
    }

    @Test
    public void testFlattenMapWithSeparator() throws Exception {
        MapFlattener mapFlattener = new MapFlattener();
        mapFlattener.withSeparator("$");

        Map<String, Object> nestedMap = new TreeMap<String, Object>();
        nestedMap.put("three", 2);
        Map<String, Object> map = new TreeMap<String, Object>();
        map.put("one", 1);
        map.put("two", nestedMap);

        Map<String, Object> flattenedMap = mapFlattener.flattenMap(map);

        assertTrue(flattenedMap.containsKey("one"));
        assertTrue(flattenedMap.containsKey("two$three"));
        assertEquals(flattenedMap.get("one"), 1);
        assertEquals(flattenedMap.get("two$three"), 2);
    }

    @Test
    public void testExpandMapWithSeparator() throws Exception {
        MapFlattener mapFlattener = new MapFlattener();
        mapFlattener.withSeparator("$");

        Map<String, Object> flattenedMap = new TreeMap<String, Object>();
        flattenedMap.put("one", 1);
        flattenedMap.put("two$three", 2);

        Map<String, Object> map = mapFlattener.expandMap(flattenedMap);

        assertTrue(map.containsKey("one"));
        assertTrue(map.containsKey("two"));
        assertEquals(map.get("one"), 1);
        assertTrue(map.get("two") instanceof Map);
        assertTrue(((Map<String, Object>) map.get("two")).containsKey("three"));
        assertEquals(((Map<String, Object>) map.get("two")).get("three"), 2);
    }
}
