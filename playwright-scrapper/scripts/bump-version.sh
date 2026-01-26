#!/bin/bash

    # Пути к файлам
    VERSION_FILE="VERSION"
    CHANGELOG_FILE="CHANGELOG.md"

    # Проверка наличия файлов
    if [ ! -f $VERSION_FILE ]; then echo "3.0.0" > $VERSION_FILE; fi

    # Читаем текущую версию
    CURRENT_VERSION=$(cat $VERSION_FILE)
    IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"

    # Получаем сообщение последнего коммита
    # Если запускается через hook, сообщение можно вытащить так:
    COMMIT_MSG=$(git log -1 --pretty=%B)

    # Логика инкремента
    if [[ $COMMIT_MSG == *"[major]"* ]]; then
        MAJOR=$((MAJOR + 1))
        MINOR=0
        PATCH=0
        TYPE="major"
    elif [[ $COMMIT_MSG == *"[minor]"* ]]; then
        MINOR=$((MINOR + 1))
        PATCH=0
        TYPE="minor"
    elif [[ $COMMIT_MSG == *"[patch]"* ]]; then
        PATCH=$((PATCH + 1))
        TYPE="patch"
    else
        echo "Тег версии [major/minor/patch] не найден в коммите. Пропуск обновления."
        exit 0
    fi

NEW_VERSION="$MAJOR.$MINOR.$PATCH"
DATE=$(date +%Y-%m-%d)

# Обновляем файл VERSION
echo $NEW_VERSION > $VERSION_FILE

# Подготовка записи в CHANGELOG
# Убираем тег из сообщения для красоты
CLEAN_MSG=$(echo $COMMIT_MSG | sed -E 's/\[(major|minor|patch)\]//g' | xargs)

# Создаем временный файл для вставки в начало CHANGELOG
TEMP_FILE="temp_changelog.md"
echo -e "## [$NEW_VERSION] — $DATE\n### [$TYPE]\n- $CLEAN_MSG\n" > $TEMP_FILE
cat $CHANGELOG_FILE >> $TEMP_FILE
mv $TEMP_FILE $CHANGELOG_FILE

echo "Версия обновлена до $NEW_VERSION"

# Автоматически добавляем изменения в git (опционально)
git add $VERSION_FILE $CHANGELOG_FILE
git commit --amend --no-edit