package com.wavjaby.serializer;


import com.wavjaby.serializer.processor.Serializable;
import lombok.AllArgsConstructor;

@Serializable
@AllArgsConstructor
public class PrimitiveTypes extends PrimitiveTypesSerializer {
    public final boolean z;
    public final byte b;
    public final char c;
    public final short s;
    public final int i;
    public final long l;
    public final float f;
    public final double d;
}