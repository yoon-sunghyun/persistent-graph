plugins {
    id("java")
}

group = "yoon.sunghyun"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20230618")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
