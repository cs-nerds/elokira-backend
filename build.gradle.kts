import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = "1.5.0"
val exposedVersion = "0.28.1"
val postgresVersion = "42.2.18.jre7"
val hikariCpVersion = "3.4.5"
val flywayVersion = "7.5.0"
val ktorFlywayVersion = "1.2.2"
val logbackVersion = "1.2.3"
val assertjVersion = "3.18.1"
val restAssuredVersion = "4.3.3"
val junitVersion = "5.7.0"
val atVersion = "3.4.6"
val seleniumVersion = "3.141.59"
val webDriverVersion = "3.8.1"
val guavaVersion = "30.1-jre"


plugins {
    kotlin("jvm") version "1.4.30"
    application
}

repositories {
    jcenter()
    maven { setUrl("http://dl.bintray.com/africastalking/java") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-websockets:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")

    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("com.zaxxer:HikariCP:$hikariCpVersion")
    implementation("com.viartemev:ktor-flyway-feature:$ktorFlywayVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.seleniumhq.selenium:selenium-java:$seleniumVersion")
    implementation("io.github.bonigarcia:webdrivermanager:$webDriverVersion")

    implementation("com.google.guava:guava:$guavaVersion") // it's shared (selenium & at)

    implementation("com.africastalking:core:$atVersion") // has conflicting deps

    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("io.ktor:ktor-client-cio:$ktorVersion")
}

application {
    mainClass.set("MainKt")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}
