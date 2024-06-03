module com.javaworld {
    requires static lombok;
    requires static com.wavjaby.serializer.processor;
    requires java.logging;
    requires java.compiler;
    requires com.almasb.fxgl.core;

    opens com.javaworld.logging to java.logging;
    requires com.wavjaby.serializer;
    requires com.javaworld.adapter;

    exports com.javaworld.client;
    exports com.javaworld.server;
    exports com.javaworld.core;
    exports com.javaworld.core.entity;
    exports com.javaworld.core.block;
    exports com.javaworld.core.update;
    exports com.javaworld.core.jwentities;
    exports com.javaworld.core.jwblocks;
    exports com.javaworld.data;
}