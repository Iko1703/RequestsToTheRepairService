## Repair Service Tickets — тестовое задание

Приложение для приёма и обработки заявок в ремонтную службу с ролями **диспетчер** и **мастер**, поддержкой борьбы с гонками запросов при операции `take`, audit log и запуском как через Docker, так и локально.

---

### Стек

- **Java**: 17
- **Spring Boot**: 4.0.2
- **Spring MVC** (REST + Thymeleaf)
- **Spring Data JPA + Hibernate**
- **БД**:
  - в Docker: PostgreSQL 15
  - локально/в тестах: H2 (in-memory)
- **Spring Security**: форм-логин, роли `DISPATCHER` и `MASTER`
- **Сборка**: Maven
- **Тесты**: JUnit 5, Spring Boot Test

---

### Доменная модель

**User**
- `id`
- `username` (уникальный)
- `passwordHash`
- `role` (`DISPATCHER`, `MASTER`)

**Ticket**
- `id`
- `clientName` (обязательно)
- `phone` (обязательно)
- `address` (обязательно)
- `problemText` (обязательно)
- `status`: `NEW | ASSIGNED | IN_PROGRESS | DONE | CANCELED`
- `assignedTo` (`User`, мастер, может быть `null`)
- `createdAt`, `updatedAt`
- `version` — для optimistic locking (борьба с гонками)

**TicketEvent** (audit log)
- `id`
- `ticket` (многие-к-одному)
- `type`: `CREATED | STATUS_CHANGED | ASSIGNED | CANCELED | COMMENTED`
- `createdAt`
- `details` (строка)
- `userName` (кто сделал действие, хранится текстом, без FK)

---

### Роли и потоки работы

**Диспетчер**
- создаёт заявки;
- видит все заявки;
- назначает мастера на заявку (`ASSIGNED`);
- может отменять заявки (`CANCELED`).

**Мастер**
- видит свои и доступные к взятию заявки;
- может "взять" заявку (`take`), переводя её из `NEW` в `IN_PROGRESS`;
- может начать работу по назначенной заявке (`ASSIGNED → IN_PROGRESS`);
- может завершить свою заявку (`IN_PROGRESS → DONE`);
- может отменить свою заявку (если разрешено, здесь разрешено до `DONE`/`CANCELED`).

Все проверки прав дублируются:
- на уровне **SecurityConfig** (доступ к URL по ролям),
- на уровне **сервисов** (`TicketService`) — бизнес-проверки по ролям и принадлежности заявки.

---

### Борьба с гонками (race conditions)

Критичный сценарий: два мастера одновременно пытаются выполнить `take` одной и той же заявки (`NEW`).

Реализация:
- в `Ticket` есть поле `@Version Long version` — **optimistic locking**;
- метод `TicketService.takeTicket`:
  - в транзакции читает заявку;
  - проверяет статус `NEW` и отсутствие `assignedTo`;
  - выставляет `assignedTo` и `status = IN_PROGRESS`;
  - сохраняет через JPA/Hibernate.
- при конкуренции Hibernate выполняет `UPDATE ... WHERE id=? AND version=?`,  
  и один из потоков получает `OptimisticLockingFailureException`;
- глобальный обработчик `RestExceptionHandler` конвертирует это в HTTP **409 Conflict**  
  с сообщением “Заявка была изменена параллельно...”.

Отдельный интеграционный тест `TicketServiceRaceConditionTest` запускает два параллельных `takeTicket` и проверяет, что:
- только один вызов считается успешным,
- второй получает ошибку (optimistic locking или бизнес-ошибку).

---

### Audit log

Сервис `TicketEventService` пишет `TicketEvent` при:
- создании заявки,
- смене статуса,
- операции `take` (мастер взял заявку),
- отмене.

Хранится:
- тип события;
- время;
- идентификатор заявки;
- текстовое имя пользователя;
- детали (например, “Status changed from NEW to ASSIGNED”).

История может использоваться для отображения на странице заявки и для аудита.

---

### Безопасность

`SecurityConfig`:
- форм-логин на `/login` (собственный шаблон `login.html`);
- успешный логин → редирект на `/`:
  - `HomeController` перенаправляет:
    - `DISPATCHER` → `/dispatcher`
    - `MASTER` → `/master`
- logout:
  - POST `/logout` → редирект на `/login?logout`;
  - кнопка “Выйти” есть в шаблонах панелей.

Для упрощения тестового задания **CSRF отключён** (`csrf.disable()`), чтобы не мешать POST-формам.

Демо-пользователи в Security (in-memory):
- `dispatcher` / `password` (роль `DISPATCHER`)
- `master1` / `password` (роль `MASTER`)
- `master2` / `password` (роль `MASTER`)

В тестах и доменной логике отдельно используются сущности `User` в БД (через `UserRepository`).

---

### Эндпоинты (основные)

REST (`TicketRestController`, `/api/tickets`):
- `POST /api/tickets` — создать заявку (принимает `TicketCreateRequest`);
- `GET /api/tickets/{id}` — получить заявку (с учётом прав);
- `GET /api/tickets` — список заявок (разное поведение для диспетчера и мастера).

