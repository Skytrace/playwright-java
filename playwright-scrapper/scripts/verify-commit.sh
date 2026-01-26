#!/bin/bash

# Скрипт проверяет наличие тега [major], [minor] или [patch] в сообщении коммита
COMMIT_MSG_FILE=$1
COMMIT_MSG=$(cat "$COMMIT_MSG_FILE")

# Регулярное выражение для поиска необходимых тегов
REGEXP="\[major\]|\[minor\]|\[patch\]"

if [[ ! $COMMIT_MSG =~ $REGEXP ]]; then
    echo -e "\n\033[0;31m[ОШИБКА] Коммит отклонен!\033[0m"
    echo -e "Ваше сообщение должно содержать один из тегов версии:"
    echo -e "  - \033[0;32m[major]\033[0m : Глобальные изменения"
    echo -e "  - \033[0;32m[minor]\033[0m : Новые функции"
    echo -e "  - \033[0;32m[patch]\033[0m : Исправление багов / правки UI"
    echo -e "\nПример: git commit -m \"[patch] Исправлено отображение кнопок\"\n"
    exit 1
fi

exit 0