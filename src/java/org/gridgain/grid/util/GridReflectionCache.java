// Copyright (C) GridGain Systems Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.util;

import org.gridgain.grid.*;
import org.gridgain.grid.lang.*;
import org.gridgain.grid.lang.utils.*;
import org.gridgain.grid.lang.utils.GridConcurrentLinkedHashMap.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import static org.gridgain.grid.GridSystemProperties.*;

/**
 * Reflection field and method cache for classes.
 *
 * @author 2012 Copyright (C) GridGain Systems
 * @version 4.0.0c.22032012
 */
public class GridReflectionCache implements Externalizable {
    /** Compares fields by name. */
    private static final Comparator<Field> FIELD_NAME_COMPARATOR = new Comparator<Field>() {
        @Override public int compare(Field f1, Field f2) {
            return f1.getName().compareTo(f2.getName());
        }
    };

    /** Compares methods by name. */
    private static final Comparator<Method> METHOD_NAME_COMPARATOR = new Comparator<Method>() {
        @Override public int compare(Method m1, Method m2) {
            return m1.getName().compareTo(m2.getName());
        }
    };

    /** Cache size. */
    private static final int CACHE_SIZE = Integer.getInteger(GG_REFLECTION_CACHE_SIZE, 128);

    /** Predicate to evict from maps. */
    private static final GridPredicate2<?, ?> PRED = new GridPredicate2<GridConcurrentLinkedHashMap<?, ?>,
        HashEntry<?, ?>>() {
        @Override public boolean apply(GridConcurrentLinkedHashMap<?, ?> map, HashEntry<?, ?> e) {
            return map.sizex() > CACHE_SIZE;
        }
    };

    /** Fields cache. */
    private ConcurrentMap<Class, List<Field>> fields = new GridConcurrentLinkedHashMap<Class, List<Field>>(
        CACHE_SIZE, 0.75f, 16, true,
        (GridPredicate2<GridConcurrentLinkedHashMap<Class, List<Field>>, HashEntry<Class, List<Field>>>)PRED);

    /** Methods cache. */
    private ConcurrentMap<Class, List<Method>> mtds = new GridConcurrentLinkedHashMap<Class, List<Method>>(
        CACHE_SIZE, 0.75f, 16, true,
        (GridPredicate2<GridConcurrentLinkedHashMap<Class, List<Method>>, HashEntry<Class, List<Method>>>)PRED);

    /** Field predicate. */
    private GridPredicate<Field> fp;

    /** Method predicate. */
    private GridPredicate<Method> mp;

    /**
     * Reflection cache without any method or field predicates.
     */
    public GridReflectionCache() {
        // No-op.
    }

    /**
     * Reflection cache with specified field and method predicates.
     * @param fp Field predicate.
     * @param mp Method predicate.
     */
    public GridReflectionCache(@Nullable GridPredicate<Field> fp, @Nullable GridPredicate<Method> mp) {
        this.fp = fp;
        this.mp = mp;
    }

    /**
     * Gets field value for object.
     *
     * @param o Key to get affinity key for.
     * @return Value of the field for given object or {@code null} if field was not found.
     * @throws GridException If failed.
     */
    @Nullable public Object firstFieldValue(Object o) throws GridException {
        assert o != null;

        Field f = firstField(o.getClass());

        if (f != null) {
            try {
                return f.get(o);
            }
            catch (IllegalAccessException e) {
                throw new GridException("Failed to access field for object [field=" + f + ", obj=" + o + ']', e);
            }
        }

        return null;
    }

    /**
     * Gets method return value for object.
     *
     * @param o Key to get affinity key for.
     * @return Method return value for given object or {@code null} if method was not found.
     * @throws GridException If failed.
     */
    @Nullable public Object firstMethodValue(Object o) throws GridException {
        assert o != null;

        Method m = firstMethod(o.getClass());

        if (m != null) {
            try {
                return m.invoke(o);
            }
            catch (IllegalAccessException e) {
                throw new GridException("Failed to invoke method for object [mtd=" + m + ", obj=" + o + ']', e);
            }
            catch (InvocationTargetException e) {
                throw new GridException("Failed to invoke method for object [mtd=" + m + ", obj=" + o + ']', e);
            }
        }

        return null;
    }

    /**
     * Gets first field in the class list of fields.
     *
     * @param cls Class.
     * @return First field.
     */
    @Nullable public Field firstField(Class<?> cls) {
        assert cls != null;

        List<Field> l = fields(cls);

        return l.isEmpty() ? null : l.get(0);
    }

    /**
     * Gets first method in the class list of methods.
     *
     * @param cls Class.
     * @return First method.
     */
    @Nullable public Method firstMethod(Class<?> cls) {
        assert cls != null;

        List<Method> l = methods(cls);

        return l.isEmpty() ? null : l.get(0);
    }

    /**
     * Gets fields.
     *
     * @param cls Class.
     * @return Annotated field.
     */
    public List<Field> fields(Class<?> cls) {
        assert cls != null;

        List<Field> fieldsList = fields.get(cls);

        if (fieldsList == null) {
            fieldsList = new ArrayList<Field>();

            for (Class<?> c = cls; c != null && !c.equals(Object.class); c = c.getSuperclass()) {
                List<Field> l = new ArrayList<Field>();

                for (Field f : c.getDeclaredFields()) {
                    if (fp == null || fp.apply(f)) {
                        f.setAccessible(true);

                        l.add(f);
                    }
                }

                if (!l.isEmpty()) {
                    Collections.sort(l, FIELD_NAME_COMPARATOR);

                    fieldsList.addAll(l);
                }
            }

            fields.putIfAbsent(cls, fieldsList);
        }

        return fieldsList;
    }


    /**
     * Gets methods.
     *
     * @param cls Class.
     * @return Annotated method.
     */
    public List<Method> methods(Class<?> cls) {
        assert cls != null;

        List<Method> mtdsList = mtds.get(cls);

        if (mtdsList == null) {
            mtdsList = new ArrayList<Method>();

            for (Class<?> c = cls; c != null && !c.equals(Object.class); c = c.getSuperclass()) {
                List<Method> l = new ArrayList<Method>();

                for (Method m : c.getDeclaredMethods()) {
                    if (mp == null || mp.apply(m)) {
                        m.setAccessible(true);

                        l.add(m);
                    }
                }

                if (!l.isEmpty()) {
                    Collections.sort(l, METHOD_NAME_COMPARATOR);

                    mtdsList.addAll(l);
                }
            }

            mtds.putIfAbsent(cls, mtdsList);
        }

        return mtdsList;
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(fp);
        out.writeObject(mp);
    }

    /** {@inheritDoc} */
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        fp = (GridPredicate<Field>)in.readObject();
        mp = (GridPredicate<Method>)in.readObject();
    }
}
