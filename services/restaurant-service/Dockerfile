FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -pl services/restaurant-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /app/services/restaurant-service/target/*.jar app.jar
USER spring
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
