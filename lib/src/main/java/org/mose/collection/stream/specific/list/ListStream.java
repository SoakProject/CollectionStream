package org.mose.collection.stream.specific.list;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mose.collection.stream.AbstractCollectionStream;
import org.mose.collection.stream.builder.StreamCollectionBuilderImpl;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public class ListStream<O, M> extends AbstractCollectionStream<O, M> implements List<M> {

    private final @NotNull ToIntFunction<M> toIndexFirst;
    private final @Nullable ToIntFunction<M> toIndexLast;
    private final @Nullable BiFunction<Integer, M, M> setter;

    public ListStream(@NotNull StreamCollectionBuilderImpl<O, M> builder, boolean isModifiable) {
        super(builder, isModifiable);
        this.toIndexFirst = Objects.requireNonNull(builder.toIndexFirst(), "stream index first is required for lists");
        this.toIndexLast = builder.toIndexLast();
        this.setter = isModifiable ? Objects.requireNonNull(builder.setter(), "Setter must be set") : null;
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends M> c) {
        if (this.adder == null) {
            throw new IllegalStateException("Collection is set as read-only");
        }
        return this.adder.test(index, c);
    }

    @Override
    public M get(int index) {
        return this.stream().skip(index).findFirst().orElseThrow(() -> new IndexOutOfBoundsException(index));
    }

    @Override
    public M set(int index, M element) {
        if (this.setter != null) {
            return this.setter.apply(index, element);
        }
        if (this.remover != null && this.adder != null) {
            var currentValue = this.remove(index);
            this.adder.test(index, Collections.singletonList(element));
            return currentValue;
        }
        throw new IllegalStateException("Collection is set as read-only");
    }

    @Override
    public void add(int index, M element) {
        addAll(index, Collections.singletonList(element));
    }

    @Override
    public M remove(int index) {
        if (this.remover != null) {
            var value = this.get(index);
            this.remover.test(Collections.singletonList(value));
            return value;
        }
        throw new IllegalStateException("Collection is set as read-only");

    }

    @Override
    public int indexOf(Object o) {
        return this.stream().filter(v -> equalsCompare.test(v, o)).findFirst().map(this.toIndexFirst::applyAsInt).orElse(-1);
    }

    @Override
    public int lastIndexOf(Object o) {
        var values = this.stream().filter(v -> equalsCompare.test(v, o)).toList();
        var lastValue = values.get(values.size() - 1);
        //hope for the best with the index first
        return Objects.requireNonNullElse(this.toIndexLast, this.toIndexFirst).applyAsInt(lastValue);
    }

    @NotNull
    @Override
    public ListIterator<M> listIterator() {
        return listIterator(0);
    }

    @NotNull
    @Override
    public ListIterator<M> listIterator(int index) {
        return new ListStreamIterator<>(this, index);
    }

    @NotNull
    @Override
    public List<M> subList(int fromIndex, int toIndex) {
        return this.stream().skip(fromIndex).limit(toIndex).toList();
    }
}
