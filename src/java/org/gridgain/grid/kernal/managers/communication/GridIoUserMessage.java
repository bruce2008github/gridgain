// Copyright (C) GridGain Systems Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal.managers.communication;

import org.gridgain.grid.*;
import org.gridgain.grid.lang.*;
import org.gridgain.grid.typedef.internal.*;
import org.gridgain.grid.lang.utils.*;
import org.gridgain.grid.util.tostring.*;
import java.io.*;
import java.util.*;

/**
 * User message wrapper.
 *
 * @author 2012 Copyright (C) GridGain Systems
 * @version 4.0.0c.22032012
 */
class GridIoUserMessage implements Serializable {
    /** */
    private final GridByteArrayList src;

    /** */
    private final GridUuid clsLdrId;

    /** */
    private final GridDeploymentMode depMode;

    /** */
    private final String srcClsName;

    /** */
    private final long seqNum;

    /** */
    private final String userVer;

    /** Node class loader participants. */
    @GridToStringInclude
    private Map<UUID, GridTuple2<GridUuid, Long>> ldrParties;

    /**
     * @param src Source message.
     * @param srcClsName Source message class name.
     * @param clsLdrId Class loader ID.
     * @param depMode Deployment mode.
     * @param seqNum Sequence number.
     * @param userVer User version.
     * @param ldrParties Node loader participant map.
     */
    GridIoUserMessage(
        GridByteArrayList src,
        String srcClsName,
        GridUuid clsLdrId,
        GridDeploymentMode depMode,
        long seqNum,
        String userVer,
        Map<UUID, GridTuple2<GridUuid, Long>> ldrParties) {
        this.src = src;
        this.srcClsName = srcClsName;
        this.depMode = depMode;
        this.seqNum = seqNum;
        this.clsLdrId = clsLdrId;
        this.userVer = userVer;
        this.ldrParties = ldrParties;
    }

    /**
     * @return Source message.
     */
    GridByteArrayList getSource() {
        return src;
    }

    /**
     * @return the Class loader ID.
     */
    public GridUuid getClassLoaderId() {
        return clsLdrId;
    }

    /**
     * @return Deployment mode.
     */
    public GridDeploymentMode getDeploymentMode() {
        return depMode;
    }

    /**
     * @return Source message class name.
     */
    public String getSourceClassName() {
        return srcClsName;
    }

    /**
     * @return Sequence number.
     */
    public long getSequenceNumber() {
        return seqNum;
    }

    /**
     * @return User version.
     */
    public String getUserVersion() {
        return userVer;
    }

    /**
     * @return Node class loader participant map.
     */
    public Map<UUID, GridTuple2<GridUuid, Long>> getLoaderParticipants() {
        return ldrParties != null ? Collections.unmodifiableMap(ldrParties) : null;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridIoUserMessage.class, this);
    }
}
