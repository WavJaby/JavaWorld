package com.wavjaby;

import java.io.IOException;

public interface Serializer {
    byte[] serialize() throws IOException;
}
