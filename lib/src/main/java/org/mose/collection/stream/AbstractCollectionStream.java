package org.mose.collection.stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mose.collection.stream.builder.StreamCollectionBuilderImpl;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

public class AbstractCollectionStream<O, M> implements Collection<M> {

    private final @NotNull Supplier<Stream<O>> getter;
    private final @Nullable Supplier<Stream<O>> parallelGetter;
    private final @NotNull Function<Stream<O>, Stream<M>> mapper;
    protected final @NotNull BiPredicate<M, Object> equalsCompare;
    protected final @Nullable BiPredicate<Integer, Collection<? extends M>> adder;
    protected final @Nullable Predicate<Collection<?>> remover;
    private final @Nullable Runnable clear;
    private final @Nullable IntSupplier size;
    private final @Nullable Predicate<Collection<?>> retainer;

    public AbstractCollectionStream(@NotNull StreamCollectionBuilderImpl<O, M> builder, boolean isModifiable) {
        this.getter = Objects.requireNonNull(builder.getter(), "Stream getter was not set");
        this.mapper = Objects.requireNonNull(builder.mapper(), "Stream mapper was not set");
        this.equalsCompare = Objects.requireNonNullElse(builder.equalsCompare(), Object::equals);

        this.parallelGetter = builder.parallelGetter();
        this.size = builder.size();

        this.adder = isModifiable ? Objects.requireNonNull(builder.adder(), "Stream adder was not set") : null;
        this.remover = isModifiable ? Objects.requireNonNull(builder.remover(), "Stream remover was not set") : null;
        this.clear = builder.clear();
        this.retainer = builder.retainer();
    }

    @Override
    public Stream<M> stream() {
        return this.mapper.apply(this.getter.get());
    }

    @Override
    public Stream<M> parallelStream() {
        if (this.parallelGetter == null) {
            return stream().parallel();
        }
        return this.mapper.apply(this.parallelGetter.get());
    }

    @SuppressWarnings("ReplaceInefficientStreamCount")
    @Override
    public int size() {
        if (this.size == null) {
            //backup for lazy developers
            return (int) this.stream().count();
        }
        return this.size.getAsInt();
    }

    @Override
    public boolean isEmpty() {
        return this.stream().findAny().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.stream().anyMatch(value -> this.equalsCompare.test(value, o));
    }

    @Override
    @NotNull
    public Iterator<M> iterator() {
        return this.stream().iterator();
    }

    @Override
    public Spliterator<M> spliterator() {
        return this.stream().spliterator();
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    @Override
    @NotNull
    public Object[] toArray() {
        return this.stream().toArray();
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    @Override
    @NotNull
    public <T> T[] toArray(@NotNull T[] array) {
        return this.stream().toArray(value -> Arrays.copyOf(array, value));
    }

    @SuppressWarnings("RedundantCollectionOperation")
    @Override
    public boolean add(M m) {
        return this.addAll(Collections.singletonList(m));
    }

    @Override
    public boolean remove(Object o) {
        return removeAll(Collections.singletonList(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.stream().allMatch(v -> c.stream().anyMatch(compare -> this.equalsCompare.test(v, compare)));
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends M> c) {
        if (this.adder == null) {
            throw new IllegalStateException("Collection is set as read-only");
        }
        return this.adder.test(-1, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (this.remover == null) {
            throw new IllegalStateException("Collection is set as read-only");
        }
        return this.remover.test(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (this.retainer != null) {
            return this.retainer.test(c);
        }
        if (this.remover != null) {
            //lazy developer fix
            var toRemove = this.stream().filter(value -> !c.contains(value)).toList();
            return this.remover.test(toRemove);
        }
        throw new IllegalStateException("Collection is set as read-only");

    }

    @Override
    public void clear() {
        if (this.clear != null) {
            this.clear.run();
            return;
        }
        if (this.remover != null) {
            //lazy developer override
            this.remover.test(this);
        }
        throw new IllegalStateException("Collection is set as read-only");
    }
}
