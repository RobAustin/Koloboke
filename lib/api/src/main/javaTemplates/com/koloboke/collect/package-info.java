/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * The root package of the collection library.
 *
 * // overview //
 * This library is a carefully designed and efficient extension of the Java Collections Framework
 * with primitive specializations and more.
 *
 * <h2>Overview</h2>
 * With a few exceptions, the library API consists of three types of classes:
 * <ol>
 *     <li>Interface hierarchy, which generally repeats and extends Java Collection Framework (JCF)
 *     interface and class hierarchy. Also, all interfaces are populated for all 7 primitive type
 *     specializations, and "object specialization" for API symmetry (see
 *     <a href="#primitive-naming">primitive specializations naming convention</a> for more
 *     information on this). For example, in JCF {@link java.util.HashSet} <i>class</i> extends
 *     {@link java.util.Set} extends {@link java.util.Collection}. In this library
 *     {@link com.koloboke.collect.set.hash.HashCharSet} <i>interface</i> extends
 *     {@link com.koloboke.collect.set.CharSet}, which extends {@link java.util.Set} and
 *     {@link com.koloboke.collect.CharCollection}, which extends
 *     {@link java.util.Collection} and {@link com.koloboke.collect.Container}.
 *
 *     <p>Also,// if !(JDK8 jdk) // a complete set of functional interfaces is defined
 *     in the {@code com.koloboke.function} package, it is needed because the library
 *     backports many methods from Java 8 Collections API (see <a href="#api-additions">API
 *     additions</a>), which employ these interfaces.// elif JDK8 jdk //
 *     the {@code com.koloboke.function} package polyfills {@link java.util.function}
 *     functional interface set with the rest specializations  ({@link java.util.function} package
 *     defines only some specializations for {@code int}, {@code long} and {@code double} types.)
 *     // endif //</li>
 *
 *     <li>Factory interfaces, each of them defines several dozens of factory methods which
 *     construct corresponding container interface instances. You can construct instances
 *     of three <a href="#mutability">mutability profiles</a>. Factory interfaces also form
 *     a hierarchy, which follow container's interface hierarchy, to ease making common
 *     configurations. For example, you can define a method which accept
 *     a {@link com.koloboke.collect.hash.HashContainerFactory} to configure all factories
 *     which produce hash sets and maps in the application.
 *
 *     <p>Note that all factories in the library are immutable, on changing any configuration a new
 *     copy of the factory is returned, with the target configuration changed.</li>
 *
 *     <li>Final uninstantiable classes for each "leaf" container and the corresponding factory
 *     interface, which define the same set of static methods as the factory interface does,
 *     all of them just delegate to the default factory instance, obtained via
 *     {@link java.util.ServiceLoader}. This default factory instance is returned
 *     by {@code getDefaultFactory()} static method in each static factory method holder class.
 *     These classes have a name of container interface plus {@code -s} suffix, for example,
 *     {@link com.koloboke.collect.map.hash.HashIntShortMaps} define static factory methods
 *     which return {@link com.koloboke.collect.map.hash.HashIntShortMap} instances,
 *     delegating to the default
 *     {@link com.koloboke.collect.map.hash.HashIntShortMapFactory} implementation.</li>
 * </ol>
 *
 * <h2><a name="jdk-equivalents"></a>Table of equivalents of JDK collection patterns</h2>
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1 summary="JDK equivalents">
 *     <tr>
 *         <th>JDK</th>
 *         <th>The closest equivalent from this library</th>
 *         <th>The recommended equivalent from this library</th>
 *     </tr>
 *     <tr valign=top>
 *         <td><pre> {@code new HashMap<String, String>();}</pre></td>
 *         <td><pre> {@code HashObjObjMaps.getDefaultFactory()
 *     .withNullKeyAllowed(true)
 *     .<String, String>newMutableMap();}</pre>
 *         </td>
 *         <td><pre> {@code HashObjObjMaps.<String, String>newUpdatableMap();}</pre></td>
 *     </tr>
 *     <tr valign=top>
 *         <td><pre> {@code // unclear how "capacity" (100)
 * // is translated to size. 50? 75? 100?
 * new HashMap<Integer, String>(100);}</pre></td>
 *
 *         <td><pre> {@code HashIntObjMaps.<String>newMutableMap(
 *     (int) (100 * HashConfig.getDefault().getTargetLoad()));}</pre></td>
 *         <td><pre> {@code // 50 is expected _size_
 * HashIntObjMaps.<String>newUpdatableMap(50);}</pre></td>
 *     </tr>
 *     <tr valign=top>
 *         <td><pre> {@code new IdentityHashMap<Object, Double>(map);}</pre></td>
 *         <td><pre> {@code HashObjDoubleMaps.getDefaultFactory()
 *     // these loads used in IdentityHashMap internally
 *     .withHashConfig(HashConfig.fromLoads(1./3., 2./3., 2./3.))
 *     .withNullKeyAllowed(true)
 *     .withKeyEquivalence(Equivalence.identity())
 *     .newMutableMap(map);}</pre></td>
 *         <td><pre> {@code HashObjDoubleMaps.getDefaultFactory()
 *     .withKeyEquivalence(Equivalence.identity())
 *     .newImmutableMap(map);}</pre></td>
 *     </tr>
 *     <tr valign=top>
 *         <td><pre> {@code Collections.unmodifiableSet(new HashSet<>() {{
 *     add("Summer");
 *     add("Autumn");
 *     add("Winter");
 *     add("Spring");
 * }});}</pre></td>
 *         <td colspan=2>
 * <pre> {@code HashObjSets.newImmutableSetOf("Summer", "Autumn", "Winter", "Spring");}</pre></td>
 *     </tr>
 * </table>
 *
 * <h2><a name="mutability">Mutability profiles</a></h2>
 * Container factories allow to construct containers with several distinct degrees of mutability.
 * It is useful for two main purposes: first, to defend your data from occasional (or intentional)
 * container misuse, i. e. the same purpose for what {@code Collections.unmodifiable*} methods
 * exist. Second, containers of lesser mutability are implemented in more efficient manner, whenever
 * possible. So using immutable collections when applicable could improve your application's
 * performance a bit.
 *
 * <h3>Immutable</h3>
 *
 * <p>Any operations that change the conceptual container state, e. g. insertions and removals,
 * as well as that could touch internal representation,
 * e. g. {@link com.koloboke.collect.Container#shrink()}, are disallowed. Other ones
 * are allowed.
 *
 * <h3>Updatable</h3>
 *
 * <p>Everything is allowed, except removals of <em>individual</em> elements (entries),
 * typically names of these operations include "remove" or "retain" verb. Emphasis on "individual"
 * means that {@link com.koloboke.collect.Container#clear()} (i. e. removal all elements or entries
 * at once) is allowed.
 *
 * <p>Think about updatable containers as "non-decreasing", which could be "reset"
 * from time to time by calling {@code clear()}.
 *
 * <p>In real practice individual removals are rarely needed, so most of the time you should use
 * updatable containers rather than fully mutable ones. On the other hand, prohibition of removals
 * permits faster implementation of {@linkplain com.koloboke.collect.hash.HashContainer hash
 * containers} and iterators over many data structures.
 *
 * <h3>Mutable</h3>
 *
 * <p>All operations are allowed.
 *
 * <p>In Koloboke Compile, the {@link com.koloboke.compile.mutability.Updatable @Updatable} and
 * {@link com.koloboke.compile.mutability.Mutable @Mutable} annotations specify that Koloboke
 * Compile should generate an updatable or a mutable implementation of a container interface,
 * respectively.
 *
 * <h3><a name="collection-mutability"></a>{@code Collection} mutability matrix</h3>
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 *   <caption>This matrix shows which methods in the {@link com.koloboke.collect.ObjCollection},
 *   {@link com.koloboke.collect.IntCollection}, etc. interfaces are supported by collections with
 *   different mutability profiles.</caption>
 *   <tr><td>Method \ Mutability</td><td>Mutable</td><td>Updatable</td><td>Immutable</td></tr>
 *   <tr><td>{@link java.util.Collection#size() size()}</td>
 *          <td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@link com.koloboke.collect.Container#sizeAsLong() sizeAsLong()}</td>
 *          <td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@link java.util.Collection#isEmpty() isEmpty()}</td>
 *          <td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@code contains(e)}</td>
 *          <td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@link java.util.Collection#containsAll(java.util.Collection) containsAll(c)}
 *     </td><td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@link java.util.Collection#toArray() toArray()}</td>
 *          <td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@link java.util.Collection#toArray(Object[]) toArray(array)}</td>
 *          <td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@link java.util.Collection#iterator() iterator()}</td>
 *          <td>✓</td><td colspan=2>✓, except {@link java.util.Iterator#remove()}</td></tr>
 *   <tr><td>{@code cursor()}</td>
 *          <td>✓</td><td colspan=2>✓, except {@link com.koloboke.collect.Cursor#remove()}</td>
 *          </tr>
 *   <tr><td>{@link com.koloboke.collect.Container#ensureCapacity(long) ensureCapacity(minSize)}
 *     </td><td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@link com.koloboke.collect.Container#shrink() shrink()}</td>
 *          <td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@link java.util.Collection#clear() clear()}</td>
 *          <td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@code add(e)}</td>
 *          <td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@link java.util.Collection#addAll(java.util.Collection) addAll(c)}</td>
 *          <td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@code remove(e)}</td>
 *          <td>✓</td><td>-</td><td>-</td></tr>
 *   <tr><td>{@link java.util.Collection#removeAll(java.util.Collection) removeAll(c)}</td>
 *          <td>✓</td><td>-</td><td>-</td></tr>
 *   <tr><td>{@link java.util.Collection#retainAll(java.util.Collection) retainAll(c)}</td>
 *          <td>✓</td><td>-</td><td>-</td></tr>
 *   <tr><td>{@code removeIf(filter)}</td><td>✓</td><td>-</td><td>-</td></tr>
 * </table>
 *
 * <h3><a name="map-mutability"></a>{@code Map} mutability matrix</h3>
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 *   <caption>This matrix shows which methods in the {@link com.koloboke.collect.map.ObjObjMap},
 *   {@link com.koloboke.collect.map.ObjIntMap}, {@link com.koloboke.collect.map.LongObjMap}, etc.
 *   interfaces are supported by maps with different mutability profiles.</caption>
 *   <tr><td>Method \ Mutability</td><td>Mutable</td><td>Updatable</td><td>Immutable</td></tr>
 *   <tr><td>{@link java.util.Map#size() size()}</td>
 *          <td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@link com.koloboke.collect.Container#sizeAsLong() sizeAsLong()}</td>
 *          <td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@link java.util.Map#isEmpty() isEmpty()}</td>
 *          <td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@code containsKey(key)}</td>
 *          <td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@code containsValue(value)}</td>
 *          <td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@code get(key)}</td>
 *          <td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@code getOrDefault(key, defaultValue)}</td>
 *          <td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@code forEach(action)}</td>
 *          <td>✓</td><td>✓</td><td>✓</td></tr>
 *   <tr><td>{@code cursor()}</td>
 *          <td>✓</td><td colspan=2>✓,
 *          except {@link com.koloboke.collect.Cursor#remove()}</td></tr>
 *   <tr><td>{@link java.util.Map#keySet() keySet()}</td>
 *          <td>✓</td><td colspan=2>✓, same mutability
 *          <a href="#collection-mutability">applied</a> to the returned {@code Set}</td></tr>
 *   <tr><td>{@link java.util.Map#values() values()}</td>
 *          <td>✓</td><td colspan=2>✓, same mutability <a
 *          href="#collection-mutability">applied</a> to the returned {@code Collection}</td></tr>
 *   <tr><td>{@link java.util.Map#entrySet() entrySet()}</td>
 *          <td>✓</td><td colspan=2>✓, same mutability
 *          <a href="#collection-mutability">applied</a> to the returned {@code Set},<br>
 *          if Immutable - additionally, {@link java.util.Map.Entry#setValue(java.lang.Object)
 *          Map.Entry.setValue(value)} not supported</td></tr>
 *   <tr><td>{@link com.koloboke.collect.Container#ensureCapacity(long) ensureCapacity(minSize)}
 *     </td><td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@link com.koloboke.collect.Container#shrink() shrink()}</td>
 *          <td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@link java.util.Map#clear() clear()}</td>
 *          <td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@code put(key, value)}</td>
 *          <td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@code putIfAbsent(key, value)}</td>
 *          <td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@code computeIfAbsent(key, mappingFunction)}</td>
 *          <td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@code replace(key, value)}</td>
 *          <td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@code replace(key, oldValue, newValue)}</td>
 *          <td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@link java.util.Map#putAll(java.util.Map) putAll(m)}</td>
 *          <td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@code replaceAll(function)}</td>
 *          <td>✓</td><td>✓</td><td>-</td></tr>
 *   <tr><td>{@code compute(key, remappingFunction)}</td>
 *          <td>✓</td><td>✓, except removing entry on returning {@code null} from
 *          the {@code remappingFunction}</td><td>-</td></tr>
 *   <tr><td>{@code computeIfPresent(key, remappingFunction)}</td>
 *          <td>✓</td><td>✓, except removing entry on returning {@code null} from
 *          the {@code remappingFunction}</td><td>-</td></tr>
 *   <tr><td>{@code merge(key, value, remappingFunction)}</td>
 *          <td>✓</td><td>✓, except removing entry on returning {@code null} from
 *          the {@code remappingFunction}</td><td>-</td></tr>
 *   <tr><td>{@code remove(key)}</td>
 *          <td>✓</td><td>-</td><td>-</td></tr>
 *   <tr><td>{@code remove(key, value)}</td>
 *          <td>✓</td><td>-</td><td>-</td></tr>
 *   <tr><td>{@code removeIf(filter)}</td>
 *          <td>✓</td><td>-</td><td>-</td></tr>
 * </table>
 *
 * <p>See other matrices for information if the concrete method is supported by the given
 * mutability profile: <a href="{@docRoot}/com/koloboke/collect/Container.html#mutability">
 * {@code Container}</a>.
 *
 * <h2><a name="iteration"></a>Comparison of iteration ways</h2>
 * In addition to the standard way// if JDK8 jdk //s// endif // &mdash;
 * {@linkplain java.util.Iterator iterators}// if JDK8 jdk // and {@code forEach()}-like methods
 * which accept closures// endif //, the library supplies {@linkplain
 * com.koloboke.collect.Cursor cursors} for every container
 * // if !(JDK8 jdk) // and {@code forEach()}-like methods which accept closures// endif //.
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 *   <caption>Overview comparison of the ways to iterate over containers within the library
 *   </caption>
 *   <tr>
 *     <td></td>
 *     <td>{@link java.util.Iterator}</td>
 *     <td>{@link com.koloboke.collect.Cursor}</td>
 *     <td>{@code forEach()}</td>
 *     <td>{@code forEachWhile()}</td>
 *     <td>{@code removeIf()}</td>
 *   </tr>
 *   <tr>
 *     <td>Available for {@link java.util.Collection} sub-interfaces in the library</td>
 *     <td colspan=5 align=center>Yes</td>
 *   </tr>
 *   <tr>
 *     <td>Available for {@link java.util.Map} sub-interfaces in the library</td>
 *     <td colspan=5 align=center>Yes</td>
 *   </tr>
 *   <tr>
 *     <td>Coding convenience</td>
 *     <td>High, if elements aren't removed and <em>generic</em> version
 *         of {@link java.util.Iterator#next()} method is used, Java "for-each" syntax
 *         is applicable. Medium otherwise.</td>
 *     <td>Medium</td>
 *     <td colspan=3>
 *         // if JDK8 jdk //High, lambda syntax// elif !(JDK8 jdk) //
 *         Low, nasty anonymous classes declarations// endif //</td>
 *   </tr>
 *   <tr>
 *     <td>Supports early break from the iteration</td>
 *     <td>Yes, by simple break from the loop</td>
 *     <td>Yes, by simple break from the loop</td>
 *     <td>No</td>
 *     <td>Yes, by returning {@code false}</td>
 *     <td>No</td>
 *   </tr>
 *   <tr>
 *     <td>Supports remove of iterated elements (entries)</td>
 *     <td>Yes, by {@link java.util.Iterator#remove()}</td>
 *     <td>Yes, by {@link com.koloboke.collect.Cursor#remove()}</td>
 *     <td>No</td>
 *     <td>No</td>
 *     <td>Yes, by returning {@code true}</td>
 *   </tr>
 *   <tr>
 *     <td>Performance, iteration over {@link java.util.Collection}</td>
 *     <td>High, if <em>specialized</em> version of {@link java.util.Iterator#next()} method
 *         is used. Medium otherwise, because every element is boxed.</td>
 *     <td colspan=4 align=center>Very high</td>
 *   </tr>
 *   <tr>
 *     <td>Performance, iteration over {@link java.util.Map}</td>
 *     <td>Medium, {@link java.util.Map.Entry} objects are allocated</td>
 *     <td colspan=4 align=center>Very high</td>
 *   </tr>
 * </table>
 *
 * <h2><a name="compatibility"></a>Compatibility with Java Collections Framework</h2>
 * All containers from the library have least possible (given initial design decisions) semantic
 * difference with the most widely used implementation from JCF of the same parental interface.
 * For example, {@link com.koloboke.collect.set.hash.HashCharSet} extends
 * {@code java.util.Set<Character>}, and made as similar as possible
 * to {@code java.util.HashSet<Character>}, which extends the same interface. Non-obvious things,
 * made compatible with JCF in the library:
 * <ul>
 *     // if !(JDK8 jdk) //
 *     <li>Library is made <i>forward-compatible</i> with Java 8 and it's own version for Java 8.
 *     This means that if you run the project on Java 6 or 7 and use this library, then move to
 *     Java 8 and simultaneously change the dependency to this library from the version for Java 6
 *     or 7 (you read documentation for this version now) to the version for Java 8, the project
 *     should compile and work <i>without changing the code</i> (if there are no compatibility
 *     issues related to other dependencies or JDK itself, of cause). If it doesn't compile or work,
 *     this situation is considered as a bug, please report about it.</li>
 *     // endif //
 *     <li>Containers of objects support {@code null} elements, keys and values, despite this is
 *     an antipattern, because most JCF implementations does. <em>Important:</em> hash maps and sets
 *     with {@code Object} keys,
 *     e. g. {@link com.koloboke.collect.map.hash.HashObjDoubleMap}, don't support
 *     {@code null} key by default, you should configure
 *{@code factory.}{@link com.koloboke.collect.hash.ObjHashFactory#withNullKeyAllowed(boolean
 *     ) withNullKeyAllowed(true)}.
 *     </li>
 *     <li>All containers try to detect concurrent access to themselves, if at least one thread
 *     modify the container structurally, and to throw
 *     {@link java.util.ConcurrentModificationException} on best-effort basis, i. e. they have
 *     <i>fail-fast semantics</i>. See documentation for {@code ConcurrentModificationException}
 *     for more information.</li>
 *     <li>Although {@link java.lang.Float#NaN} {@code != Float.NaN} (similarly for {@code Double})
 *     in Java, in this library in containers these values are treated consistently with their boxed
 *     versions (i. e. {@code new Float(Float.NaN)}, all such objects are equal to each other.</li>
 * </ul>
 *
 * <h3>Known incompatibilities</h3>
 * <ul>
 *     <li>Collections of primitives don't support {@code null} element, key or value. Obviously,
 *     this is by design and can't be fixed.</li>
 *     <li>Collections don't implement {@link java.lang.Cloneable} yet. To be fixed, see
 *     <a href="https://github.com/leventov/Koloboke/issues/14">the issue</a>.</li>
 *     <li>Collections don't implement {@link java.io.Serializable} yet. To be fixed, see
 *     <a href="https://github.com/leventov/Koloboke/issues/15">the issue</a>.</li>
 *     <li>Hash sets and maps with {@code byte}, {@code char} or {@code short} keys
 *     can't be complete, i. e. contain <i>all</i> keys of the type, unlike {@code HashSet<Byte>},
 *     {@code HashSet<Character>} and {@code HashSet<Short>}. There should be 1-2 absent keys.
 *     On attempt of insertion the last keys
 *     {@link com.koloboke.collect.hash.HashOverflowException} is thrown.</li>
 *     <li><i>It is not guaranteed</i> that any hash set or map implementation can hold more than
 *     250 millions of elements or entries. {@code HashOverflowException} is thrown on attempt
 *     of insertion an element or entry beyond the actual limit. {@code java.util.HashMap} and
 *     {@code java.util.HashSet} have higher limit, if there is enough heap space.</li>
 * </ul>
 *
 * <h2><a name="primitive-naming"></a>Primitive specializations naming convention</h2>
 * <ol>
 *     <li>The name of the specialized class is the name of the "basic" class with prefix equal
 *     to capitalized Java primitive type name of the element specialization, or key specialization
 *     type name followed value specialization type name without anything in between. Examples:
 *     {@link com.koloboke.collect.CharCollection} extends {@link java.util.Collection},
 *     {@link com.koloboke.collect.map.IntFloatMap} extends {@link java.util.Map}.
 *     There are also classes with {@code Obj-} prefix, they bring <a href="#api-additions">API
 *     additions</a> to collections of objects, if there are no additions for the class or
 *     interface, {@code Obj-} "specializations" are present anyway, for global API symmetry.</li>
 *     <li>If the specialized method has arguments of the specialized type, it has the same name
 *     as the non-specialized, thanks to Java's method overloading feature. There could be
 *     compilation issues in the client code, due to ambiguity, if there are several specialized
 *     arguments and some of them are boxed. You should "cast" them to unboxed values. For example:
 *     <pre> {@code
 * IntIntMap map = HashIntIntMaps.newUpdatableMap();
 * Integer key = 1;
 * map.put(key, 2); // ambiguous method call
 * map.put((int) key, 2); // correct}</pre>
 *     There is one exception from this rule:
 *     {@link com.koloboke.collect.ByteCollection#removeByte(byte)} is a specialized version
 *     of {@link java.util.Collection#remove(Object)}, but have a different name (the same
 *     for {@link com.koloboke.collect.LongCollection},
 *     {@link com.koloboke.collect.FloatCollection}, etc.
 *     for symmetry). This is because {@code remove(int)}
 *     in {@link com.koloboke.collect.IntCollection} will conflict with
 *     {@link java.util.List#remove(int)} method in {@code IntList} (not implemented yet, however).
 *     </li>
 *     <li>If the specialized method doesn't have arguments of the specialized types, but return
 *     the specialized type, capitalized primitive type name, optionally preceded by {@code -As-}
 *     infix, is added to the original method name. Examples: <ul>
 *         <li>{@link com.koloboke.collect.map.ObjCharMap#getChar(Object)}</li>
 *         <li>{@link com.koloboke.collect.map.ObjCharMap#removeAsChar(Object)}</li>
 *         <li>{@link com.koloboke.collect.CharIterator#nextChar()}</li>
 *         <li>{@link com.koloboke.function.ToCharFunction#applyAsChar(Object)}</li>
 *     </ul>
 *     </li>
 *     <li>Method {@link com.koloboke.collect.DoubleCollection#toDoubleArray()}, and others
 *     similar, is exceptional from those rules and have special name.</li>
 * </ol>
 *
 * <h2><a name="api-additions"></a>API additions beyond JCF interfaces</h2>
 * The library brings some extra functionality beyond implementing JCF interfaces and generating
 * primitive specializations for each interface and method.
 *
 * <h3>The concept of pluggable element (key, value) equivalences</h3>
 * JCF interfaces and implementations rely on Java built-in equality and hash code infrastructure:
 * {@link java.lang.Object#equals(Object)} and {@link java.lang.Object#hashCode()}. Container
 * factories in the library allow to configure {@linkplain com.koloboke.collect.Equivalence
 * equivalences} for elements, keys and values. This allows to implement some functionality very
 * easy, without defining new subclasses of the existing collections implementations. See
 * the documentation to {@link com.koloboke.collect.ObjCollection#equivalence()},
 * {@link com.koloboke.collect.map.ObjObjMap#keyEquivalence()} and
 * {@link com.koloboke.collect.map.ObjObjMap#valueEquivalence()} methods for more
 * information.
 *
 * <h3>Functional additions to {@link java.util.Collection} interface:</h3>
 * <ul>
 *     // if !(JDK8 jdk) //
 *     <li>{@link
 *     com.koloboke.collect.ObjCollection#forEach(com.koloboke.function.Consumer)}
 *     simply performs the given action for each element of the collection. This method
 *     is backported from Java 8 {@code Collection} API.</li>
 *     <li>{@link
 *     com.koloboke.collect.ObjCollection#removeIf(com.koloboke.function.Predicate)}
 *     removes all of the elements of this collection that satisfy the given predicate. This method
 *     is backported from Java 8 {@code Collection} API.</li>
 *     // endif //
 *     <li>{@link
 * com.koloboke.collect.ObjCollection#forEachWhile(com.koloboke.function.Predicate)}
 *     performs the given action for each element of the collection, while it returns {@code true}.
 *     </li>
 *     <li>Containers in the library support {@link com.koloboke.collect.Cursor} iteration:
 *     {@link com.koloboke.collect.ObjCollection#cursor()}. See also
 *     <a href="#iteration">the comparison of iteration ways</a> in the library.</li>
 * </ul>
 *
 * Of cause, there are appropriate specialized methods in the {@link java.util.Collection} interface
 * primitive specializations: {@link com.koloboke.collect.ByteCollection},
 * {@link com.koloboke.collect.CharCollection}, etc.
 *
 * <h3>Functional additions to {@link java.util.Map} interface:</h3>
 * <ul>
 *     // if !(JDK8 jdk) //
 *     <li>
 *         Methods, backported from Java 8 {@code Map} API:
 *         <ul>
 *          <li>{@link com.koloboke.collect.map.ObjObjMap#getOrDefault(Object, Object)}</li>
 *             <li>{@link
 *com.koloboke.collect.map.ObjObjMap#forEach(com.koloboke.function.BiConsumer)}</li>
 *             <li>{@link
 *com.koloboke.collect.map.ObjObjMap#replaceAll(com.koloboke.function.BiFunction)}
 *             </li>
 *           <li>{@link com.koloboke.collect.map.ObjObjMap#putIfAbsent(Object, Object)}</li>
 *             <li>{@link com.koloboke.collect.map.ObjObjMap#remove(Object, Object)}</li>
 *             <li>{@link com.koloboke.collect.map.ObjObjMap#replace(Object,
 *             Object, Object)}</li>
 *             <li>{@link com.koloboke.collect.map.ObjObjMap#replace(Object, Object)}</li>
 *             <li>{@link com.koloboke.collect.map.ObjObjMap#computeIfAbsent(Object,
 *             com.koloboke.function.Function)}</li>
 *             <li>{@link com.koloboke.collect.map.ObjObjMap#computeIfPresent(Object,
 *             com.koloboke.function.BiFunction)}</li>
 *             <li>{@link com.koloboke.collect.map.ObjObjMap#compute(Object,
 *             com.koloboke.function.BiFunction)}</li>
 *             <li>{@link com.koloboke.collect.map.ObjObjMap#merge(Object, Object,
 *             com.koloboke.function.BiFunction)}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         All-new methods:
 *         <ul>
 *     // endif //
 *             <li>{@link com.koloboke.collect.map.ObjObjMap#cursor()} &mdash; cursor
 *             iteration over maps.</li>
 *             <li>{@link
 *com.koloboke.collect.map.ObjObjMap#forEachWhile(com.koloboke.function.BiPredicate)
 *        } performs the given action for each entry of the map, while it returns {@code true}.</li>
 *             <li>{@link
 *   com.koloboke.collect.map.ObjObjMap#removeIf(com.koloboke.function.BiPredicate)}
 *             removes all of the entries of this map that satisfy the given predicate.</li>
 *             <li>{@link com.koloboke.collect.map.ObjIntMap#addValue(Object, int)} and
 *             {@link com.koloboke.collect.map.ObjIntMap#addValue(Object, int, int)} add
 *             the given value to the value associated to the given key. These methods are present
 *             in the map specializations with primitive value.</li>
 *     // if !(JDK8 jdk) //
 *         </ul>
 *     </li>
 *     // endif //
 * </ul>
 *
 * <h3>Additional control over hash table behaviour</h3>
 * The single thing in the API of JDK hash table implementations, including
 * {@link java.util.HashMap}, {@link java.util.LinkedHashMap}, {@link java.util.HashSet}
 * and {@link java.util.WeakHashMap}, that allows to control it's memory footprint and performance
 * characteristics, is {@code loadFactor} constructor argument. {@link java.util.IdentityHashMap}
 * don't have even this one. This library allows to tune hash tables very precisely via a bunch
 * of per-instance methods and factory configurations. See the documentation
 * to {@link com.koloboke.collect.hash.HashContainer} and
 * {@link com.koloboke.collect.hash.HashConfig} classes for more information.
 * // endOverview //
 */
package com.koloboke.collect;
