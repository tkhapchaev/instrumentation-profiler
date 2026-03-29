import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("application")
}

group = "ru.tkhapchaev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-commons:9.7")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("ru.tkhapchaev.Main")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to application.mainClass.get(),
            "Premain-Class" to "ru.tkhapchaev.profiler.ProfilerAgent",
            "Can-Redefine-Classes" to "false",
            "Can-Retransform-Classes" to "false",
        )
    }
}

val profilerArgs = providers.gradleProperty("profilerArgs").orElse("top=10,sort=total")

tasks.register<JavaExec>("runProfiled") {
    group = "application"
    description = "Runs demo workload with the custom javaagent profiler"

    dependsOn(tasks.classes, tasks.jar)

    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath

    jvmArgs("-javaagent:${tasks.jar.get().archiveFile.get().asFile.absolutePath}=${profilerArgs.get()}")
}

val jfrReportFile = layout.buildDirectory.file("reports/jfr/demo.jfr")
val jfrTempDir = layout.buildDirectory.dir("tmp/jfr")
val jfrSummaryFile = layout.buildDirectory.file("reports/jfr/summary.txt")
val jfrHotMethodsFile = layout.buildDirectory.file("reports/jfr/hot-methods.txt")

tasks.register<JavaExec>("runWithJfr") {
    group = "application"
    description = "Runs demo workload with Java Flight Recorder and prints JFR report"

    dependsOn(tasks.classes)

    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath

    doFirst {
        jfrReportFile.get().asFile.parentFile.mkdirs()
        jfrTempDir.get().asFile.mkdirs()
    }

    jvmArgs(
        "-Djava.io.tmpdir=${jfrTempDir.get().asFile.absolutePath}",
        "-XX:StartFlightRecording=filename=${jfrReportFile.get().asFile.absolutePath},settings=profile,dumponexit=true,jdk.ExecutionSample#enabled=true,jdk.ExecutionSample#period=5ms"
    )

    doLast {
        val javaExecutable = javaLauncher.get().executablePath.asFile
        val jfrExecutableName = if (System.getProperty("os.name").lowercase().contains("win")) "jfr.exe" else "jfr"
        val jfrExecutable = javaExecutable.parentFile.resolve(jfrExecutableName)

        if (!jfrExecutable.exists()) {
            logger.warn("JFR CLI not found near Java executable: ${jfrExecutable.absolutePath}")
            logger.warn("Recording saved to: ${jfrReportFile.get().asFile.absolutePath}")

            return@doLast
        }

        val recordingPath = jfrReportFile.get().asFile.absolutePath
        val summaryOutput = ByteArrayOutputStream()
        val hotMethodsOutput = ByteArrayOutputStream()

        exec {
            commandLine(jfrExecutable.absolutePath, "summary", recordingPath)
            standardOutput = summaryOutput
        }

        exec {
            commandLine(jfrExecutable.absolutePath, "view", "hot-methods", recordingPath)
            standardOutput = hotMethodsOutput
        }

        jfrSummaryFile.get().asFile.writeText(summaryOutput.toString(Charsets.UTF_8))
        jfrHotMethodsFile.get().asFile.writeText(hotMethodsOutput.toString(Charsets.UTF_8))

        println()
        println("=== JFR summary ===")
        println(summaryOutput.toString(Charsets.UTF_8))
        println("=== JFR hot methods ===")
        println(hotMethodsOutput.toString(Charsets.UTF_8))
        println("Saved:")
        println(" - ${jfrReportFile.get().asFile.absolutePath}")
        println(" - ${jfrSummaryFile.get().asFile.absolutePath}")
        println(" - ${jfrHotMethodsFile.get().asFile.absolutePath}")
    }
}

tasks.test {
    useJUnitPlatform()
}