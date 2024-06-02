package com.javaworld.data;

import com.wavjaby.serializer.processor.Serializable;
import lombok.AllArgsConstructor;

@Serializable
@AllArgsConstructor
public class ServerResponse extends ServerResponseSerializer {
    public final boolean success;
    public final String message;
}
