plugins {
    id("com.github.johnrengelman.shadow") version "5.2.0"
    java
}

group = "com.openosrs"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.10")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.springframework.boot:spring-boot-starter-web:2.2.2.RELEASE")
    implementation("org.projectlombok:lombok:1.18.10")
    testCompile("junit", "junit", "4.12")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "service.SpringBootWebApplication"
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}