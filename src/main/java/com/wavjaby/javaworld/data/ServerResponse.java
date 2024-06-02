package com.wavjaby.javaworld.data;

import com.wavjaby.Serializable;
import lombok.AllArgsConstructor;

@Serializable
@AllArgsConstructor
public class ServerResponse extends ServerResponseSerializer {
    public final boolean success;
    public final String message;
}
