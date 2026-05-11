@REM
@REM Maven wrapper batch script for Windows
@REM

@echo off
setlocal enabledelayedexpansion

set MAVEN_PROJECTBASEDIR=%~dp0

if "%JAVA_HOME%"=="" (
    for /f "tokens=*" %%i in ('where java.exe') do set JAVA_HOME=%%~dpi..
    if "%JAVA_HOME%"=="" exit /b 1
)

set JAVA_EXE=%JAVA_HOME%\bin\java.exe

"%JAVA_EXE%" %MAVEN_OPTS% ^
  -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" ^
  org.apache.maven.wrapper.MavenWrapperMain %*

endlocal
