# WebMorph Bootstrap

**WebMorph** is a [Spring](https://docs.spring.io/spring-framework/reference/) bootstrap that delivers deep,
fine-grained runtime control via [ClassTransform](https://github.com/Lenni0451/Classtransform) and [Mixin](https://github.com/SpongePowered/Mixin) - no forks, no hacks required.

<p align="center">
<a href="https://github.com/web-morph/bootstrap?tab=LGPL-3.0-1-ov-file"><img alt="License" src="https://img.shields.io/github/license/web-morph/bootstrap"></a>
<a href="https://docs.gradle.org/8.14/release-notes.html"><img src="https://img.shields.io/badge/Gradle-8.14-brightgreen.svg?colorB=469C00&logo=gradle"></a>
<a href="https://repo.billmarssoft.com/api/maven/latest/file/releases/com/github/webmorph/bootstrap?extension=jar" target="_blank"><img alt="Download" src="https://repo.billmarssoft.com/api/badge/latest/releases/com/github/webmorph/bootstrap"></a>
</p>

---

## ⚙️ Requirements

* Java 17 or above

## 📦 Installation

⚙️ Gradle (Kotlin DSL – build.gradle.kts)

```kts
plugins {
    id("java")
    id("com.gradleup.shadow").version("8.3.6")
    id("io.spring.dependency-management").version("1.1.7")
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
    implementation("com.github.webmorph:bootstrap:<version>")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        manifest {
            attributes(
                "Main-Class" to "com.example.project.Application" // << Main class here
            )
        }
        mergeServiceFiles()
        append("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
        append("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.replacements")
    }
    build {
        dependsOn(shadowJar)
    }
    withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }
    jar {
        enabled = false
    }
}
```

⚙️ Gradle (Groovy DSL – build.gradle)

```groovy
plugins {
    id 'java'
    id 'com.github.johnrengelman.shadow' version '8.3.6'
    id 'io.spring.dependency-management' version '1.1.7'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
    all {
        exclude module: 'spring-boot-starter-logging'
        exclude group: 'ch.qos.logback'
    }
}

repositories {
    mavenCentral()
    maven {
        url 'https://repo.billmarssoft.com/public/'
    }
}


dependencies {
    implementation "com.github.webmorph:bootstrap:<version>"
}

tasks.named('shadowJar') {
    archiveClassifier.set('')
    manifest {
        attributes 'Main-Class': 'com.example.project.Application' // << Main class here
    }
    mergeServiceFiles()
    append 'META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports'
    append 'META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.replacements'
}

tasks.named('build') {
    dependsOn tasks.named('shadowJar')
}

tasks.withType(JavaCompile).configureEach {
    options.compilerArgs += '-parameters'
}

tasks.named('jar') {
    enabled = false
}
```

### ⚠️ Critical Note

It is strongly discouraged to use the spring-boot-gradle-plugin with WebMorph.
This plugin packages your application using a nested JAR format (BOOT-INF/...), which breaks classpath visibility and
makes runtime transformation via Mixin/ClassTransform impossible.

Instead, you must use the Shadow Gradle plugin, as shown in the example below. It produces a flat, fully compatible fat
JAR.

## 🧰 Usage

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        // If you're using mixins, register your mixin package before calling the bootstrap
        MixinTransformerRegistrationEvent.<MixinTransformerRegistrationEvent>on(event ->
                event.addTransformer("org.example.package.mixins.**")
        );

        // This will bootstrap WebMorph (including Spring context and mixin system)
        ApplicationContext context = WebMorph.bootstrap(args);
    }
}
```

## License

### This project is licensed under the LGPL-3.0-only License.

See the [LICENSE.md](LICENSE.md) file for details.

## Author

### [CKATEPTb](https://github.com/CKATEPTb), [fakeivchenko](https://github.com/fakeivchenko)

Feel free to open issues and submit pull requests to improve the library!