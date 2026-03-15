/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package com.alibaba.fastjson2.support.vertx;

import io.vertx.core.buffer.Buffer;

import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

/**
 * vertx 官方内置测试类改写
 */
public class TestUtils {
    /**
     * Creates a Buffer of random bytes.
     *
     * @param length The length of the Buffer
     * @return the Buffer
     */
    public static Buffer randomBuffer(int length) {
        return randomBuffer(length, false, (byte) 0);
    }

    /**
     * Create an array of random bytes
     *
     * @param length The length of the created array
     * @return the byte array
     */
    public static byte[] randomByteArray(int length) {
        return randomByteArray(length, false, (byte) 0);
    }

    /**
     * Create an array of random bytes
     *
     * @param length The length of the created array
     * @param avoid If true, the resulting array will not contain avoidByte
     * @param avoidByte A byte that is not to be included in the resulting array
     * @return an array of random bytes
     */
    public static byte[] randomByteArray(int length, boolean avoid, byte avoidByte) {
        byte[] line = new byte[length];
        if (avoid) {
            for (int i = 0; i < length; i++) {
                byte rand;
                do {
                    rand = randomByte();
                } while (rand == avoidByte);

                line[i] = rand;
            }
        } else {
            ThreadLocalRandom.current().nextBytes(line);
        }
        return line;
    }

    /**
     * Creates a Buffer containing random bytes
     *
     * @param length the size of the Buffer to create
     * @param avoid if true, the resulting Buffer will not contain avoidByte
     * @param avoidByte A byte that is not to be included in the resulting array
     * @return a Buffer of random bytes
     */
    public static Buffer randomBuffer(int length, boolean avoid, byte avoidByte) {
        byte[] line = randomByteArray(length, avoid, avoidByte);
        return Buffer.buffer(line);
    }

    /**
     * @return a random byte
     */
    public static byte randomByte() {
        return (byte) ((int) (Math.random() * 255) - 128);
    }

    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder decoder = Base64.getUrlDecoder();

    public static String toBase64String(byte[] bytes) {
        return encoder.encodeToString(bytes);
    }

    public static byte[] fromBase64String(String s) {
        return decoder.decode(s);
    }
}
