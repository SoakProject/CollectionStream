package org.mose.collection.stream.specific.set;

import org.jetbrains.annotations.NotNull;
import org.mose.collection.stream.AbstractCollectionStream;
import org.mose.collection.stream.builder.StreamCollectionBuilderImpl;

import java.util.Set;

public class SetStream<O, M> extends AbstractCollectionStream<O, M> implements Set<M> {
    public SetStream(@NotNull StreamCollectionBuilderImpl<O, M> builder, boolean isModifiable) {
        super(builder, isModifiable);
    }
}
