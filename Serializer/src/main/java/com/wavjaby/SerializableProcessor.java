package com.wavjaby;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static javax.lang.model.element.ElementKind.FIELD;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes("com.wavjaby.Serializable")
public class SerializableProcessor extends AbstractProcessor {
    private static final List<String> serializedClassId = new ArrayList<>();
    private static final String serializerClassName = "com.wavjaby.Serializer";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty())
            return false;
        annotations.forEach(annotation -> roundEnv.getElementsAnnotatedWith(annotation).forEach(this::generateFile));

        String packageName = "com.wavjaby";
        String className = "SerialClassId";
        StringBuilder codeOut = new StringBuilder();
        codeOut.append("package ").append(packageName).append(";\n");
        codeOut.append("import java.lang.reflect.Method;\n");
        codeOut.append(format(" public abstract class %s{\n", className));
        codeOut.append(" public static final Method[] serialClassId;\n");
        codeOut.append(" static{\n");
        codeOut.append("  Method[] _serialClassId=new Method[0];\n");
        codeOut.append("  try{\n");
        codeOut.append("   _serialClassId = new Method[]{");
        for (String name : serializedClassId) {
            codeOut.append(name).append(".class.getMethod(\"deserialize\", byte[].class),");
        }
        codeOut.append("   };\n");
        codeOut.append("  }catch(NoSuchMethodException e){e.printStackTrace();}\n");
        codeOut.append("  serialClassId=_serialClassId;\n");
        codeOut.append(" }\n");
        codeOut.append("}\n");
        try (Writer out = processingEnv.getFiler().createSourceFile(packageName + "." + className).openWriter()) {
            out.write(codeOut.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("serializer process done");
        return true;
    }

    private void generateFile(Element element) {
        String className = element.getSimpleName().toString();
        String packageName = element.getEnclosingElement().toString();
        String serializerName = className + "Serializer";
        String builderFullName = packageName + "." + serializerName;
        String classFullName = packageName + "." + className;
        List<? extends Element> fields = element.getEnclosedElements()
                .stream().filter(e -> FIELD.equals(e.getKind())).toList();

        int classId = serializedClassId.size();

        int staticDataLen = 4;
        StringBuilder codeOut = new StringBuilder();
        StringBuilder encodeCache = new StringBuilder(), encodeDynamicCache = new StringBuilder(),
                decodeCache = new StringBuilder(), constructorCache = new StringBuilder();

        for (Element field : fields) {
            TypeKind type = field.asType().getKind();
            String fieldName = field.getSimpleName().toString();
            // Create constructor arguments
            if (!constructorCache.isEmpty()) constructorCache.append(',');
            constructorCache.append('_').append(fieldName);

            String typeName = type.name().toLowerCase();
            String typeNameUp = type.name().charAt(0) + typeName.substring(1);
            switch (type) {
                case FLOAT -> {
                    encodeCache.append("  ").append(setFloat(staticDataLen, "obj." + fieldName));
                    decodeCache.append(format("  %s _%s = %s;\n", typeName, fieldName, getFloat(staticDataLen)));
                    staticDataLen += 4;
                }
                case LONG -> {
                    encodeCache.append("  ").append(setLong(staticDataLen, "obj." + fieldName));
                    decodeCache.append(format("  %s _%s = %s;\n", typeName, fieldName, getLong(staticDataLen)));
                    staticDataLen += 8;
                }
                case INT -> {
                    encodeCache.append("  ").append(setInt(staticDataLen, "obj." + fieldName));
                    decodeCache.append(format("  %s _%s = %s;\n", typeName, fieldName, getInt(staticDataLen)));
                    staticDataLen += 4;
                }
                case SHORT -> {
                    encodeCache.append(format("  set%s(obj.%s,data,%d);\n", typeNameUp, fieldName, staticDataLen));
                    decodeCache.append(format("  %s _%s = get%s(data,%d);\n", typeName, fieldName, typeNameUp, staticDataLen));
                    staticDataLen += 2;
                }
                case BYTE -> {
                    encodeCache.append("  ").append(setByte(staticDataLen, "obj." + fieldName));
                    decodeCache.append(format("  %s _%s = %s;\n", typeName, fieldName, getByte(staticDataLen)));
                    staticDataLen += 1;
                }
                case BOOLEAN -> {
                    encodeCache.append("  ").append(setByte(staticDataLen, "(obj." + fieldName + "?1:0)"));
                    decodeCache.append(format("  %s _%s = %s==1?true:false;\n", typeName, fieldName, getByte(staticDataLen)));
                    staticDataLen += 1;
                }
                case ARRAY -> {
                    TypeKind arrType = ((ArrayType) field.asType()).getComponentType().getKind();
                    int sh = 0;
                    switch (arrType) {
                        case LONG, DOUBLE:
                            sh += 1;
                        case INT, FLOAT:
                            sh += 1;
                        case SHORT:
                            sh += 1;
                        case BYTE, BOOLEAN:
                            String arrTypeName = arrType.name().toLowerCase();
                            String arrTypeNameUp = arrType.name().charAt(0) + arrTypeName.substring(1);
                            encodeCache.append(format("  int _%sLen = obj.%s==null?-1:obj.%s.length;\n", fieldName, fieldName, fieldName));
                            encodeCache.append("  ").append(setInt(staticDataLen, "_" + fieldName + "Len"));
                            encodeDynamicCache.append(format("  write%sArray(dataOut,obj.%s);\n", arrTypeNameUp, fieldName));

                            decodeCache.append(format("  %s[] _%s = read%sArray(data,dataOffset,%s);\n", arrTypeName, fieldName, arrTypeNameUp, getInt(staticDataLen)));
                            if (arrType == TypeKind.BYTE)
                                decodeCache.append(format("  if(_%s!=null)dataOffset += _%s.length;\n", fieldName, fieldName));
                            else
                                decodeCache.append(format("  if(_%s!=null)dataOffset += _%s.length<<%d;\n", fieldName, fieldName, sh));
                            break;

                        case DECLARED:
                            Class<?> c = getClass(((DeclaredType) ((ArrayType) field.asType()).getComponentType()).asElement());
                            if (c == String.class) {
                                encodeCache.append("  ").append(setInt(staticDataLen, "obj." + fieldName + ".length"));
                                encodeDynamicCache.append(format("  for(int i=0;i<obj.%s.length;i++){\n", fieldName));
                                encodeDynamicCache.append(format("   byte[] _%s = obj.%s[i]==null?null:obj.%s[i].getBytes(StandardCharsets.UTF_8);\n", fieldName, fieldName, fieldName));
                                encodeDynamicCache.append(format("   int _%sLen = _%s==null?-1:_%s.length;\n", fieldName, fieldName, fieldName));
                                encodeDynamicCache.append("   ").append(addDynamicInt("_" + fieldName + "Len"));
                                encodeDynamicCache.append(format("   if(_%s!=null)dataOut.write(_%s,0,_%s.length);\n  }\n", fieldName, fieldName, fieldName));


                                decodeCache.append(format("  String[] _%s = new String[%s];\n", fieldName, getInt(staticDataLen)));
                                decodeCache.append(format("  for(int i=0;i<_%s.length;i++){\n", fieldName));
                                decodeCache.append(format("   int _%sLen = %s;\n", fieldName, getInt("dataOffset")));
                                decodeCache.append(format("   _%s[i] = _%sLen==-1?null:new String(data,dataOffset+4,_%sLen,StandardCharsets.UTF_8);\n", fieldName, fieldName, fieldName));
                                decodeCache.append(format("   dataOffset += 4+(_%sLen==-1?0:_%sLen);\n  }\n", fieldName, fieldName));
                                break;
                            }
                        default:
                            processingEnv.getMessager().printMessage(ERROR, "Array " + field.asType() + " not support", field);
                    }
                    staticDataLen += 4;
                }
                case DECLARED -> {
                    Class<?> c = getClass(field);
                    // Read/Write string
                    if (c != null && c.equals(String.class)) {
                        // Add string
                        encodeCache.append(format("  byte[] _%s = obj.%s==null?null:obj.%s.getBytes(StandardCharsets.UTF_8);\n", fieldName, fieldName, fieldName));
                        encodeCache.append(format("  int _%sLen = _%s==null?-1:_%s.length;\n", fieldName, fieldName, fieldName));
                        encodeCache.append("  ").append(setInt(staticDataLen, "_" + fieldName + "Len"));
                        encodeDynamicCache.append(format("  if(_%s!=null)dataOut.write(_%s,0,_%s.length);\n", fieldName, fieldName, fieldName));

                        // Get string
                        decodeCache.append(format("  int _%sLen = %s;\n", fieldName, getInt(staticDataLen)));
                        decodeCache.append(format("  String _%s = _%sLen==-1?null:new String(data,dataOffset,_%sLen,StandardCharsets.UTF_8);\n", fieldName, fieldName, fieldName));
                        decodeCache.append(format("  if(_%sLen!=-1)dataOffset += _%sLen;\n", fieldName, fieldName));
                        staticDataLen += 4;
                        break;
                    }

                    processingEnv.getMessager().printMessage(ERROR, "Class " + field.asType() + " is not serializable", field);
                }
                default -> processingEnv.getMessager().printMessage(ERROR, "Type " + field.asType() + " not support", field);
            }
        }

        codeOut.append("package ").append(packageName).append(";\n");
        codeOut.append("import java.nio.ByteBuffer;\n");
        codeOut.append("import java.nio.charset.StandardCharsets;\n");
        codeOut.append("import java.io.ByteArrayOutputStream;\n");
        codeOut.append("import java.io.IOException;\n");
        codeOut.append("import ").append(classFullName).append(";\n");
        codeOut.append("import static com.wavjaby.ArrayWriter.*;\n");
        codeOut.append(format(" public abstract class %s implements %s{\n", serializerName, serializerClassName));
        codeOut.append(format(" private static final int staticDataLength = %d;\n", staticDataLen));
        codeOut.append(format(" private static final int serialId = %d;\n", classId));

        // Serialize function
        codeOut.append(" public byte[] serialize() throws IOException {\n");
        codeOut.append(format("  %s obj = (%s)this;\n", className, className));
        codeOut.append(format("  byte[] data = new byte[%d];\n", staticDataLen));
        encodeCache.append("  ").append(setInt(0, "serialId"));
        codeOut.append(encodeCache);
        codeOut.append("  ByteArrayOutputStream dataOut = new ByteArrayOutputStream();\n");
        codeOut.append("  dataOut.write(data,0,data.length);\n");
        codeOut.append(encodeDynamicCache);
        codeOut.append("  return dataOut.toByteArray();\n");
        codeOut.append(" }\n");

        // Deserialize function
        codeOut.append(format(" public static %s deserialize(byte[] data) {\n", className));
        codeOut.append("  int serialId = ").append(getInt(0)).append(";\n");
        codeOut.append("  int dataOffset = staticDataLength;\n");
        codeOut.append(decodeCache);
        codeOut.append(format("  return new %s(%s);\n", className, constructorCache));
        codeOut.append(" }\n");

        codeOut.append("}\n");

        String str = codeOut.toString();
        serializedClassId.add(classFullName);

        try (Writer out = processingEnv.getFiler().createSourceFile(builderFullName).openWriter()) {
            out.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Class<?> getClass(Element field) {
        try {
            return Class.forName(field.asType().toString());
        } catch (ClassNotFoundException e) {
            processingEnv.getMessager().printMessage(ERROR, e.toString(), field);
        }
        return null;
    }

    private String setByte(int off, String valueName) {
        return "data[" + off + "]=(byte)" + valueName + ";\n";
    }

    private String getByte(int off) {
        return "data[" + (off) + "]";
    }

    private String setShort(int off, String valueName) {
        return "data[" + (off) + "]=(byte)(" + valueName + ">>24);" +
                "data[" + (off + 1) + "]=(byte)(" + valueName + ">>16);" +
                "data[" + (off + 2) + "]=(byte)(" + valueName + ">>8);" +
                "data[" + (off + 3) + "]=(byte)" + valueName + ";\n";
    }

    private String getShort(int off) {
        return "(data[" + (off) + "]<<24)&0xFF000000|" +
                "(data[" + (off + 1) + "]<<16)&0xFF0000|" +
                "(data[" + (off + 2) + "]<<8)&0xFF00|" +
                "data[" + (off + 3) + "]&0xFF";
    }

    private String setInt(int off, String valueName) {
        return "data[" + (off) + "]=(byte)(" + valueName + ">>24);" +
                "data[" + (off + 1) + "]=(byte)(" + valueName + ">>16);" +
                "data[" + (off + 2) + "]=(byte)(" + valueName + ">>8);" +
                "data[" + (off + 3) + "]=(byte)" + valueName + ";\n";
    }

    private String getInt(int off) {
        return "(data[" + (off) + "]<<24)&0xFF000000|" +
                "(data[" + (off + 1) + "]<<16)&0xFF0000|" +
                "(data[" + (off + 2) + "]<<8)&0xFF00|" +
                "data[" + (off + 3) + "]&0xFF";
    }

    private String getInt(String off) {
        return "(data[" + (off) + "]<<24)&0xFF000000|" +
                "(data[" + (off) + "+1]<<16)&0xFF0000|" +
                "(data[" + (off) + "+2]<<8)&0xFF00|" +
                "data[" + (off) + "+3]&0xFF";
    }

    private String setLong(int off, String valueName) {
        return "data[" + (off) + "]=(byte)(" + valueName + ">>56);" +
                "data[" + (off + 1) + "]=(byte)(" + valueName + ">>48);" +
                "data[" + (off + 2) + "]=(byte)(" + valueName + ">>40);" +
                "data[" + (off + 3) + "]=(byte)(" + valueName + ">>32);" +

                "data[" + (off + 4) + "]=(byte)(" + valueName + ">>24);" +
                "data[" + (off + 5) + "]=(byte)(" + valueName + ">>16);" +
                "data[" + (off + 6) + "]=(byte)(" + valueName + ">>8);" +
                "data[" + (off + 7) + "]=(byte)" + valueName + ";\n";
    }

    private String getLong(int off) {
        return "((data[" + (off + 1) + "]&0xFFL)<<56)|" +
                "((data[" + (off + 2) + "]&0xFFL)<<48)|" +
                "((data[" + (off + 3) + "]&0xFFL)<<40)|" +
                "((data[" + (off + 4) + "]&0xFFL)<<32)|" +

                "((data[" + (off + 5) + "]&0xFF)<<24)|" +
                "((data[" + (off + 6) + "]&0xFF)<<16)|" +
                "((data[" + (off + 7) + "]&0xFF)<<8)|" +
                "data[" + (off + 8) + "]&0xFF";
    }

    private String setFloat(int off, String valueName) {
        return "int _" + valueName + "B = Float.floatToIntBits(" + valueName + ");" +
                setInt(off, "_" + valueName + "B") + "\n";
    }

    private String getFloat(int off) {
        return "Float.intBitsToFloat(" + getInt(off) + ")";
    }

    private String addDynamicInt(String valueName) {
        return "dataOut.write((byte)(" + valueName + ">>24));" +
                "dataOut.write((byte)(" + valueName + ">>16));" +
                "dataOut.write((byte)(" + valueName + ">>8));" +
                "dataOut.write((byte)" + valueName + ");\n";
    }
}
