# MapFlattener

Convert nested Maps into flat Maps and then expand them back!

[![Build Status](https://travis-ci.org/nathanmyles/MapFlattener.svg?branch=master)](https://travis-ci.org/nathanmyles/MapFlattener)

[![Coverage Status](https://coveralls.io/repos/github/nathanmyles/MapFlattener/badge.svg?branch=master)](https://coveralls.io/github/nathanmyles/MapFlattener?branch=master)

### Easy to use

Create an instance on the MapFlattener class
```java
MapFlattener mapFlattener = new MapFlattener();
```

By default key concatenation uses dots, but you can override this like so
```java
mapFlattener.withSeparator("$");
```

Now you can start flattening and expanding maps
```java
// flatten it!
Map<String, Object> flattenedMap = mapFlattener.flattenMap(map);

// expand it!
Map<String, Object> map = mapFlattener.expandMap(flattenedMap);
```

Check out the tests for more examples

### Pull it from Maven Central!

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.nathanmyles/mapflattener/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.nathanmyles/mapflattener)

You can pull from the central maven repository, just add this to your __pom.xml__ file's `<dependencies>` section:

```xml
<dependency>
    <groupId>com.nathanmyles</groupId>
    <artifactId>mapflattener</artifactId>
    <version>1.0</version>
</dependency>
```

