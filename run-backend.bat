@echo off
cd /d "D:\Claudev\hospital-management-system"
set JAVA_HOME=C:\Program Files\Java\jdk-21
call "D:\Claudev\hospital-management-system\mvnw.cmd" spring-boot:run -Dspring-boot.run.profiles=local
