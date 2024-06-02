plugins {
    id("java")
    id("java-library")
}

group = "com.wavjaby"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("../JavaWorldAdapter/build/libs/JavaWorldAdapter-1.0-SNAPSHOT.jar"))

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}