plugins {
    application
}

group = "com.wavjaby"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("JavaWorldAdapter/build/libs/JavaWorldAdapter-1.0-SNAPSHOT.jar"))
//    implementation(project("JavaWorldAdapter"))

    implementation(project("Serializer"))
    annotationProcessor(project("Serializer"))

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