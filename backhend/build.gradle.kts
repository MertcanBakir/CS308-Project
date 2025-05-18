plugins {
	java
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "cs308"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
	maven { url = uri("https://maven.itextpdf.com") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-mail")

	implementation("com.github.librepdf:openpdf:1.3.30")

	implementation("org.postgresql:postgresql:42.7.1")
	implementation("com.h2database:h2:2.1.214")
	implementation("org.hibernate:hibernate-core:6.2.7.Final")

	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
	implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
	implementation("com.itextpdf:kernel:7.2.3")
	implementation("com.itextpdf:layout:7.2.3")
	implementation("com.itextpdf:io:7.2.3")
	compileOnly("org.projectlombok:lombok:1.18.30")
	annotationProcessor("org.projectlombok:lombok:1.18.30")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

 tasks.withType<Test> {
	useJUnitPlatform()
}