/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.WeakOuter;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import javax.annotation.CheckForNull;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;
import org.checkerframework.checker.pico.qual.Assignable;
import org.checkerframework.checker.pico.qual.Immutable;
import org.checkerframework.checker.pico.qual.Mutable;
import org.checkerframework.checker.pico.qual.PolyMutable;
import org.checkerframework.checker.pico.qual.Readonly;
import org.checkerframework.checker.pico.qual.ReceiverDependentMutable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.framework.qual.AnnotatedFor;
import org.checkerframework.framework.qual.CFComment;

/**
 * A skeleton {@code Multimap} implementation, not necessarily in terms of a {@code Map}.
 *
 * @author Louis Wasserman
 */
@AnnotatedFor({"nullness", "pico"})
@GwtCompatible
@ElementTypesAreNonnullByDefault
@ReceiverDependentMutable
abstract class AbstractMultimap<K extends @Nullable @Immutable Object, V extends @Nullable @Readonly Object>
    implements Multimap<K, V> {
  @Pure
  @Override
  public boolean isEmpty(@Readonly AbstractMultimap<K, V> this) {
    return size() == 0;
  }

  @Pure
  @Override
  public boolean containsValue(@Readonly AbstractMultimap<K, V> this, @CheckForNull @UnknownSignedness @Readonly Object value) {
    for (Collection<V> collection : asMap().values()) {
      if (collection.contains(value)) {
        return true;
      }
    }

    return false;
  }

  @Pure
  @Override
  public boolean containsEntry(@Readonly AbstractMultimap<K, V> this, @CheckForNull @Readonly Object key, @CheckForNull @Readonly Object value) {
    Collection<V> collection = asMap().get(key);
    return collection != null && collection.contains(value);
  }

  @CanIgnoreReturnValue
  @Override
  public boolean remove(@Mutable AbstractMultimap<K, V> this, @CheckForNull @Readonly Object key, @CheckForNull @Readonly Object value) {
    Collection<V> collection = asMap().get(key);
    return collection != null && collection.remove(value);
  }

  @CanIgnoreReturnValue
  @Override
  public boolean put(@Mutable AbstractMultimap<K, V> this, @ParametricNullness K key, @ParametricNullness V value) {
    return get(key).add(value);
  }

  @CanIgnoreReturnValue
  @Override
  public boolean putAll(@Mutable AbstractMultimap<K, V> this, @ParametricNullness K key, Iterable<? extends V> values) {
    checkNotNull(values);
    // make sure we only call values.iterator() once
    // and we only call get(key) if values is nonempty
    if (values instanceof Collection) {
      Collection<? extends V> valueCollection = (Collection<? extends V>) values;
      return !valueCollection.isEmpty() && get(key).addAll(valueCollection);
    } else {
      Iterator<? extends V> valueItr = values.iterator();
      return valueItr.hasNext() && Iterators.addAll(get(key), valueItr);
    }
  }

  @CanIgnoreReturnValue
  @Override
  public boolean putAll(@Mutable AbstractMultimap<K, V> this, Multimap<? extends K, ? extends V> multimap) {
    boolean changed = false;
    for (Entry<? extends K, ? extends V> entry : multimap.entries()) {
      changed |= put(entry.getKey(), entry.getValue());
    }
    return changed;
  }

  @CanIgnoreReturnValue
  @Override
  public Collection<V> replaceValues(@Mutable AbstractMultimap<K, V> this, @ParametricNullness K key, Iterable<? extends V> values) {
    checkNotNull(values);
    Collection<V> result = removeAll(key);
    putAll(key, values);
    return result;
  }

  @CFComment("Change to @LazyFinal later")
  @LazyInit @CheckForNull private transient @Assignable Collection<Entry<K, V>> entries;

  @SideEffectFree
  @Override
  public @PolyMutable Collection<Entry<K, V>> entries(@PolyMutable AbstractMultimap<K, V> this) {
    Collection<Entry<K, V>> result = entries;
    return (result == null) ? entries = createEntries() : result;
  }

  abstract @PolyMutable Collection<Entry<K, V>> createEntries(@PolyMutable AbstractMultimap<K, V> this);

  @WeakOuter
  @ReceiverDependentMutable
  class Entries extends Multimaps.Entries<K, V> {
    @Override
    Multimap<K, V> multimap() {
      return AbstractMultimap.this;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
      return entryIterator();
    }

    @Override
    public Spliterator<Entry<K, V>> spliterator() {
      return entrySpliterator();
    }
  }

  @WeakOuter
  @ReceiverDependentMutable
  class EntrySet extends Entries implements Set<Entry<K, V>> {
    @Pure
    @Override
    public int hashCode(@UnknownSignedness @Readonly EntrySet this) {
      return Sets.hashCodeImpl(this);
    }

    @Pure
    @Override
    public boolean equals(@Readonly EntrySet this, @CheckForNull @UnknownSignedness @Readonly Object obj) {
      return Sets.equalsImpl(this, obj);
    }
  }

  abstract Iterator<Entry<K, V>> entryIterator();

  Spliterator<Entry<K, V>> entrySpliterator() {
    return Spliterators.spliterator(
        entryIterator(), size(), (this instanceof SetMultimap) ? Spliterator.DISTINCT : 0);
  }

  @CFComment("Change to @LazyFinal later")
  @LazyInit @CheckForNull private transient @Assignable Set<K> keySet;

  @SideEffectFree
  @Override
  public @PolyMutable Set<K> keySet(@PolyMutable AbstractMultimap<K, V> this) {
    Set<K> result = keySet;
    return (result == null) ? keySet = createKeySet() : result;
  }

  @SideEffectFree
  abstract @PolyMutable Set<K> createKeySet(@PolyMutable AbstractMultimap<K, V> this);

  @CFComment("Change to @LazyFinal later")
  @LazyInit @CheckForNull private transient @Assignable Multiset<K> keys;

  @Override
  public @PolyMutable Multiset<K> keys(@PolyMutable AbstractMultimap<K, V> this) {
    Multiset<K> result = keys;
    return (result == null) ? keys = createKeys() : result;
  }

  abstract @PolyMutable Multiset<K> createKeys(@PolyMutable AbstractMultimap<K, V> this);

  @CFComment("Change to @LazyFinal later")
  @LazyInit @CheckForNull private transient @Assignable Collection<V> values;

  @SideEffectFree
  @Override
  public Collection<V> values() {
    Collection<V> result = values;
    return (result == null) ? values = createValues() : result;
  }

  abstract Collection<V> createValues();

  @WeakOuter
  @ReceiverDependentMutable
  class Values extends AbstractCollection<V> {
    @Override
    public Iterator<V> iterator(@Readonly Values this) {
      return valueIterator();
    }

    @Pure
    @Override
    public Spliterator<V> spliterator(@Readonly Values this) {
      return valueSpliterator();
    }

    @Override
    public @NonNegative int size(@Readonly Values this) {
      return AbstractMultimap.this.size();
    }

    @Pure
    @Override
    public boolean contains(@Readonly Values this, @CheckForNull @UnknownSignedness @Readonly Object o) {
      return AbstractMultimap.this.containsValue(o);
    }

    @Override
    public void clear(@Mutable Values this) {
      AbstractMultimap.this.clear();
    }
  }

  Iterator<V> valueIterator() {
    return Maps.valueIterator(entries().iterator());
  }

  Spliterator<V> valueSpliterator() {
    return Spliterators.spliterator(valueIterator(), size(), 0);
  }

  @LazyInit @CheckForNull private transient @Assignable Map<K, Collection<V>> asMap;

  @Override
  public @PolyMutable Map<K, Collection<V>> asMap(@PolyMutable AbstractMultimap<K, V> this) {
    Map<K, Collection<V>> result = asMap;
    return (result == null) ? asMap = createAsMap() : result;
  }

  abstract @PolyMutable Map<K, Collection<V>> createAsMap(@PolyMutable AbstractMultimap<K, V> this);

  // Comparison and hashing

  @Pure
  @Override
  public boolean equals(@Readonly AbstractMultimap<K, V> this, @CheckForNull @Readonly Object object) {
    return Multimaps.equalsImpl(this, object);
  }

  /**
   * Returns the hash code for this multimap.
   *
   * <p>The hash code of a multimap is defined as the hash code of the map view, as returned by
   * {@link Multimap#asMap}.
   *
   * @see Map#hashCode
   */
  @Pure
  @Override
  public int hashCode(@UnknownSignedness @Readonly AbstractMultimap<K, V> this) {
    return asMap().hashCode();
  }

  /**
   * Returns a string representation of the multimap, generated by calling {@code toString} on the
   * map returned by {@link Multimap#asMap}.
   *
   * @return a string representation of the multimap
   */
  @Pure
  @Override
  public String toString(@Readonly AbstractMultimap<K, V> this) {
    return asMap().toString();
  }
}
