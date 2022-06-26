# Вступительное задание в Летнюю Школу Бэкенд Разработки Яндекса 2022

## Требования

Для локального запуска необходимы:

- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven 3](https://maven.apache.org)

## Локальный запуск

Локальный запуск возможен через запуск `/ru/megamarket/MegaMarket.java` из IDE.

Также можно использовать [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) следующим образом:

```
mvn spring-boot:run
```

При локальном запуске нужно указать ссылку на БД postgres и авторизационные данные в файле ```application.yml```:

```
spring:
  datasource:
    url: jdbc:postgresql://localhost/{название БД}
    username: {логин}
    password: {пароль}
```

## Запуск в докере

Перед первым запуском необходимо выполнить команду ```docker-compose build``` в корневом каталоге.

Последующий запуск выполняется командой ```docker-compose up```
