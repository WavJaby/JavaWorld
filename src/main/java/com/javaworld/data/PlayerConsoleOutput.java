package com.javaworld.data;

import com.wavjaby.serializer.Serializable;
import lombok.AllArgsConstructor;

@Serializable
@AllArgsConstructor
public class PlayerConsoleOutput extends PlayerConsoleOutputSerializer {
    public final String log;
    public final String error;
}
