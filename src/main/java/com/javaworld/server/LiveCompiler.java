package com.javaworld.server;

import com.javaworld.adapter.PlayerApplication;
import com.javaworld.util.FilteredClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LiveCompiler {
    private static final Logger logger = Logger.getLogger(LiveCompiler.class.getSimpleName());
    private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    //    private static final Field outField, errField;
    private static final String isNotInterrupt;

    static {
//        Field outField_ = null, errField_ = null;
        Method isNotInterrupt_ = null;
        try {
//            outField_ = PlayerApplication.class.getDeclaredField("out");
//            outField_.setAccessible(true);
//            errField_ = PlayerApplication.class.getDeclaredField("err");
//            errField_.setAccessible(true);
            isNotInterrupt_ = PlayerApplication.class.getDeclaredMethod("isNotInterrupt");
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Could not find PlayerApplication console out", e);
        }
//        outField = outField_;
//        errField = errField_;
        isNotInterrupt = isNotInterrupt_ == null ? "true" : isNotInterrupt_.getName() + "()";
    }

    private final File adapterLib;

    public LiveCompiler() {
        adapterLib = new File("..\\JavaWorldAdapter\\build\\libs\\JWAdapter-1.0-SNAPSHOT.jar");
    }

    public CompiledResult compileCode(String source) {
        long start = System.currentTimeMillis();
        int classNameIndex = source.indexOf("class ");
        int classNameEndIndex = source.indexOf(' ', classNameIndex + 6);
        if (classNameEndIndex == -1) classNameEndIndex = source.indexOf('{', classNameIndex + 6);
        if (classNameIndex == -1 || classNameEndIndex == -1)
            return new CompiledResult(CompiledResult.ErrorCode.BAD_SOURCECODE, "Root class not found");
        String className = source.substring(classNameIndex + 6, classNameEndIndex);

        int packageNameIndex = source.indexOf("package ");
        int packageNameEndIndex = source.indexOf(';', packageNameIndex + 8);
        String fullClassName, packageName = "";
        if (packageNameIndex != -1 && packageNameEndIndex != -1) {
            packageName = source.substring(packageNameIndex + 8, packageNameEndIndex);
            fullClassName = packageName + "." + className;
        } else fullClassName = className;
        if (fullClassName.contains("javaworld"))
            return new CompiledResult(CompiledResult.ErrorCode.BAD_SOURCECODE, "Class name '" + fullClassName + "' not allowed");

        logger.info("Compiling: " + className + ".java");

        // Create temp folder and file
        String uuid = UUID.randomUUID().toString().replace("-", "");
        File sourceFile = new File(uuid + '/' + className + ".java");
        File dir = sourceFile.getParentFile();
        if (!dir.isDirectory()) {
            if (dir.exists() || !dir.mkdir())
                return new CompiledResult(CompiledResult.ErrorCode.SERVER_ERROR, "Failed create temporary folder: " + dir);
        }

        source = addLoopInterrupt(source);

        try {
            FileWriter writer = new FileWriter(sourceFile);
            writer.write(source);
            writer.close();
        } catch (IOException e) {
            if (!deleteDirectory(dir)) logger.warning("Failed to delete temporary folder: " + dir);
            return new CompiledResult(CompiledResult.ErrorCode.SERVER_ERROR, "Failed to create temporary file: " + dir);
        }

        // Compile source
        ByteArrayOutputStream errOut = new ByteArrayOutputStream();
//        char split = System.getProperty("os.name").contains("win") ? ';' : ':';
        String[] args = new String[]{
                "-d", dir.getAbsolutePath(),
                sourceFile.getAbsolutePath(),
                "-cp", adapterLib.getAbsolutePath()
        };
        int result = compiler.run(null, errOut, errOut, args);
        if (result == 0) {
            // Load class
            try {
                ClassLoader systemClassloader = ClassLoader.getSystemClassLoader();
                FilteredClassLoader classloader = new FilteredClassLoader(new URL[]{dir.toURI().toURL()}, systemClassloader, fullClassName);
                // Load playerApplication root class
                Class<?> playerRootClass;
                try {
                    playerRootClass = Class.forName(fullClassName, true, classloader);
                } catch (IllegalAccessError e) {
                    return new CompiledResult(CompiledResult.ErrorCode.BAD_SOURCECODE, e.toString());
                }
                // Check default constructor
                Object instance;
                try {
                    instance = playerRootClass.getDeclaredConstructor().newInstance();
                } catch (NoSuchMethodException e) {
                    deleteTempFolder(dir);
                    return new CompiledResult(CompiledResult.ErrorCode.BAD_SOURCECODE, "Default constructor not found: " + e.getMessage());
                }
                // Check class extend
                if (!(instance instanceof PlayerApplication playerApplication)) {
                    deleteTempFolder(dir);
                    return new CompiledResult(CompiledResult.ErrorCode.BAD_SOURCECODE, "Root class should extend '" + PlayerApplication.class.getName() + "'");
                }

                ByteArrayOutputStream out = playerApplication.out;
                ByteArrayOutputStream err = playerApplication.err;

                // Return result
                String message = "Compile success in " + (System.currentTimeMillis() - start) + "ms";
                logger.info(message);
                return new CompiledResult(message, classloader, playerApplication, out, err, dir);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to compile: '" + fullClassName + "'\n", e);
                deleteTempFolder(dir);
                return new CompiledResult(CompiledResult.ErrorCode.COMPILE_FAILED, "Unknown error when compile: '" + fullClassName + "'" + e.getMessage());
            }
        } else {
            // Compile error
            deleteTempFolder(dir);
            Pattern p = Pattern.compile(sourceFile.getAbsolutePath().replace("\\", "\\\\").replace(".", "\\.") +
                    ":([\\d:]+): ");
            StringBuilder err = new StringBuilder();
            Matcher m = p.matcher(errOut.toString());
//            Thread.currentThread().isInterrupted()
            while (m.find()) {
                m.appendReplacement(err, "    at " + fullClassName + "(" + className + ".java:" + m.group(1) + ") ");
            }
            m.appendTail(err);

            return new CompiledResult(CompiledResult.ErrorCode.COMPILE_FAILED, "Failed to compile:\n" + err);
        }
    }

    private static class LoopToken {
        final int index;
        final boolean start;
        boolean empty;

        public LoopToken(int index, boolean start) {
            this.index = index;
            this.start = start;
        }
    }

    public static String addLoopInterrupt(String source) {
        List<LoopToken> tokens = new ArrayList<>();
        if (findLoop(source, 0, 0, tokens) != -1) {
            int offset = 0;
            StringBuilder builder = new StringBuilder();
            for (LoopToken token : tokens) {
                builder.append(source, offset, token.index);
                if (token.start) builder.append(token.empty ? isNotInterrupt : isNotInterrupt + "&&(");
                else if (!token.empty) builder.append(')');
                offset = token.index;
            }
            builder.append(source, offset, source.length());
            return builder.toString();
        }
        return source;
    }

    private static int findLoop(String source, int index, int inFor, List<LoopToken> result) {
        Stack<Character> bracketStack = new Stack<>();

        while (index < source.length()) {
            // Skip string
            index = skipString(source, index);
            if (index == source.length()) return -1;

            char c = source.charAt(index);
            if (c == '(' || c == '[' || c == '{') {
                bracketStack.push(c);
            } else if (c == ')') {
                if (bracketStack.pop() != '(') return -1;
                if (bracketStack.isEmpty()) return index;
            } else if (c == ']') {
                if (bracketStack.pop() != '[') return -1;
            } else if (c == '}') {
                if (bracketStack.pop() != '{') return -1;
            } else if (inFor == 1 || inFor == 2) {
                if (bracketStack.size() == 1 && c == ';') {
                    if (bracketStack.pop() != '(') return -1;
                    if (bracketStack.isEmpty()) return index;
                }
                // Middle
                if (inFor == 2) {
                    if (bracketStack.isEmpty() && c == ';')
                        bracketStack.push('(');
                }
            } else if (inFor == 3) {
                // End
                if (bracketStack.isEmpty() && c == ';')
                    bracketStack.push('(');
            }

            // find while
            if (source.indexOf("while", index - 4) == index - 4 &&
                    (index - 5 < 0 || isOther(source.charAt(index - 5)))) {
                // Find start
                int whileStart = findNext('(', source, index + 1);
                if (whileStart == source.length()) return -1;
                if (whileStart != -1 && whileStart + 1 < source.length()) {
                    // While start
                    LoopToken start = new LoopToken(whileStart + 1, true);
                    result.add(start);

                    whileStart = findLoop(source, whileStart, 0, result);
                    if (whileStart == source.length() || whileStart == -1) return -1;

                    // While end
                    LoopToken end = new LoopToken(whileStart, false);
                    result.add(end);
                    // Empty while
                    if (source.substring(start.index, end.index).isBlank())
                        return -1;

                    index = whileStart;
                }
            }
            // find for
            if (source.indexOf("for", index - 2) == index - 2 &&
                    (index - 3 < 0 || isOther(source.charAt(index - 3)))) {
                // Find start
                int forIndex = findNext('(', source, index + 1);
                if (forIndex == source.length()) return -1;
                if (forIndex != -1 && forIndex + 1 < source.length()) {
                    // Init
                    forIndex = findLoop(source, forIndex, 1, result);
                    if (forIndex + 1 >= source.length() || forIndex == -1) return -1;

                    // Condition start
                    LoopToken start = new LoopToken(forIndex + 1, true);
                    result.add(start);
                    // Condition
                    forIndex = findLoop(source, forIndex, 2, result);
                    if (forIndex == source.length() || forIndex == -1) return -1;
                    // Condition end
                    LoopToken end = new LoopToken(forIndex, false);
                    result.add(end);
                    if (source.substring(start.index, end.index).isBlank())
                        start.empty = end.empty = true;


                    // Increment
                    forIndex = findLoop(source, forIndex, 3, result);
                    if (forIndex + 1 >= source.length() || forIndex == -1) return -1;

                    index = forIndex;
                }
            }
            index++;
        }

        return index;
    }

    private static int skipString(String source, int index) {
        if (index == source.length()) return index;
        char start = source.charAt(index);
        if (start != '\'' && start != '"')
            return index;
        char last = start;
        index++;
        while (index < source.length()) {
            char c = source.charAt(index);
            // Find end
            if (c == start && last != '\\') {
                index++;
                break;
            }
            last = c;
            index++;
        }
        return index;
    }

    private static int findNext(char bracket, String source, int index) {
        while ((index = skipString(source, index)) < source.length()) {
            char c = source.charAt(index);
            if (c == bracket) return index;
            else if (!Character.isWhitespace(c)) return -1;
            index++;
        }
        return index;
    }

    private static boolean isOther(char c) {
        if (Character.isWhitespace(c)) return true;
        return c < '0' || c > '9' && c < 'A' || c > 'Z' && c < 'a' || c > 'z' && c < 127;
    }

    public static void deleteTempFolder(File dir) {
        if (!deleteDirectory(dir))
            logger.warning("Failed to delete temporary folder: " + dir);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) for (File file : files) {
            if (file.isDirectory() && !deleteDirectory(file))
                return true;
            if (!file.delete())
                return true;
        }
        return dir.delete();
    }
}
