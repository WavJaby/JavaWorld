package com.javaworld.data;

import com.wavjaby.serializer.Serializable;
import lombok.AllArgsConstructor;

@Serializable
@AllArgsConstructor
public class PlayerLogin extends PlayerLoginSerializer {
    final public int version;
    final public String name;
}
