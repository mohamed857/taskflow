# المرحلة الأولى: بناء التطبيق (Build)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# بننسخ ملف الـ pom الأول عشان لو ملفات الجافا اتغيرت وهو مش اتغيرت، مش ينعمل تحميل للمكتبات تاني (Caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B
# بننسخ باقي الكود
COPY src ./src
# بنعمل بناء للمشروع ون_skip الـ Tests عشان السرعة
RUN mvn clean package -DskipTests

# المرحلة الثانية: تشغيل التطبيق (Run) - بيكون حجمه صغير جداً
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# ناخد الملف اللي اتبنى من المرحلة الأولى
COPY --from=build /app/target/*.jar app.jar
# نشغل التطبيق على بورت 8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]