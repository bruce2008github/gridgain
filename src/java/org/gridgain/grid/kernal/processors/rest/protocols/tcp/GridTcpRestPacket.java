// Copyright (C) GridGain Systems Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */
package org.gridgain.grid.kernal.processors.rest.protocols.tcp;

import org.gridgain.client.message.*;
import org.gridgain.grid.typedef.internal.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Request.
 *
 * @author 2012 Copyright (C) GridGain Systems
 * @version 4.0.0c.22032012
 */
class GridTcpRestPacket implements GridClientMessage {
    /** Random UUID used for memcached clients authentication. */
    private static final UUID MEMCACHED_ID = UUID.randomUUID();

    /** Header length. */
    public static final int HDR_LEN = 24;

    /** Flags length. */
    public static final byte FLAGS_LENGTH = 4;

    /** Memcache client request flag. */
    public static final byte MEMCACHE_REQ_FLAG = (byte)0x80;

    /** Response flag. */
    public static final byte MEMCACHE_RES_FLAG = (byte)0x81;

    /** Custom client request flag. */
    public static final byte GRIDGAIN_REQ_FLAG = (byte)0x90;

    /** Success status. */
    public static final int SUCCESS = 0x0000;

    /** Key not found status. */
    public static final int KEY_NOT_FOUND = 0x0001;

    /** Failure status. */
    public static final int FAILURE = 0x0004;

    /** Serialized flag. */
    public static final int SERIALIZED_FLAG = 1;

    /** Boolean flag. */
    public static final int BOOLEAN_FLAG = (1 << 8);

    /** Integer flag. */
    public static final int INT_FLAG = (2 << 8);

    /** Long flag. */
    public static final int LONG_FLAG = (3 << 8);

    /** Date flag. */
    public static final int DATE_FLAG = (4 << 8);

    /** Byte flag. */
    public static final int BYTE_FLAG = (5 << 8);

    /** Float flag. */
    public static final int FLOAT_FLAG = (6 << 8);

    /** Double flag. */
    public static final int DOUBLE_FLAG = (7 << 8);

    /** Byte array flag. */
    public static final int BYTE_ARR_FLAG = (8 << 8);

    /** Request flag. */
    private byte reqFlag;

    /** Operation code. */
    private byte opCode;

    /** Key length. */
    private short keyLength;

    /** Extras length. */
    private byte extrasLength;

    /** Status. */
    private int status;

    /** Total body length. */
    private int totalLength;

    /** Opaque. */
    private byte[] opaque;

    /** Extras. */
    private byte[] extras;

    /** Key. */
    private Object key;

    /** Value. */
    private Object val;

    /** Value to add/subtract */
    private Long delta;

    /** Initial value for increment and decrement commands. */
    private Long init;

    /** Expiration time. */
    private Long expiration;

    /** Cache name. */
    private String cacheName;

    /**
     * Creates empty packet which will be filled in parser.
     */
    GridTcpRestPacket() {
    }

    /**
     * Creates copy of request packet for easy response construction.
     *
     * @param req Source request packet.
     */
    GridTcpRestPacket(GridTcpRestPacket req) {
        assert req != null;

        reqFlag = req.reqFlag;
        opCode = req.opCode;

        opaque = new byte[req.opaque.length];
        System.arraycopy(req.opaque, 0, opaque, 0, req.opaque.length);
    }

    /** {@inheritDoc} */
    @Override public long requestId() {
        return U.bytesToInt(opaque, 0);
    }

    /** {@inheritDoc} */
    @Override public void requestId(long reqId) {
        U.intToBytes((int)reqId, opaque, 0);
    }

    /** {@inheritDoc} */
    @Override public UUID clientId() {
        return MEMCACHED_ID;
    }

