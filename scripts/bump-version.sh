#!/bin/bash

# Пути к файлам в корне проекта
VERSION_FILE="VERSION"
CHANGELOG_FILE="CHANGELOG.md"

# Читаем текущую версию (если файла нет, создаем 3.2.1)
if [ ! -f $VERSION_FILE ]; then echo "3.2.1" > $VERSION_FILE; fi
CURRENT_VERSION=$(cat $VERSION_FILE)
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"

# Получаем текст последнего коммита
COMMIT_MSG=$(git log -1 --pretty=%B)

# Если этот коммит уже обработан (есть метка [auto-bump]), выходим
if [[ $COMMIT_MSG == *"[auto-bump]"* ]]; then
    exit 0
fi

# Определяем, что именно нужно увеличить
if [[ $COMMIT_MSG == *"[major]"* ]]; then
    MAJOR=$((MAJOR + 1))
    MINOR=0
    PATCH=0
    TYPE="Major Update"
elif [[ $COMMIT_MSG == *"[minor]"* ]]; then
    MINOR=$((MINOR + 1))
    PATCH=0
    TYPE="Minor Update"
elif [[ $COMMIT_MSG == *"[patch]"* ]]; then
    PATCH=$((PATCH + 1))
    TYPE="Patch"
else
    # Если тег не найден (не должно случиться при работе хука), выходим
    exit 0
fi

NEW_VERSION="$MAJOR.$MINOR.$PATCH"
DATE=$(date +%Y-%m-%d)

# 1. Записываем новую версию в файл VERSION
echo $NEW_VERSION > $VERSION_FILE

# 2. Обновляем CHANGELOG.md (добавляем запись в начало файла)
CLEAN_MSG=$(echo $COMMIT_MSG | sed -E 's/\[(major|minor|patch)\]//g' | xargs)
TEMP_FILE="temp_ch.md"
echo -e "## [$NEW_VERSION] — $DATE\n### $TYPE\n- $CLEAN_MSG\n" > $TEMP_FILE
cat $CHANGELOG_FILE >> $TEMP_FILE
mv $TEMP_FILE $CHANGELOG_FILE

# 3. Добавляем файлы и дополняем текущий коммит
git add $VERSION_FILE $CHANGELOG_FILE
# --no-verify нужен, чтобы не сработал хук commit-msg на измененное сообщение
git commit --amend --no-edit --no-verify -m "$COMMIT_MSG [auto-bump]"

echo -e "\033[0;32m[SUCCESS]\033[0m Версия повышена до $NEW_VERSION"