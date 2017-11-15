package com.jayway.jsonpath;

import com.jayway.jsonpath.spi.json.PojoJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class PojoProviderTest extends BaseTest {

    private static final Configuration POJO_CONFIGURATION = Configuration
            .builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new PojoJsonProvider())
            .build();


    @Test
    public void should_pares_pojo() {
        FooBarBaz source = new FooBarBaz();
        source.foo = "fooValue";
        source.bar = 1234L;

        String foo = JsonPath.using(POJO_CONFIGURATION).parse(source).read("$.foo");
        Long bar = JsonPath.using(POJO_CONFIGURATION).parse(source).read("$.bar");

        assertThat(foo).isEqualTo(source.foo);
        assertThat(bar).isEqualTo(1234L);
    }

    @Test
    public void recursion_should_work() {
        FooBarBaz source = new FooBarBaz();
        source.foo = "fooValue";
        source.bar = 1234L;
        source.recursive = source;

        String foo = JsonPath.using(POJO_CONFIGURATION).parse(source).read("$.recursive.recursive.recursive.foo");
        Long bar = JsonPath.using(POJO_CONFIGURATION).parse(source).read("$.bar");

        assertThat(foo).isEqualTo(source.foo);
        assertThat(bar).isEqualTo(1234L);
    }

    @Test
    public void array_should_work() throws Exception {
        FooAggregate fooAggregate = prepareFooAggregate();

        FooBarBaz result = JsonPath.using(POJO_CONFIGURATION).parse(fooAggregate).read("$.values[1]");

        assertThat(result.bar).isEqualTo(2);
    }
    @Test
    public void real_array_should_work() throws Exception {
        FooAggregate fooAggregate = prepareFooAggregate();

        Object result = JsonPath.using(POJO_CONFIGURATION).parse(fooAggregate).read("$.arr[1]");

        assertThat(result).isEqualTo(8);
    }

    @Test
    public void subarray_should_work() throws Exception {
        FooAggregate fooAggregate = prepareFooAggregate();

        Collection<FooBarBaz> read = JsonPath.using(POJO_CONFIGURATION).parse(fooAggregate).read("$.values[1:3]");

        assertThat(read).hasSize(2);
    }

    @Test
    public void select_should_work() throws Exception {
        FooAggregate fooAggregate = prepareFooAggregate();

        Collection<Long> read = JsonPath.using(POJO_CONFIGURATION).parse(fooAggregate).read("$.values[1:3].bar");

        assertThat(read).containsSequence(2L, 3L);
    }

    @Test
    public void search_should_work() throws Exception {
        FooAggregate fooAggregate = prepareFooAggregate();

        Collection<Long> read = JsonPath.using(POJO_CONFIGURATION).parse(fooAggregate).read("$.values[?(@.baz == false)].bar");

        assertThat(read).containsSequence(1L, 4L);
    }

    @Test
    public void traversing_map() throws Exception {
        FooAggregate fooAggregate = prepareFooAggregate();

        Long adf = JsonPath.using(POJO_CONFIGURATION).parse(fooAggregate)
                .read("$.values[2].map.adf");

        String kuu = JsonPath.using(POJO_CONFIGURATION).parse(fooAggregate)
                .read("$.values[2].map.boo.kuu");

        assertThat(adf).isEqualTo(621L);
        assertThat(kuu).isEqualTo("kuu");
    }

    private FooAggregate prepareFooAggregate() {
        FooAggregate fooAggregate = new FooAggregate();
        fooAggregate.arr = new int[]{4, 8, 16};
        fooAggregate.values = Arrays.asList(
                new FooBarBaz("A", 1L, false),
                new FooBarBaz("B", 2L, true),
                new FooBarBaz("C", 3L, true),
                new FooBarBaz("D", 4L, false)
        );
        fooAggregate.values.get(2).map = new HashMap<String, Object>() {{
            put("adf", 621L);
            put("boo", new HashMap<String, String>(){{
                put("kuu", "kuu");
            }});
        }};
        return fooAggregate;
    }

    @Test
    public void an_object_can_be_mapped_to_pojo() {

        String json = "{\n" +
                "   \"foo\" : \"foo\",\n" +
                "   \"bar\" : 11,\n" +
                "   \"baz\" : true\n" +
                "}";


        FooBarBaz fooBarBaz = JsonPath.using(POJO_CONFIGURATION).parse(json).read("$", FooBarBaz.class);

        assertThat(fooBarBaz.foo).isEqualTo("foo");
        assertThat(fooBarBaz.bar).isEqualTo(11L);
        assertThat(fooBarBaz.baz).isEqualTo(true);

    }

    private static class FooAggregate {
        List<FooBarBaz> values;
        public int[] arr;
    }

    private static class FooBarBaz {
        FooBarBaz recursive;
        public String foo;
        public Long bar;
        public boolean baz;
        public Map map;
        FooBarBaz(String foo, Long bar, boolean baz) {
            this.foo = foo;
            this.bar = bar;
            this.baz = baz;
        }
        FooBarBaz() {
        }
    }

}
