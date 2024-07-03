package org.mose.collection.stream.builder;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface CollectionStreamBuilder {

    static Origin builder() {
        return new StreamCollectionBuilderImpl<>();
    }

    interface Origin {

        <O> Mapping<O> stream(@NotNull Supplier<Stream<O>> supplier);

        default <O> Mapping<O> stream(@NotNull Stream<O> stream) {
            return stream(() -> stream);
        }

        default <O> Mapping<O> collection(@NotNull Collection<O> collection) {
            return stream(collection::stream).withParallel(collection::parallelStream).withClear(collection::clear);
        }

        <O, M> SortedMapping<O, M> collection(@NotNull Collection<O> collection, Function<M, O> mapBack);

        default <O> Mapping<O> array(@NotNull Supplier<O[]> array) {
            return stream(() -> Stream.of(array.get()));
        }

        default <O> Mapping<O> array(O... array) {
            return stream(() -> Stream.of(array));
        }

        <O, M> SortedMapping<O, M> array(O[] array, Function<M, O> mapBack);

        default <O> Mapping<O> splitIterator(@NotNull Supplier<Spliterator<O>> supplier) {
            return stream(() -> StreamSupport.stream(supplier.get(), false)).withParallel(() -> StreamSupport.stream(supplier.get(), true));
        }

        default <O> Mapping<O> iterable(@NotNull Supplier<Iterable<O>> supplier) {
            return splitIterator(() -> supplier.get().spliterator());
        }

        default <O> Mapping<O> iterable(@NotNull Iterable<O> iterable) {
            return splitIterator(iterable::spliterator);
        }

    }

    interface Mapping<O> {

        Mapping<O> withParallel(@NotNull Supplier<Stream<O>> supplier);

        Mapping<O> withClear(@NotNull Runnable runner);

        <M> Rules<O, M> map(@NotNull Function<Stream<O>, Stream<M>> mapping);

        default <M> Rules<O, M> basicMap(@NotNull Function<O, M> mapping) {
            return map((stream) -> stream.map(mapping));
        }
    }

    interface SortedMapping<O, M> extends Mapping<O> {

        @Override
        SortedMapping<O, M> withParallel(@NotNull Supplier<Stream<O>> supplier);

        @Override
        SortedMapping<O, M> withClear(@NotNull Runnable runner);

        @Override
        <I> SortedRules<O, I> map(@NotNull Function<Stream<O>, Stream<I>> mapping);

        @Override
        default <I> SortedRules<O, I> basicMap(@NotNull Function<O, I> mapping) {
            return (SortedRules<O, I>) Mapping.super.basicMap(mapping);
        }
    }

    interface Rules<O, M> {

        Rules<O, M> withAddToLast(@NotNull Predicate<Collection<? extends M>> last);

        Rules<O, M> withEquals(@NotNull BiPredicate<M, Object> compare);

        Rules<O, M> withParallel(@NotNull Supplier<Stream<O>> supplier);

        Rules<O, M> withClear(@NotNull Runnable runner);

        Rules<O, M> withRemoveAll(@NotNull Predicate<Collection<?>> toRemove);

        SortedRules<O, M> withFirstIndexOf(@NotNull ToIntFunction<M> indexFind);

        Set<M> buildSet(boolean modifiable);

        default Set<M> buildSet() {
            return buildSet(false);
        }
    }

    interface SortedRules<O, M> extends Rules<O, M> {

        SortedRules<O, M> withLastIndexOf(@NotNull ToIntFunction<M> indexFind);

        SortedRules<O, M> withSet(@NotNull BiFunction<Integer, M, M> setter);

        SortedRules<O, M> withAddToIndex(@NotNull BiPredicate<Integer, Collection<? extends M>> index);

        @Override
        @Deprecated
        SortedRules<O, M> withAddToLast(@NotNull Predicate<Collection<? extends M>> last);

        @Override
        Rules<O, M> withEquals(@NotNull BiPredicate<M, Object> compare);

        @Override
        SortedRules<O, M> withParallel(@NotNull Supplier<Stream<O>> supplier);

        @Override
        SortedRules<O, M> withClear(@NotNull Runnable runner);

        @Override
        SortedRules<O, M> withRemoveAll(@NotNull Predicate<Collection<?>> toRemove);

        List<M> buildList(boolean modifiable);

        default List<M> buildList() {
            return buildList(false);
        }

    }
}
