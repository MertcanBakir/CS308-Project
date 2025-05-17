FROM openjdk:17-jdk-slim
VOLUME /tmp

# PostgreSQL istemci araçlarını ekle
RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*

# wait-for scriptini ekle
COPY wait-for-postgres.sh /wait-for-postgres.sh
RUN chmod +x /wait-for-postgres.sh

# JAR dosyasını ve config'i ekle
COPY build/libs/*.jar app.jar
COPY src/main/resources/application.properties /config/application.properties

ENV SPRING_CONFIG_LOCATION=file:/config/application.properties

# wait-for-postgres çalıştıktan sonra app başlasın
ENTRYPOINT ["/wait-for-postgres.sh", "--", "java", "-jar", "/app.jar"]
