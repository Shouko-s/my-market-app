# My Market App — витрина интернет-магазина

Веб-приложение на Spring Boot (Web MVC, Thymeleaf, Spring Data JPA, PostgreSQL).

Перед запуском создайте файл `.env` с параметрами подключения к БД:

```bash
cp .env.example .env
```

## Запуск в среде разработки (IntelliJ IDEA)

1. Открыть проект как Maven-проект, выбрать JDK 21.
2. Включить обработку аннотаций (Lombok): *Settings → Build → Compiler → Annotation Processors*.
3. Поднять PostgreSQL: `docker compose up -d postgres`.
4. Запустить класс `MyMarketAppApplication`.

## Локальный запуск

Нужны JDK 21 и Maven 3.9+.

1. Поднять PostgreSQL:
   ```bash
   docker compose up -d postgres
   ```
2. Собрать и запустить Executable JAR:
   ```bash
   mvn clean package
   java -jar target/my-market-app-0.0.1-SNAPSHOT.jar
   ```

## Запуск в Docker

Нужны JDK 21, Maven 3.9+, Docker и Docker Compose. Образ собирается из готового Executable JAR, поэтому сначала нужно собрать JAR:

```bash
mvn clean package -DskipTests
docker compose up -d --build
```

Приложение доступно на http://localhost:8080.
