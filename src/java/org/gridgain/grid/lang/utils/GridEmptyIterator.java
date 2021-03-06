// Copyright (C) GridGain Systems Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.lang.utils;

import org.gridgain.grid.lang.*;
import org.gridgain.grid.typedef.internal.*;
import java.util.*;

/**
 * Empty iterator.
 *
 * @author 2012 Copyright (C) GridGain Systems
 * @version 4.0.0c.22032012
 */
public class GridEmptyIterator<T> extends GridIteratorAdapter<T> {
    /** {@inheritDoc} */
    @Override public boolean hasNext() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public T next() {
        throw new NoSuchElementException("Iterator is empty.");
    }

    /** {@inheritDoc} */
    @Override public void remove() {
        throw new NoSuchElementException("Iterator is empty.");
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridEmptyIterator.class, this);
    }               
}