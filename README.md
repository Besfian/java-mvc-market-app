# My Market App

Веб-приложение интернет-магазина на Spring Boot с блокирующим стеком Web MVC и Spring Data JPA.

## Требования

- Java 21
- Maven 3.8+
- Docker (опционально)

## Сборка и запуск

### Локально

```bash

# Очистить кэш Maven и перезагрузить зависимости
./mvnw dependency:purge-local-repository
./mvnw clean install -Dmaven.test.skip=true

# Очистить проект
./mvnw clean

# Собрать  проект
chmod +x mvnw
./mvnw clean package
java -jar target/my-market-app-1.0.0.jar

# Собрать  проект без тестов
./mvnw clean package -DskipTests


# Запустить  приложение
./mvnw spring-boot:run
./mvnw clean spring-boot:run


# Собрать  образ
docker build -t my-market-app .
# Запустите контейнер
docker run -p 8080:8080 my-market-app

cd shop-app
chmod +x mvnw
./mvnw spring-boot:run

cd payment-service
chmod +x mvnw
./mvnw spring-boot:run
