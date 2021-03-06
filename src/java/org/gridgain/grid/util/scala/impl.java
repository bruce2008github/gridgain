// Copyright (C) GridGain Systems Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.util.scala;

import java.lang.annotation.*;

/**
 * Documentation annotation for Scala. Scaladoc doesn't support any means to indicate that
 * function implements an abstract function (from either trait or abstract class) and all its
 * documentation should be copied over.
 * <p>
 * This annotation allows to mark Scala function as an implementation for documentation and
 * readability purposes.
 *
 * @author 2012 Copyright (C) GridGain Systems
 * @version 4.0.0c.22032012
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
public @interface impl {
    // No-op.
}