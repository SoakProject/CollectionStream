package org.mose.collection.stream.builder;

import org.jetbrains.annotations.NotNull;
import org.mose.collection.stream.specific.list.ListStream;
import org.mose.collection.stream.specific.set.SetStream;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Stream;

public class StreamCollectionBuilderImpl<O, M> implements CollectionStreamBuilder.Origin, CollectionStreamBuilder.Mapping<O>, CollectionStreamBuilder.Rules<O, M>, CollectionStreamBuilder.SortedRules<O, M>, CollectionStreamBuilder.SortedMapping<O, M> {

    private Supplier<Stream<O>> getter;
    private Supplier<Stream<O>> parallelGetter;
    private Function<Stream<O>, Stream<M>> mapper;
    private BiPredicate<M, Object> equalsCompare;
    private BiPredicate<Integer, Collection<? extends M>> adder;
    private Predicate<Collection<?>> remover;
    private Runnable clear;
    private IntSupplier size;
    private Predicate<Collection<?>> retainer;
    private ToIntFunction<M> toIndexFirst;
    private ToIntFunction<M> toIndexLast;
    private BiFunction<Integer, M, M> setter;

    public BiFunction<Integer, M, M> setter() {
        return setter;
    }

    public ToIntFunction<M> toIndexFirst() {
        return toIndexFirst;
    }

    public ToIntFunction<M> toIndexLast() {
        return toIndexLast;
    }

    public Supplier<Stream<O>> getter() {
        return getter;
    }

    public Supplier<Stream<O>> parallelGetter() {
        return parallelGetter;
    }

    public Function<Stream<O>, Stream<M>> mapper() {
        return mapper;
    }

    public BiPredicate<M, Object> equalsCompare() {
        return equalsCompare;
    }

    public BiPredicate<Integer, Collection<? extends M>> adder() {
        return adder;
    }

    public Predicate<Collection<?>> remover() {
        return remover;
    }

    public Runnable clear() {
        return clear;
    }

    public IntSupplier size() {
        return size;
    }

    public Predicate<Collection<?>> retainer() {
        return retainer;
    }

    @Override
    public <I> CollectionStreamBuilder.Mapping<I> stream(@NotNull Supplier<Stream<I>> supplier) {
        this.getter = (Supplier<Stream<O>>) (Object) supplier;
        return (CollectionStreamBuilder.Mapping<I>) this;
    }

    @Override
    public <Origin, Map> CollectionStreamBuilder.SortedMapping<Origin, Map> collection(@NotNull Collection<Origin> collection, Function<Map, Origin> mapBack) {
        if (collection instanceof List<Origin> list) {
            this.toIndexFirst = value -> list.indexOf(mapBack.apply((Map) value));
            this.toIndexLast = value -> list.lastIndexOf(mapBack.apply((Map) value));

            this.adder = (integer, ms) -> {
                int index = integer == -1 ? collection.size() - 1 : integer;
                return list.addAll(index, ms.stream().map(value -> mapBack.apply((Map) value)).toList());
            };
        } else {
            this.adder = (integer, ms) -> collection.addAll(ms.stream().map(value -> mapBack.apply((Map) value)).toList());
        }
        this.remover = mappedCollection -> collection.removeAll(mappedCollection.stream().map(value -> mapBack.apply((Map) value)).toList());
        this.retainer = mappedCollection -> collection.retainAll(mappedCollection.stream().map(value -> mapBack.apply((Map) value)).toList());
        this.size = collection::size;
        return (CollectionStreamBuilder.SortedMapping<Origin, Map>) collection(collection);
    }

    @Override
    public <O, M> CollectionStreamBuilder.SortedMapping<O, M> array(O[] array, Function<M, O> mapBack) {
        return (CollectionStreamBuilder.SortedMapping<O, M>) array(array);
    }

    @Override
    public CollectionStreamBuilder.Rules<O, M> withEquals(@NotNull BiPredicate<M, Object> compare) {
        equalsCompare = compare;
        return this;
    }

    @Override
    public StreamCollectionBuilderImpl<O, M> withParallel(@NotNull Supplier<Stream<O>> supplier) {
        this.parallelGetter = supplier;
        return this;
    }

    @Override
    public StreamCollectionBuilderImpl<O, M> withClear(@NotNull Runnable runner) {
        this.clear = runner;
        return this;
    }

    @Override
    public CollectionStreamBuilder.SortedRules<O, M> withFirstIndexOf(@NotNull ToIntFunction<M> indexFind) {
        this.toIndexFirst = indexFind;
        return this;
    }

    @Override
    public StreamCollectionBuilderImpl<O, M> withRemoveAll(@NotNull Predicate<Collection<?>> toRemove) {
        this.remover = toRemove;
        return this;
    }

    @Override
    public CollectionStreamBuilder.SortedRules<O, M> withLastIndexOf(@NotNull ToIntFunction<M> indexFind) {
        this.toIndexLast = indexFind;
        return this;
    }

    @Override
    public CollectionStreamBuilder.SortedRules<O, M> withSet(@NotNull BiFunction<Integer, M, M> setter) {
        this.setter = setter;
        return this;
    }

    @Override
    public CollectionStreamBuilder.SortedRules<O, M> withAddToIndex(@NotNull BiPredicate<Integer, Collection<? extends M>> index) {
        this.adder = index;
        return this;
    }

    @Override
    public CollectionStreamBuilder.SortedRules<O, M> withAddToLast(@NotNull Predicate<Collection<? extends M>> last) {
        this.adder = (index, collection) -> last.test(collection);
        return this;
    }

    @Override
    public List<M> buildList(boolean isModifiable) {
        return new ListStream<>(this, isModifiable);
    }

    @Override
    public Set<M> buildSet(boolean isModifiable) {
        return new SetStream<>(this, isModifiable);
    }

    @Override
    public <I> CollectionStreamBuilder.SortedRules<O, I> map(@NotNull Function<Stream<O>, Stream<I>> mapping) {
        this.mapper = (Function<Stream<O>, Stream<M>>) (Object) mapping;
        return (CollectionStreamBuilder.SortedRules<O, I>) this;
    }
}
