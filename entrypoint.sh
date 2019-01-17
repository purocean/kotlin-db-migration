#!/bin/sh -l

echo $GNUPG_KEY | sed 's/ //g' | base64 -d > ./secring.gpg
echo $GRADLE_CONFIG | sed 's/ //g' | base64 -d > ./gradle.properties

./gradlew -Dgradle.user.home=./ publishMavenJavaPublicationToMavenRepository
