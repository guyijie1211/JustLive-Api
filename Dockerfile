# 使用Java 8作为基础镜像
FROM openjdk:8-jre-alpine

# 设置容器的工作目录为/tmp
VOLUME /tmp

# 将Spring Boot应用程序的JAR文件复制到镜像中，并命名为app.jar
COPY target/mixed-live-back.jar /home/admin/app/target/app.jar

# 将MySQL的初始化脚本init.sql复制到容器的/docker-entrypoint-initdb.d/目录下
COPY init.sql /docker-entrypoint-initdb.d/

# 暴露Spring Boot应用程序的默认端口8013 8014
EXPOSE 8013 8014 3306

# 运行Spring Boot应用程序
ENTRYPOINT ["java", "-jar", "/home/admin/app/target/app.jar"]