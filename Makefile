build-jar:
	./gradlew clean shadowJar -x test -x detekt
