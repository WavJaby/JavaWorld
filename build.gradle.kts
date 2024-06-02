plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.wavjaby"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
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