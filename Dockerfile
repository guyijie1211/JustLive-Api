# 使用Java 8作为基础镜像
FROM openjdk:8-jre-alpine

# 将Spring Boot应用程序的JAR文件复制到镜像中，并命名为app.jar
COPY target/mixed-live-back.jar app.jar

# 暴露Spring Boot应用程序的默认端口8013 8014
EXPOSE 8013 8014

# 运行Spring Boot应用程序
ENTRYPOINT ["java", "-jar", "app.jar"]
