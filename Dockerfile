FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:21-jre
ENV TZ=Asia/Ho_Chi_Minh
WORKDIR /app

RUN useradd --system --uid 10001 medclinic \
    && mkdir -p /app/uploads/avatars \
    && chown -R medclinic:medclinic /app/uploads
COPY --from=build /workspace/target/medclinic-system-*.jar /app/medclinic.jar

USER medclinic
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/medclinic.jar"]
