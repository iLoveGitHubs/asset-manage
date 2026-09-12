FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY backend/target/asset-manage-backend-1.0.0.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/health || exit 1
CMD ["java", "-jar", "app.jar"]
