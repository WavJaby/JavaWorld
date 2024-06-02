package com.wavjaby.javaworld.data;

import com.wavjaby.Serializable;
import lombok.AllArgsConstructor;

@Serializable
@AllArgsConstructor
public class PlayerLogin extends PlayerLoginSerializer {
    final public int version;
    final public String name;
}
