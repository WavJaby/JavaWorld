package com.javaworld.data;

import com.wavjaby.serializer.processor.Serializable;
import lombok.AllArgsConstructor;

@Serializable
@AllArgsConstructor
public class PlayerCodeData extends PlayerCodeDataSerializer {
    public final String sourceCode;
}
