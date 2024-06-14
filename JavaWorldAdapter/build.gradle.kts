plugins {
    `java-library`
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.wavjaby"
version = "1.0.0"

repositories {
    mavenCentral()
}

tasks.register<Jar>("JavaWorldSDK") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    includeEmptyDirs = false
    archiveBaseName = "JavaWorldSDK"
    destinationDirectory = file("../build/libs")

    from(sourceSets.main.get().output)

    configurations["runtimeClasspath"].forEach { file: File ->
        if (file.nameWithoutExtension.startsWith("fxgl-core") ||
            file.nameWithoutExtension.startsWith("javafx-graphics")
        ) {
//            println(file.nameWithoutExtension)
//            zipTree(file.absoluteFile).forEach { i ->
//                println(i.path)
//            }
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

    testImplementation(platform("org.junit:junit-bom:5.11.0-M1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}