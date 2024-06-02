package com.wavjaby.serializer;

import java.io.IOException;

public interface Serializable {
    byte[] serialize() throws IOException;
}
