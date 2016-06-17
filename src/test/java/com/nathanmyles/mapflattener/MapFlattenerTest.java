package com.nathanmyles.mapflattener;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class MapFlattenerTest {

    // flatten a map
    @Test
    public void testFlattenMap() throws Exception {
        MapFlattener mapFlattener = new MapFlattener();

        /**
         * build up a nested map
         *
         * {
         *     "one": 1,
         *     "two": {
         *         "three": 2,
         *         "threeList": [
         *             1, 2, 3, {
         *                 "six": 6,
         *                 "sixList": [7, 8, 9]
         *             }
         *         ],
         *         "four": {
         *             "five": 5
         *         }
         *     }
         * }
         *
         */
        Map<String, Object> nestedListMap = new TreeMap<String, Object>();
        nestedListMap.put("six", 6);
        nestedListMap.put("sixList", Arrays.asList(7, 8, 9));
        Map<String, Object> nestedMap = new TreeMap<String, Object>();
        nestedMap.put("three", 3);
        nestedMap.put("threeList", Arrays.asList(1, 2, 3, nestedListMap));
        Map<String, Object> nestedTwiceMap = new TreeMap<String, Object>();
        nestedTwiceMap.put("five", 5);
        nestedMap.put("four", nestedTwiceMap);
        Map<String, Object> map = new TreeMap<String, Object>();
        map.put("one", 1);
        map.put("two", nestedMap);

        // flatten it!
        Map<String, Object> flattenedMap = mapFlattener.flattenMap(map);

        // now the map is no longer nested and all the values are available at the following keys:
        assertTrue(flattenedMap.containsKey("one"));
        assertTrue(flattenedMap.containsKey("two.three"));
        assertTrue(flattenedMap.containsKey("two.threeList[0]"));
        assertTrue(flattenedMap.containsKey("two.threeList[1]"));
        assertTrue(flattenedMap.containsKey("two.threeList[2]"));
        assertTrue(flattenedMap.containsKey("two.threeList[3].six"));
        assertTrue(flattenedMap.containsKey("two.threeList[3].sixList[0]"));
        assertTrue(flattenedMap.containsKey("two.threeList[3].sixList[1]"));
        assertTrue(flattenedMap.containsKey("two.threeList[3].sixList[2]"));
        assertTrue(flattenedMap.containsKey("two.four.five"));
        assertEquals(flattenedMap.get("one"), 1);
        assertEquals(flattenedMap.get("two.three"), 3);
        assertEquals(flattenedMap.get("two.threeList[0]"), 1);
        assertEquals(flattenedMap.get("two.threeList[1]"), 2);
        assertEquals(flattenedMap.get("two.threeList[2]"), 3);
        assertEquals(flattenedMap.get("two.threeList[3].six"), 6);
        assertEquals(flattenedMap.get("two.threeList[3].sixList[0]"), 7);
        assertEquals(flattenedMap.get("two.threeList[3].sixList[1]"), 8);
        assertEquals(flattenedMap.get("two.threeList[3].sixList[2]"), 9);
        assertEquals(flattenedMap.get("two.four.five"), 5);
    }

    // expand a map
    @Test
    public void testExpandMap() throws Exception {
        MapFlattener mapFlattener = new MapFlattener();

        // take a map that has been flatten
        Map<String, Object> flattenedMap = new TreeMap<String, Object>();
        flattenedMap.put("one", 1);
        flattenedMap.put("oneList[0]", 1);
        flattenedMap.put("oneList[1]", 2);
        flattenedMap.put("oneList[2]", 3);
        flattenedMap.put("two.three", 2);
        flattenedMap.put("two.threeList[0]", 1);
        flattenedMap.put("two.threeList[1]", 2);
        flattenedMap.put("two.threeList[2]", 3);
        flattenedMap.put("two.threeList[3].six", 6);
        flattenedMap.put("two.threeList[3].sixList[0]", 7);
        flattenedMap.put("two.threeList[3].sixList[1]", 8);
        flattenedMap.put("two.threeList[3].sixList[2]", 9);
        flattenedMap.put("two.four.five", 5);
        flattenedMap.put("two.four.seven", 7);
        flattenedMap.put("two.four.eight.nine", 9);
        flattenedMap.put("two.four.eight.ten", 10);

        // expand it!
        Map<String, Object> map = mapFlattener.expandMap(flattenedMap);

        // now the map is nested again
        assertTrue(map.containsKey("one"));
        assertTrue(map.containsKey("oneList"));
        assertTrue(map.get("oneList") instanceof List);
        List<Object> listOne = (List<Object>) map.get("oneList");
        assertEquals(listOne.get(0), 1);
        assertEquals(listOne.get(1), 2);
        assertEquals(listOne.get(2), 3);
        assertTrue(map.containsKey("two"));
        assertEquals(map.get("one"), 1);
        assertTrue(map.get("two") instanceof Map);
        Map<String, Object> mapTwo = (Map<String, Object>) map.get("two");
        assertTrue(mapTwo.containsKey("three"));
        assertTrue(mapTwo.containsKey("threeList"));
        assertTrue(mapTwo.containsKey("four"));
        assertEquals(mapTwo.get("three"), 2);
        assertTrue(mapTwo.get("threeList") instanceof List);
        List<Object> listThree = (List<Object>) mapTwo.get("threeList");
        assertEquals(listThree.get(0), 1);
        assertEquals(listThree.get(1), 2);
        assertEquals(listThree.get(2), 3);
        assertTrue(listThree.get(3) instanceof Map);
        Map<String, Object> listThreeMapSix = (Map<String, Object>) listThree.get(3);
        assertTrue(listThreeMapSix.containsKey("six"));
        assertTrue(listThreeMapSix.containsKey("sixList"));
        assertEquals(listThreeMapSix.get("six"), 6);
        assertTrue(listThreeMapSix.get("sixList") instanceof List);
        List<Object> ListSix = (List<Object>) listThreeMapSix.get("sixList");
        assertEquals(ListSix.get(0), 7);
        assertEquals(ListSix.get(1), 8);
        assertEquals(ListSix.get(2), 9);
        assertTrue(mapTwo.get("four") instanceof Map);
        Map<String, Object> mapFour = (Map<String, Object>) mapTwo.get("four");
        assertTrue(mapFour.containsKey("five"));
        assertTrue(mapFour.containsKey("seven"));
        assertTrue(mapFour.containsKey("eight"));
        assertEquals(mapFour.get("five"), 5);
        assertEquals(mapFour.get("seven"), 7);
        assertTrue(mapFour.get("eight") instanceof Map);
        Map<String, Object> mapEight = (Map<String, Object>) mapFour.get("eight");
        assertTrue(mapEight.containsKey("nine"));
        assertTrue(mapEight.containsKey("ten"));
        assertEquals(mapEight.get("nine"), 9);
        assertEquals(mapEight.get("ten"), 10);

    }

    // use a different separator to flatten
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

    // use a different separator to expand
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
