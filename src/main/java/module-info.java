module com.javaworld {
    requires static lombok;
    requires static com.wavjaby.serializer.processor;
    requires java.logging;
    requires java.compiler;
    requires com.wavjaby.serializer;

    requires com.almasb.fxgl.core;
    requires com.javaworld.adapter;

    opens com.javaworld.logging to java.logging;

    exports com.javaworld.client;
    exports com.javaworld.core.entity;
    exports com.javaworld.core.block;
}