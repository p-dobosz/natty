plugins {
    id("java-library")
    id("maven-publish")
}

group = "pl.chunky"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.nats:jnats:2.25.3")
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("org.apache.fury:fury-core:0.10.3")
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}