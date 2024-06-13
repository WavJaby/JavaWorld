package com.wavjaby.serializer;

import java.io.IOException;

/**
 * To make object serializable
 */
public interface Serializable {
    byte[] serialize() throws IOException;
}
