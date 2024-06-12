package com.javaworld.data;

import com.wavjaby.serializer.processor.Serializable;
import lombok.AllArgsConstructor;

@Serializable
@AllArgsConstructor
public class PlayerLoginData extends PlayerLoginSerializer {
    final public int version;
    final public String name;
}
