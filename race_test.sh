#!/bin/bash
# race_test.sh — Скрипт для проверки гонки при одновременном взятии заявки двумя мастерами
# Использование: ./race_test.sh [ticket_id]
# Требования: curl, запущенное приложение на localhost:8080

TICKET_ID=${1:-1}
BASE_URL="http://localhost:8080"

echo "=== Тест гонки запросов (Race Condition Test) ==="
echo "Попытка одновременного взятия заявки #$TICKET_ID двумя мастерами..."
echo ""

# Функция для попытки взять заявку
take_ticket() {
    local master=$1
    local result=$(curl -s -X POST "$BASE_URL/api/master/tickets/$TICKET_ID/take" \
        -u "$master:password" \
        -H "Content-Type: application/json" \
        -w "\n%{http_code}")
    
    local http_code=$(echo "$result" | tail -n1)
    local body=$(echo "$result" | head -n -1)
    
    echo "[$master] HTTP $http_code"
    if [ "$http_code" == "200" ]; then
        echo "[$master] ✅ УСПЕХ: Заявка взята"
    elif [ "$http_code" == "409" ]; then
        echo "[$master] ❌ КОНФЛИКТ: Заявка уже взята другим мастером"
    else
        echo "[$master] ⚠️  Ответ: $body"
    fi
    echo ""
}

# Запускаем два запроса параллельно
take_ticket "master1" &
take_ticket "master2" &

# Ждём завершения обоих
wait

echo "=== Результат ==="
echo "Если один мастер получил 200 (успех), а второй 409 (конфликт) — гонка обработана корректно!"
echo "Optimistic locking работает: только один запрос может успешно изменить заявку."

