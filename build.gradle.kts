var javaVersion = 17;
group = "com.github.webmorph"
version = "1.0.2"

plugins {
    id("java-library")
    id("maven-publish")
    id("io.spring.dependency-management").version("1.1.7")
    id("io.github.gradle-nexus.publish-plugin").version("1.1.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    all {
        exclude(module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback")
    }
}

repositories {
    mavenCentral()
    maven("https://repo.billmarssoft.com/public/")
}

dependencies {
    // Mixin
    api("net.lenni0451.classtransform:core:1.14.1")
    annotationProcessor("net.lenni0451.classtransform:mixinsdummy:1.14.1")
    api("net.lenni0451.classtransform:mixinsdummy:1.14.1")
    api("net.lenni0451.classtransform:mixinstranslator:1.14.1")
    api("net.lenni0451.classtransform:additionalclassprovider:1.14.1")
    api("net.lenni0451:Reflect:1.5.0")

    // Spring
    api("org.springframework.boot:spring-boot-starter:3.5.0")

    // Event
    api("com.github.webmorph:eventbus:1.0.3")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
}

tasks {
    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }
    register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from(javadoc)
    }
    javadoc {
        options.encoding = "UTF-8"
        options.memberLevel = JavadocMemberLevel.PUBLIC
        isFailOnError = false
    }
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(javaVersion)
    }
    build {
        dependsOn("sourcesJar", "javadocJar")
    }
    jar {
        enabled = true
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
    repositories {
        maven {
            name = "BillmarsSoft"
            url = uri("https://repo.billmarssoft.com/releases/")
            credentials {
                username = System.getenv("REPOSITORY_USERNAME")
                password = System.getenv("REPOSITORY_PASSWORD")
            }
        }
    }
}