Панель диспетчера (`DispatcherController`, `/dispatcher`):
- `GET /dispatcher` — HTML-таблица с заявками.

Панель мастера (`MasterController`, `/master`):
- `GET /master` — HTML-таблица с заявками, relevantes для мастера.

Логин/логаут:
- `GET /login`, `POST /login`, `POST /logout`.

---

### UI

Шаблоны Thymeleaf:
- `login.html` — форма входа;
- `dispatcher-panel.html` — панель диспетчера, список заявок + кнопка “Выйти”;
- `master-panel.html` — панель мастера, список заявок + кнопка “Выйти”;
- `create-ticket.html` — форма создания заявки (можно подключить к `/api/tickets`);
- `error.html` — единая страница отображения ошибок для MVC-контроллеров.

UI выдержан в минималистичном стиле, акцент на читаемости и простоте проверки.

---

### Тестирование

Интеграционные тесты (`@SpringBootTest`):

- `TicketServiceTest`:
  - **happy-path**:
    - создаёт диспетчера и мастера в БД через `UserRepository`;
    - создаёт заявку;
    - назначает мастера (DISPATCHER → `ASSIGNED`);
    - переводит в `IN_PROGRESS` и `DONE`, проверяя статусы.
  - **негативный сценарий**:
    - проверяет, что мастер не может завершить заявку, если она не в `IN_PROGRESS` (ожидается `ForbiddenException`).

- `TicketServiceRaceConditionTest`:
  - создаёт диспетчера и двух мастеров в БД;
  - создаёт заявку `NEW`;
  - запускает два параллельных `takeTicket` для одного и того же `ticketId`;
  - считает успешные и неуспешные попытки и проверяет, что:
    - ровно один мастер успешно взял заявку,
    - второй получил ошибку.

Тесты используют H2 (in-memory) и транзакции, поэтому не требуют реальной PostgreSQL.

Запуск тестов:

```bash
mvn test
```

---

### Запуск без Docker (локальная разработка)

Требования:
- JDK 17
- Maven 3.9+

Шаги:

```bash
cd Tasks-for-the-company-Business-Base
mvn spring-boot:run
```

Приложение поднимется на:
- `http://localhost:8080` — логин,
- `http://localhost:8080/h2` — консоль H2 (если нужна).

Для логина используй:
- `dispatcher` / `password`
- `master1` / `password`
- `master2` / `password`

---

### Запуск с Docker / Docker Compose

Требуется установленный Docker Desktop.

В корне проекта есть:
- `Dockerfile` — multi-stage сборка Spring Boot приложения (Maven + JRE);
- `docker-compose.yml` — два сервиса:
  - `db` — PostgreSQL 15 (порт 5433 на хосте);
  - `app` — Spring Boot, подключённый к `db`.

Шаги:

```bash
cd Tasks-for-the-company-Business-Base
docker compose down               # на всякий случай
docker compose up --build -d      # собрать и запустить в фоне
```

После старта:
- приложение: `http://localhost:8080`
- БД PostgreSQL (если нужно отдельно): `localhost:5433`, БД `repairdb`, пользователь `repairuser`, пароль `repairpass`.

Остановить:

```bash
docker compose down
```

---

### Демоданные (Сиды)

При старте приложения автоматически загружаются данные из `data.sql`:
- **3 пользователя**: 1 диспетчер (`dispatcher`), 2 мастера (`master1`, `master2`)
- **5 заявок**: в разных статусах (`NEW`, `ASSIGNED`, `IN_PROGRESS`)
- **События аудита**: история создания и назначения

Это позволяет сразу увидеть непустые панели и протестировать функционал.

---

### Как проверить гонку запросов

**Способ 1: Скрипт race_test.sh (Linux/Mac/WSL)**

```bash
chmod +x race_test.sh
./race_test.sh 1
```

Скрипт параллельно отправляет два запроса на взятие заявки #1 от разных мастеров и показывает результат.

**Способ 2: Автотест**

```bash
mvn test -Dtest=TicketServiceRaceConditionTest
```

Тест создаёт заявку и запускает два потока, каждый пытается её взять. Проверяется, что только один успешен.

**Ожидаемый результат**: один мастер получает HTTP 200 (успех), второй — HTTP 409 Conflict (заявка уже взята).

---

### Дополнительно

- Глобальные обработчики ошибок:
  - `RestExceptionHandler` — для REST (возвращает JSON c `timestamp`, `status`, `error`, `message`).
  - `GlobalExceptionHandler` — для MVC (Thymeleaf), показывает `error.html` с текстом ошибки.
- Структура пакетов:
  - `model` — сущности и enum'ы,
  - `repository` — Spring Data JPA репозитории,
  - `service` / `service.impl` — бизнес-логика,
  - `security` — конфигурация Spring Security,
  - `web` — контроллеры и обработчики ошибок,
  - `config` — вспомогательные конфиги и демо-данные,
  - `dto` — входные/выходные модели для REST/UI.

Код и архитектура ориентированы на читаемость, демонстрацию слоистого подхода, корректную работу с транзакциями и конкурентным доступом к заявкам.


