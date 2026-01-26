#!/bin/bash

# Скрипт для проверки наличия тега версии [major], [minor] или [patch]
COMMIT_MSG_FILE=$1
COMMIT_MSG=$(cat "$COMMIT_MSG_FILE")

# Регулярное выражение для поиска тегов
REGEXP="\[major\]|\[minor\]|\[patch\]"

# Если это автоматический коммит от скрипта бампа, пропускаем проверку
if [[ $COMMIT_MSG == *"[auto-bump]"* ]]; then
    exit 0
fi

# Проверка на наличие тега
if [[ ! $COMMIT_MSG =~ $REGEXP ]]; then
    echo -e "\n\033[0;31m[ОШИБКА] Коммит отклонен!\033[0m"
    echo -e "Сообщение должно содержать один из тегов: \033[0;32m[major]\033[0m, \033[0;32m[minor]\033[0m или \033[0;32m[patch]\033[0m."
    echo -e "Пример: git commit -m \"[patch] исправил баг в поиске\"\n"
    exit 1
fi

echo -e "\033[0;32m[OK]\033[0m Тег версии найден."
exit 0