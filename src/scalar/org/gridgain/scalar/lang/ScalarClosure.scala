// Copyright (C) GridGain Systems Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*
 * ________               ______                    ______   _______
 * __  ___/_____________ ____  /______ _________    __/__ \  __  __ \
 * _____ \ _  ___/_  __ `/__  / _  __ `/__  ___/    ____/ /  _  / / /
 * ____/ / / /__  / /_/ / _  /  / /_/ / _  /        _  __/___/ /_/ /
 * /____/  \___/  \__,_/  /_/   \__,_/  /_/         /____/_(_)____/
 *
 */
 
package org.gridgain.scalar.lang

import org.gridgain.grid.lang.GridClosure
import org.gridgain.grid.util.{GridUtils => U}

/**
 * Peer deploy aware adapter for Java's `GridClosure`.
 *
 * @author 2012 Copyright (C) GridGain Systems
 * @version 4.0.0c.22032012
 */
class ScalarClosure[E, R](private val f: E => R) extends GridClosure[E, R] {
    assert(f != null)

    peerDeployLike(U.peerDeployAware(f))

    /**
     * Delegates to passed in function.
     */
    def apply(e: E): R = {
        f(e)
    }
}