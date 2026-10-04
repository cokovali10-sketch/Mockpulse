@echo off
echo Building MockPulse (Kotlin/Gradle)...
call gradlew.bat package
echo Done: check build/compose/binaries/main/
