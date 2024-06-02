package com.javaworld.data;

import com.wavjaby.serializer.Serializable;
import lombok.AllArgsConstructor;

@Serializable
@AllArgsConstructor
public class PlayerCodeUpload extends PlayerCodeUploadSerializer {
    public final String sourceCode;
}
