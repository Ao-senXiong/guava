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

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Predicate;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.pico.qual.Immutable;
import org.checkerframework.checker.pico.qual.Readonly;
import org.checkerframework.checker.pico.qual.ReceiverDependentMutable;

/**
 * An interface for all filtered multimap types.
 *
 * @author Louis Wasserman
 */
@GwtCompatible
@ElementTypesAreNonnullByDefault
@ReceiverDependentMutable
interface FilteredMultimap<K extends @Nullable @Immutable Object, V extends @Nullable @Readonly Object>
    extends Multimap<K, V> {
  Multimap<K, V> unfiltered();

  Predicate<? super Entry<K, V>> entryPredicate();
}
