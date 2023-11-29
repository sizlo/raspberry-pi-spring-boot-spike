import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.1.5"
	id("io.spring.dependency-management") version "1.1.3"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
}

group = "com.timsummertonbrier"
version = "0.0.7"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")
	implementation("org.flywaydb:flyway-core")
	implementation("org.postgresql:postgresql")

	implementation("org.jetbrains.exposed:exposed-spring-boot-starter:0.44.1")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("com.h2database:h2")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Replace the token `${version}` in application.yml
// with the version string defined in this file
tasks.processResources {
	filesMatching("**/application.yml") {
		filter { line ->
			line.replace("\${version}", "$version")
		}
	}
}
