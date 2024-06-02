package com.wavjaby.javaworld.data;

import com.wavjaby.Serializable;
import lombok.AllArgsConstructor;

@Serializable
@AllArgsConstructor
public class PlayerCodeUpload extends PlayerCodeUploadSerializer {
    public final String sourceCode;
}
