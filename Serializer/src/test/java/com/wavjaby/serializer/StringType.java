package com.wavjaby.serializer;

import com.wavjaby.serializer.processor.Serializable;
import lombok.AllArgsConstructor;

@Serializable
@AllArgsConstructor
public class StringType extends StringTypeSerializer {
    public final long a;
    public final String str;
    public final String[] strNull;
    public final String[] strEmpty;
    public final String[] strArr;
    public final long b;
}
