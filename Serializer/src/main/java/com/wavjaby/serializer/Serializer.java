package com.wavjaby.serializer;

import java.io.IOException;

public interface Serializer {
    byte[] serialize() throws IOException;
}
