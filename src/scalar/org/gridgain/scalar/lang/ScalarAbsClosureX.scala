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

import org.gridgain.grid.util.{GridUtils => U}
import org.gridgain.grid.lang._
import org.gridgain.grid._

/**
 * Peer deploy aware adapter for Java's `GridAbsClosureX`.
 *
 * @author 2011 Copyright (C) GridGain Systems
 * @version 3.6.0c.22122011
 */
class ScalarAbsClosureX(private val f: () => Unit) extends GridAbsClosureX {
    assert(f != null)

    peerDeployLike(U.peerDeployAware(f))

    /**
     * Delegates to passed in function.
     */
    @throws(classOf[GridException])
    def applyx() {
        f()
    }
}