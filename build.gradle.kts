plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.wavjaby"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.register<Jar>("JavaWorldServer") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    includeEmptyDirs = false
    archiveBaseName = "JavaWorldServer"

    manifest.attributes(
        "Main-Class" to "com.javaworld.server.Server",
    )

    from(sourceSets.main.get().output)

    configurations["runtimeClasspath"].forEach { file: File ->
        println(file.path)
        if (file.nameWithoutExtension.startsWith("JavaWorldAdapter") ||
            file.nameWithoutExtension.startsWith("Serializer")
        )
            from(zipTree(file.absoluteFile))
        if (file.nameWithoutExtension.startsWith("fxgl-core") ||
            file.nameWithoutExtension.startsWith("javafx-graphics")
        ) {
            from(zipTree(file.absoluteFile).matching {
                include("**/fxgl/core/math/**")
                include("**/fxgl/core/pool/**")
                include("**/fxgl/animation/**")
                include("**/javafx/geom/**")
                include("**/javafx/geometry/**")
            })
        }
    }
}

javafx {
    version = "17.0.11"
    modules("javafx.graphics", "javafx.media")
}

dependencies {
    implementation("com.github.almasb:fxgl-core:17.3") {
        exclude("org.openjfx")
    }

    implementation(project(":JavaWorldAdapter"))

    implementation(project(":Serializer"))
    compileOnly(project(":SerializerProcessor"))
    annotationProcessor(project(":SerializerProcessor"))

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")

    testImplementation(platform("org.junit:junit-bom:5.11.0-M1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}