-- Сиды: начальные данные для демонстрации
-- Пароль для всех: password (BCrypt хеш)

-- Пользователи: 1 диспетчер, 2 мастера
INSERT INTO users (username, password_hash, role) VALUES
    ('dispatcher', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.u1R1Ah1Qj6eBZ.Bf9C', 'DISPATCHER'),
    ('master1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.u1R1Ah1Qj6eBZ.Bf9C', 'MASTER'),
    ('master2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.u1R1Ah1Qj6eBZ.Bf9C', 'MASTER');

-- Демо-заявки для проверки
INSERT INTO tickets (client_name, phone, address, problem_text, status, assigned_to_id, created_at, updated_at, version) VALUES
    ('Иван Иванов', '+7 900 111-22-33', 'Москва, ул. Пушкина, д. 1, кв. 10', 'Не работает розетка на кухне', 'NEW', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('Петр Петров', '+7 900 222-33-44', 'Москва, ул. Лермонтова, д. 5, кв. 25', 'Протечка крана в ванной', 'NEW', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('Анна Сидорова', '+7 900 333-44-55', 'Москва, пр. Мира, д. 15, кв. 7', 'Не открывается входная дверь', 'NEW', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('Мария Козлова', '+7 900 444-55-66', 'Москва, ул. Гагарина, д. 20, кв. 42', 'Сломался замок на балконной двери', 'ASSIGNED', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('Сергей Волков', '+7 900 555-66-77', 'Москва, ул. Ленина, д. 8, кв. 3', 'Не работает отопление', 'IN_PROGRESS', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- События аудита для демо-заявок (user_name вместо user_id)
INSERT INTO ticket_events (ticket_id, user_name, type, details, created_at) VALUES
    (1, 'dispatcher', 'CREATED', 'Заявка создана', CURRENT_TIMESTAMP),
    (2, 'dispatcher', 'CREATED', 'Заявка создана', CURRENT_TIMESTAMP),
    (3, 'dispatcher', 'CREATED', 'Заявка создана', CURRENT_TIMESTAMP),
    (4, 'dispatcher', 'CREATED', 'Заявка создана', CURRENT_TIMESTAMP),
    (4, 'dispatcher', 'ASSIGNED', 'Назначен мастер: master1', CURRENT_TIMESTAMP),
    (5, 'dispatcher', 'CREATED', 'Заявка создана', CURRENT_TIMESTAMP),
    (5, 'dispatcher', 'ASSIGNED', 'Назначен мастер: master2', CURRENT_TIMESTAMP),
    (5, 'master2', 'STATUS_CHANGED', 'Статус изменён: NEW -> IN_PROGRESS', CURRENT_TIMESTAMP);
