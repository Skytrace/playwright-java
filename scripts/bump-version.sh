#!/bin/bash

# File path
VERSION_FILE="VERSION"
CHANGELOG_FILE="CHANGELOG.md"

# Read current version (if file absent create them, create 3.2.1)
if [ ! -f $VERSION_FILE ]; then echo "3.2.1" > $VERSION_FILE; fi
CURRENT_VERSION=$(cat $VERSION_FILE)
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"

# Get last commit text
COMMIT_MSG=$(git log -1 --pretty=%B)

# if change already commited (point [auto-bump]), exit
if [[ $COMMIT_MSG == *"[auto-bump]"* ]]; then
    exit 0
fi

# Detect, what need to increase
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
    # if tag not found - exit
    exit 0
fi

NEW_VERSION="$MAJOR.$MINOR.$PATCH"
DATE=$(date +%Y-%m-%d)

# 1. writing new version in file VERSION
echo $NEW_VERSION > $VERSION_FILE

# 2. Updating CHANGELOG.md (added a record in the beginning of file)
CLEAN_MSG=$(echo $COMMIT_MSG | sed -E 's/\[(major|minor|patch)\]//g' | xargs)
TEMP_FILE="temp_ch.md"
echo -e "## [$NEW_VERSION] — $DATE\n### $TYPE\n- $CLEAN_MSG\n" > $TEMP_FILE
cat $CHANGELOG_FILE >> $TEMP_FILE
mv $TEMP_FILE $CHANGELOG_FILE

# 3. adding files and add into commit
git add $VERSION_FILE $CHANGELOG_FILE

git commit --amend --no-edit --no-verify -m "$COMMIT_MSG [auto-bump]"

echo -e "\033[0;32m[SUCCESS]\033[0m Version increased to $NEW_VERSION"