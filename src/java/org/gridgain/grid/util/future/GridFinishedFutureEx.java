// Copyright (C) GridGain Systems Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.util.future;

import org.gridgain.grid.*;
import org.gridgain.grid.lang.*;
import org.gridgain.grid.typedef.*;
import org.gridgain.grid.typedef.internal.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.concurrent.*;

/**
 * Future that is completed at creation time. This future is different from
 * {@link GridFinishedFuture} as it does not take context as a parameter and
 * performs notifications in the same thread.
 *
 * @author 2012 Copyright (C) GridGain Systems
 * @version 4.0.0c.22032012
 */
public class GridFinishedFutureEx<T> extends GridMetadataAwareAdapter implements GridFuture<T>, Externalizable {
    /** Complete value. */
    private T t;

    /** Error. */
    private Throwable err;

    /** Start time. */
    private final long startTime = System.currentTimeMillis();

    /**
     * Created finished future with {@code null} value.
     */
    public GridFinishedFutureEx() {
        t = null;
        err = null;
    }

    /**
     * Creates finished future with complete value.
     *
     * @param t Finished value.
     */
    public GridFinishedFutureEx(T t) {
        this.t = t;

        err = null;
    }

    /**
     * @param err Future error.
     */
    public GridFinishedFutureEx(Throwable err) {
        this.err = err;

        t = null;
    }

    /** {@inheritDoc} */
    @Override public long startTime() {
        return startTime;
    }

    /** {@inheritDoc} */
    @Override public long duration() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override public boolean concurrentNotify() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public void concurrentNotify(boolean concurNotify) {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public void syncNotify(boolean syncNotify) {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public boolean syncNotify() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public T call() throws Exception {
        return get();
    }

    /** {@inheritDoc} */
    @Override public boolean cancel() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean isCancelled() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean isDone() {
        return true;
    }

    /** {@inheritDoc} */
    @Override public GridAbsPredicate predicate() {
        return F.alwaysTrue().curry(null);
    }

    /** {@inheritDoc} */
    @Override public T get() throws GridException {
        if (err != null)
            throw U.cast(err);

        return t;
    }

    /** {@inheritDoc} */
    @Override public T get(long timeout) throws GridException {
        return get();
    }

    /** {@inheritDoc} */
    @Override public T get(long timeout, TimeUnit unit) throws GridException {
        return get();
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"unchecked"})
    @Override public void listenAsync(GridInClosure<? super GridFuture<T>> lsnr) {
        if (lsnr != null)
            lsnr.apply(this);
    }

    /** {@inheritDoc} */
    @Override public void stopListenAsync(@Nullable GridInClosure<? super GridFuture<T>>... lsnr) {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(t);
        out.writeObject(err);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"unchecked"})
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        t = (T)in.readObject();
        err = (Throwable)in.readObject();
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridFinishedFutureEx.class, this);
    }
}
