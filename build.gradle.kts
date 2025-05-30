import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val publishUsername: String by project
val publishPassword: String by project
val build: String by project

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
    id("io.izzel.taboolib") version "2.0.22"
    id("org.jetbrains.dokka") version "2.0.0"
}

taboolib {
    env {
        install(Basic)
        install(Bukkit)
        install(BukkitFakeOp)
        install(BukkitHook)
        install(BukkitNavigation)
        install(BukkitUI)
        install(BukkitUtil)
        install(BukkitNMS)
        install(BukkitNMSUtil)
        install(BukkitNMSItemTag)
        install(BukkitNMSDataSerializer)
        install(BukkitNMSEntityAI)
        install(MinecraftChat)
        install(MinecraftEffect)
        install(I18n)
        install(Metrics)
        install(CommandHelper)
        install(Database)
        install(Kether)
        install(Jexl)
    }
    description {
        name = "Orryx"
        desc("跨时代技能插件，支持实现复杂逻辑，为稳定高效而生")
        contributors {
            name("zhibei")
        }
        links {
            name("homepage").url("https://www.mcwar.cn/orryx")
        }
        dependencies {
            name("Adyeshach").optional(true)
            name("DragonCore").optional(true)
            name("DragonArmourers").optional(true)
            name("GermPlugin").optional(true)
            name("MythicMobs").optional(true)
            name("RedisChannel").optional(true)
            name("OriginAttribute").optional(true)
            name("AttributePlus").optional(true)
            name("packetevents").optional(true)
            name("ProtocolLib").optional(true)
            name("GDDTitle").optional(true)
            name("PlaceholderAPI").optional(true)
            name("GlowAPI").optional(true)
            name("DungeonPlus").optional(true)
        }
    }
    relocate("kotlinx.serialization", "org.gitee.orryx.serialization")
    relocate("org.apache.commons.jexl3", "org.gitee.orryx.jexl3")
    relocate("com.github.benmanes.caffeine", "org.gitee.orryx.caffeine")
    relocate("org.joml", "org.gitee.orryx.joml")
    relocate("com.larksuite.oapi", "org.gitee.orryx.larksuite.oapi")
    version {
        taboolib = "6.2.2"
        coroutines = "1.8.0"
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.tabooproject.org/repository/releases") }
    maven { url = uri("https://www.mcwar.cn/nexus/repository/maven-public/") }
}

dependencies {
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v12004:12004:universal")
    compileOnly("ink.ptms.core:v11200:11200")
    compileOnly("ink.ptms:nms-all:1.0.0")

    compileOnly("com.gitee.redischannel:RedisChannel:1.1:api")
    compileOnly("ink.ptms.adyeshach:plugin:2.0.26:api")
    compileOnly("org.eldergod.ext:DragonCore:2.6.2.9")
    compileOnly("org.eldergod.ext:GermPlugin:4.4.1-5")
    compileOnly("org.eldergod.ext:MythicMobs:4.11.0")
    compileOnly("org.eldergod.ext:DragonArmourers:6.72")
    compileOnly("org.eldergod.ext:GDDTitle:2.1")
    compileOnly("org.eldergod.ext:GlowAPI:1.4.6")
    compileOnly("org.eldergod.ext:OriginAttribute:1.1.4")
    compileOnly("org.eldergod.ext:AttributePlus:3.3.3.0")
    compileOnly("org.eldergod.ext:DungeonPlus:1.4.3")
    compileOnly("org.eldergod.ext:packetevents:2.7.0")
    compileOnly("org.eldergod.ext:ProtocolLib:5.3.0")

    taboo("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3") { isTransitive = false }
    taboo("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3") { isTransitive = false }
    taboo("org.apache.commons:commons-jexl3:3.4.0")
    compileOnly("com.github.ben-manes.caffeine:caffeine:2.9.3")
    compileOnly("org.joml:joml:1.10.7")
    compileOnly("com.larksuite.oapi:oapi-sdk:2.4.7")

    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("reflect"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    destinationDirectory.set(File(build))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://www.mcwar.cn/nexus/repository/maven-releases/")
            credentials {
                username = publishUsername
                password = publishPassword
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
            artifact(tasks["kotlinSourcesJar"]) {
                classifier = "sources"
            }
            artifact("${build}/${rootProject.name}-${version}-api.jar") {
                classifier = "api"
            }
            groupId = project.group.toString()
        }
    }
}

tasks.dokkaHtml {
    // 配置输出目录
    outputDirectory.set(file("${build}/doc"))
    // 配置模块名称
    moduleName.set("Orryx")
    // 禁用自动生成文档链接
    suppressObviousFunctions.set(false)
    dokkaSourceSets {
        named("main") {
            // 配置源代码链接（GitHub）
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(uri("https://github.com/zhibeigg/Orryx/tree/master/src/main/kotlin").toURL())
                remoteLineSuffix.set("#L")
            }
            // 添加外部文档链接（如 JDK）
            externalDocumentationLink {
                url.set(uri("https://docs.oracle.com/javase/8/docs/api/").toURL())
                packageListUrl.set(uri("https://docs.oracle.com/javase/8/docs/api/package-list").toURL())
            }
        }
        configureEach {
            // 包含/排除包
            includeNonPublic.set(true)
            skipDeprecated.set(true)
            reportUndocumented.set(true)

            platform.set(org.jetbrains.dokka.Platform.jvm)
            jdkVersion.set(8)

            perPackageOption {
                matchingRegex.set(".*internal.*")
                suppress.set(true) // 隐藏 internal API
            }
        }
    }
}
