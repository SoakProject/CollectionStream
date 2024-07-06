package org.mose.collection.stream.specific.list;

import org.jetbrains.annotations.NotNull;

import java.util.ListIterator;

public class ListStreamIterator<O, M> implements ListIterator<M> {

    private final @NotNull ListStream<O, M> list;
    private int index;

    ListStreamIterator(@NotNull ListStream<O, M> list, int index) {
        this.list = list;
        this.index = index;
    }


    @Override
    public boolean hasNext() {
        return this.list.size() <= this.index;
    }

    @Override
    public M next() {
        this.index++;
        return this.list.get(this.index);
    }

    @Override
    public boolean hasPrevious() {
        return this.index > 0;
    }

    @Override
    public M previous() {
        this.index--;
        return this.list.get(this.index);
    }

    @Override
    public int nextIndex() {
        return this.index + 1;
    }

    @Override
    public int previousIndex() {
        return this.index - 1;
    }

    @Override
    public void remove() {
        this.list.remove(this.index);
    }

    @Override
    public void set(M m) {
        this.list.set(this.index, m);
    }

    @Override
    public void add(M m) {
        this.list.add(this.index, m);
    }
}
