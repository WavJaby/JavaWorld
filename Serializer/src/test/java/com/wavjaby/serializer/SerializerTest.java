package com.wavjaby.serializer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializerTest {
    @Test
    void primitiveType() {
        PrimitiveTypes primitiveTypes = new PrimitiveTypes(
                true,
                (byte) 0b10101010,
                'c',
                (short) 0xABCD,
                0x89ABCDEF,
                0xFEDCBA9876543210L,
                0.12345f,
                0.123456789
        );
        byte[] data = primitiveTypes.serialize();
        PrimitiveTypes primitiveTypesResult = PrimitiveTypes.deserialize(data);

        assertEquals(true, primitiveTypesResult.z);
        assertEquals((byte) 0b10101010, primitiveTypesResult.b);
        assertEquals('c', primitiveTypesResult.c);
        assertEquals((short) 0xABCD, primitiveTypesResult.s);
        assertEquals(0x89ABCDEF, primitiveTypesResult.i);
        assertEquals(0xFEDCBA9876543210L, primitiveTypesResult.l);
        assertEquals(0.12345f, primitiveTypesResult.f);
        assertEquals(0.123456789, primitiveTypesResult.d);
    }

    @Test
    void primitiveTypeArray() {
        boolean[] z = new boolean[]{true, false};
        byte[] b = new byte[]{(byte) 0b10101010, (byte) 0b01010101};
        char[] c = new char[]{'c', 'h', 'a', 'r'};
        short[] s = new short[]{(short) 0xABCD, (short) 0xCDEF};
        int[] i = new int[]{0x89ABCDEF, 0x76543210};
        long[] l = new long[]{0xFEDCBA9876543210L, 0x0123456789ABCDEFL};
        float[] f = new float[]{0.12345f, 0.123456f};
        double[] d = new double[]{0.123456789, 0.987654321};
        PrimitiveTypesArray primitiveTypes = new PrimitiveTypesArray(z, b, c, s, i, l, f, d);
        byte[] data = primitiveTypes.serialize();
        PrimitiveTypesArray primitiveTypesResult = PrimitiveTypesArray.deserialize(data);

        assertArrayEquals(z, primitiveTypesResult.z);
        assertArrayEquals(b, primitiveTypesResult.b);
        assertArrayEquals(c, primitiveTypesResult.c);
        assertArrayEquals(s, primitiveTypesResult.s);
        assertArrayEquals(i, primitiveTypesResult.i);
        assertArrayEquals(l, primitiveTypesResult.l);
        assertArrayEquals(f, primitiveTypesResult.f);
        assertArrayEquals(d, primitiveTypesResult.d);
    }

    @Test
    void primitiveTypeArrayEmpty() {
        boolean[] z = new boolean[0];
        byte[] b = null;
        char[] c = new char[]{'c', 'h', 'a', 'r'};
        short[] s = new short[0];
        int[] i = new int[0];
        long[] l = new long[]{0xFEDCBA9876543210L, 0x0123456789ABCDEFL};
        float[] f = null;
        double[] d = new double[]{0.123456789, 0.987654321};
        PrimitiveTypesArray primitiveTypes = new PrimitiveTypesArray(z, b, c, s, i, l, f, d);
        byte[] data = primitiveTypes.serialize();
        PrimitiveTypesArray primitiveTypesResult = PrimitiveTypesArray.deserialize(data);

        assertArrayEquals(z, primitiveTypesResult.z);
        assertArrayEquals(b, primitiveTypesResult.b);
        assertArrayEquals(c, primitiveTypesResult.c);
        assertArrayEquals(s, primitiveTypesResult.s);
        assertArrayEquals(i, primitiveTypesResult.i);
        assertArrayEquals(l, primitiveTypesResult.l);
        assertArrayEquals(f, primitiveTypesResult.f);
        assertArrayEquals(d, primitiveTypesResult.d);
    }

    @Test
    void string() {
        StringType string = new StringType(
                0xFEDCBA9876543210L,
                "Hello World",
                null,
                new String[0],
                new String[]{"Hello", "World"},
                0x0123456789ABCDEFL
        );
        byte[] data = string.serialize();
        StringType stringResult = StringType.deserialize(data);

        assertEquals("Hello World", stringResult.str);
        assertEquals(null, stringResult.strNull);
        assertArrayEquals(new String[0], stringResult.strEmpty);
        assertArrayEquals(new String[]{"Hello", "World"}, stringResult.strArr);
        assertEquals("Hello World", stringResult.str);
        assertEquals(0xFEDCBA9876543210L, stringResult.a);
        assertEquals(0x0123456789ABCDEFL, stringResult.b);
    }
}