    /** {@inheritDoc} */
    @Override public void clientId(UUID id) {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public byte[] sessionToken() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public void sessionToken(byte[] sesTok) {
        // No-op.
    }

    /**
     * @return Request flag.
     */
    public byte requestFlag() {
        return reqFlag;
    }

    /**
     * @param reqFlag Request flag.
     */
    public void requestFlag(byte reqFlag) {
        this.reqFlag = reqFlag;
    }

    /**
     * @return Operation code.
     */
    public byte operationCode() {
        return opCode;
    }

    /**
     * @param opCode Operation code.
     */
    public void operationCode(byte opCode) {
        assert opCode >= 0;

        this.opCode = opCode;
    }

    /**
     * @return Key length.
     */
    public short keyLength() {
        return keyLength;
    }

    /**
     * @param keyLength Key length.
     */
    public void keyLength(short keyLength) {
        assert keyLength >= 0;

        this.keyLength = keyLength;
    }

    /**
     * @return Extras length.
     */
    public byte extrasLength() {
        return extrasLength;
    }

    /**
     * @param extrasLength Extras length.
     */
    public void extrasLength(byte extrasLength) {
        assert extrasLength >= 0;

        this.extrasLength = extrasLength;
    }

    /**
     * @return Status.
     */
    public int status() {
        return status;
    }

    /**
     * @param status Status.
     */
    public void status(int status) {
        this.status = status;
    }

    /**
     * @return Total length.
     */
    public int totalLength() {
        return totalLength;
    }

    /**
     * @param totalLength Total length.
     */
    public void totalLength(int totalLength) {
        assert totalLength >= 0;

        this.totalLength = totalLength;
    }

    /**
     * @return Opaque.
     */
    public byte[] opaque() {
        return opaque;
    }

    /**
     * @param opaque Opaque.
     */
    public void opaque(byte[] opaque) {
        assert opaque != null;

        this.opaque = opaque;
    }

    /**
     * @return Extras.
     */
    public byte[] extras() {
        return extras;
    }

    /**
     * @param extras Extras.
     */
    public void extras(byte[] extras) {
        assert extras != null;

        this.extras = extras;
    }

    /**
     * @return Key.
     */
    public Object key() {
        return key;
    }

    /**
     * @param key Key.
     */
    public void key(Object key) {
        assert key != null;

        this.key = key;
    }

    /**
     * @return Value.
     */
    public Object value() {
        return val;
    }

    /**
     * @param val Value.
     */
    public void value(Object val) {
        assert val != null;

        this.val = val;
    }

    /**
     * @return Expiration.
     */
    @Nullable public Long expiration() {
        return expiration;
    }

    /**
     * @param expiration Expiration.
     */
    public void expiration(long expiration) {
        this.expiration = expiration;
    }

    /**
     * @return Delta for increment and decrement commands.
     */
    @Nullable public Long delta() {
        return delta;
    }

    /**
     * @param delta Delta for increment and decrement commands.
     */
    public void delta(long delta) {
        this.delta = delta;
    }

    /**
     * @return Initial value for increment and decrement commands.
     */
    @Nullable public Long initial() {
        return init;
    }

    /**
     * @param init Initial value for increment and decrement commands.
     */
    public void initial(long init) {
        this.init = init;
    }

    /**
     * @return Cache name.
     */
    @Nullable public String cacheName() {
        return cacheName;
    }

    /**
     * @param cacheName Cache name.
     */
    public void cacheName(String cacheName) {
        assert cacheName != null;

        this.cacheName = cacheName;
    }

    /**
     * @return Whether request MUST have flags in extras.
     */
    public boolean hasFlags() {
        return opCode == 0x01 ||
            opCode == 0x02 ||
            opCode == 0x03 ||
            opCode == 0x11 ||
            opCode == 0x12 ||
            opCode == 0x13;
    }

    /**
     * @return Whether request has expiration field.
     */
    public boolean hasExpiration() {
        return opCode == 0x01 ||
            opCode == 0x02 ||
            opCode == 0x03 ||
            opCode == 0x11 ||
            opCode == 0x12 ||
            opCode == 0x13;
    }

    /**
     * @return Whether request has delta field.
     */
    public boolean hasDelta() {
        return opCode == 0x05 ||
            opCode == 0x06 ||
            opCode == 0x15 ||
            opCode == 0x16;
    }

    /**
     * @return Whether request has initial field.
     */
    public boolean hasInitial() {
        return opCode == 0x05 ||
            opCode == 0x06 ||
            opCode == 0x15 ||
            opCode == 0x16;
    }

    /**
     * @return Whether to add data to response.
     */
    public boolean addData() {
        return opCode == 0x00 ||
            opCode == 0x05 ||
            opCode == 0x06 ||
            opCode == 0x09 ||
            opCode == 0x0B ||
            opCode == 0x0C ||
            opCode == 0x0D ||
            opCode == 0x20 ||
            opCode == 0x24 ||
            opCode == 0x25 ||
            opCode == 0x26 ||
            opCode == 0x27 ||
            opCode == 0x28 ||
            opCode == 0x29;
    }

    /**
     * @return Whether to add flags to response.
     */
    public boolean addFlags() {
        return opCode == 0x00 ||
            opCode == 0x09 ||
            opCode == 0x0C ||
            opCode == 0x0D;
    }
}
