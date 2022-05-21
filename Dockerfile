FROM openjdk:17-jdk-alpine as builder
WORKDIR /app

# Copy maven executable to the image
COPY mvnw .
COPY .mvn .mvn

# Copy the pom.xml file
COPY pom.xml .

# Fix permissions for mvnw executable
RUN chmod +x mvnw

# Download all dependecies
RUN ./mvnw dependency:go-offline -B


# Copy the project source
COPY ./src ./src
COPY ./pom.xml ./pom.xml

RUN ./mvnw package -DskipTests -Dmaven.gitcommitid.skip=true

FROM openjdk:17-alpine
WORKDIR /app

EXPOSE 8080
COPY --from=builder /app/target/course-journal-backend.jar /app/app.jar

ENTRYPOINT ["java","-jar","app.jar"]
