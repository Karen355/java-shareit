# ShareIt

Сервис обмена вещами. Пользователи добавляют вещи, бронируют их, оставляют отзывы и создают запросы на вещи, которых пока нет в каталоге.

Проект собирается одной командой:

```bash
mvn clean verify -Pcheck,coverage
```

`gateway` принимает HTTP-запросы на порту 8080, проверяет входные данные и передаёт их модулю `server`. `server` работает на порту 9090 и хранит данные в PostgreSQL.

Для локального запуска нужны PostgreSQL и две команды в отдельных терминалах:

```bash
mvn -f server/pom.xml spring-boot:run
mvn -f gateway/pom.xml spring-boot:run
```

Параметры БД задаются через `SHAREIT_DB_URL`, `SHAREIT_DB_USER`, `SHAREIT_DB_PASSWORD`. Адрес сервера для gateway можно переопределить через `SHAREIT_SERVER_URL`. Для запуска всей системы с БД также предусмотрен `docker-compose.yml`.
