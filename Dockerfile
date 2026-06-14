# Giai đoạn 1: Build Frontend
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Giai đoạn 2: Build Backend
FROM maven:3.9.6-eclipse-temurin-17 AS backend-build
WORKDIR /app
# Copy toàn bộ code backend
COPY backend/pom.xml backend/
COPY backend/src backend/src
COPY backend/mvnw backend/
COPY backend/.mvn backend/.mvn

# Copy kết quả build từ frontend vào thư mục static của backend
COPY --from=frontend-build /app/frontend/dist /app/backend/src/main/resources/static/

# Build JAR
WORKDIR /app/backend
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Giai đoạn 3: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
