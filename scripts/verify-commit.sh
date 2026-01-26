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
    echo -e "\n\033[0;31m[Error] commit rejected!\033[0m"
    echo -e "The commit must have once of tag: \033[0;32m[major]\033[0m, \033[0;32m[minor]\033[0m or \033[0;32m[patch]\033[0m."
    echo -e "Example: git commit -m \"[patch] fix bug in search\"\n"
    exit 1
fi

echo -e "\033[0;32m[OK]\033[0m Version tag is found."
exit 0