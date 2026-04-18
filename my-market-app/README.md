# My Market App

Веб-приложение интернет-магазина на Spring Boot с блокирующим стеком Web MVC и Spring Data JPA.

## Требования

- Java 21
- Maven 3.8+
- Docker (опционально)

## Сборка и запуск

### Локально

```bash
# Собрать  проект
./mvnw clean package
java -jar target/my-market-app-1.0.0.jar

# Запустить  приложение
./mvnw spring-boot:run



# Запустить все тесты
./mvnw test

# Запустить только контроллеры
./mvnw test -Dtest="*ControllerTest"

# Запустить только сервисы
./mvnw test -Dtest="*ServiceTest"

# Запустить только репозитории
./mvnw test -Dtest="*RepositoryTest"


# Собрать  образ
docker build -t my-market-app .
# Запустите контейнер
docker run -p 8080:8080 my-market-app