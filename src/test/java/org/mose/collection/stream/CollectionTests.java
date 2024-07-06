package org.mose.collection.stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mose.collection.stream.builder.CollectionStreamBuilder;
import org.mose.collection.stream.builder.StreamCollectionBuilderImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionTests {

    @Test
    public void testBasicReadonlyListWithMinimumFromList() {
        //arrange
        var origin = List.of(1, 2, 3, 4, 5, 6);

        //act
        var mapped = CollectionStreamBuilder
                .builder()
                .collection(origin)
                .basicMap(Integer::byteValue)
                .withFirstIndexOf(b -> origin.indexOf((int) b))
                .buildList();

        //assert
        Assertions.assertEquals(origin.size(), mapped.size());
        Assertions.assertEquals(origin.get(0).byteValue(), mapped.get(0));
        Assertions.assertEquals(origin.indexOf(2), mapped.indexOf((byte) 2));
        Assertions.assertTrue(mapped.contains((byte) 5));
        Assertions.assertEquals(origin.lastIndexOf(6), mapped.lastIndexOf((byte) 6));
        Assertions.assertArrayEquals(new Byte[]{1, 2, 3, 4, 5, 6}, mapped.toArray(Byte[]::new));
    }

    @Test
    public void testModifiableListWithMinimumFromList() {
        //arrange
        var origin = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        var mapped = CollectionStreamBuilder
                .builder()
                .collection(origin)
                .basicMap(Integer::byteValue)
                .withRemoveAll(toRemove -> origin.removeAll(toRemove.stream().map(in -> ((Byte) in).intValue()).collect(Collectors.toSet())))
                .withFirstIndexOf(b -> origin.indexOf((int) b))
                .withAddToIndex((index, adding) -> origin.addAll(index, adding.stream().map(Byte::intValue).collect(Collectors.toSet())))
                .withSet((index, value) -> origin.set(index, value.intValue()).byteValue())
                .buildList(true);

        //act
        mapped.add(3, (byte)4);

        //assert
        Assertions.assertEquals(origin.size(), mapped.size());
        Assertions.assertEquals(origin.get(0).byteValue(), mapped.get(0));
        Assertions.assertEquals(origin.indexOf(2), mapped.indexOf((byte) 2));
        Assertions.assertTrue(mapped.contains((byte) 5));
        Assertions.assertEquals(origin.lastIndexOf(6), mapped.lastIndexOf((byte) 6));
        Assertions.assertArrayEquals(new Byte[]{1, 2, 3, 4, 4, 5, 6}, mapped.toArray(Byte[]::new));
    }

    @Test
    public void testBasicReadonlyListWithMinimumFromArray() {
        //arrange
        var origin = new Integer[]{1, 2, 3, 4, 5, 6};

        //act
        var mapped = CollectionStreamBuilder
                .builder()
                .array(origin)
                .basicMap(Integer::byteValue)
                .withFirstIndexOf(value -> {
                    for (int index = 0; index < origin.length; index++) {
                        if (origin[index].byteValue() == value) {
                            return index;
                        }
                    }
                    return -1;
                })
                .buildList();

        //assert
        Assertions.assertEquals(origin.length, mapped.size());
        Assertions.assertEquals(origin[0].byteValue(), mapped.get(0));
        Assertions.assertEquals(1, mapped.indexOf((byte) 2));
        Assertions.assertTrue(mapped.contains((byte) 5));
        Assertions.assertEquals(5, mapped.lastIndexOf((byte) 6));
        Assertions.assertArrayEquals(new Byte[]{1, 2, 3, 4, 5, 6}, mapped.toArray(Byte[]::new));
    }

    @Test
    public void testBasicReadonlySetWithMinimumFromList() {
        //arrange
        var origin = List.of(1, 2, 3, 4, 5, 6);

        //act
        var mapped = CollectionStreamBuilder
                .builder()
                .collection(origin)
                .basicMap(Integer::byteValue)
                .buildSet();

        //assert
        Assertions.assertEquals(origin.size(), mapped.size());
        Assertions.assertTrue(mapped.contains((byte) 5));
        Assertions.assertArrayEquals(new Byte[]{1, 2, 3, 4, 5, 6}, mapped.toArray(Byte[]::new));
    }
}
