@ECHO OFF
SETLOCAL ENABLEDELAYEDEXPANSION

SET "APP_HOME=%~dp0"
SET "GRADLE_VERSION=9.4.1"
SET "WRAPPER_DIR=%APP_HOME%.gradle-wrapper"
SET "GRADLE_HOME=%WRAPPER_DIR%\gradle-%GRADLE_VERSION%"
SET "GRADLE_ZIP=%WRAPPER_DIR%\gradle-%GRADLE_VERSION%-bin.zip"
SET "GRADLE_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"

WHERE java >NUL 2>NUL
IF ERRORLEVEL 1 (
  ECHO Java runtime not found. Install JDK 25 or newer and run gradlew.bat again.
  EXIT /B 1
)

IF NOT EXIST "%GRADLE_HOME%\bin\gradle.bat" (
  IF NOT EXIST "%WRAPPER_DIR%" MKDIR "%WRAPPER_DIR%"
  IF NOT EXIST "%GRADLE_ZIP%" (
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%GRADLE_URL%' -OutFile '%GRADLE_ZIP%'"
    IF ERRORLEVEL 1 EXIT /B 1
  )
  IF EXIST "%GRADLE_HOME%" RMDIR /S /Q "%GRADLE_HOME%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%GRADLE_ZIP%' -DestinationPath '%WRAPPER_DIR%' -Force"
  IF ERRORLEVEL 1 EXIT /B 1
)

CALL "%GRADLE_HOME%\bin\gradle.bat" %*
