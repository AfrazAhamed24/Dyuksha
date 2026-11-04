@REM ============================================================
@REM Maven Wrapper Script for Windows
@REM Dyuksha — AI-Native Emergency Response Platform
@REM ============================================================
@echo off
setlocal enableDelayedExpansion

set MAVEN_WRAPPER_PROPERTIES=%~dp0.mvn\wrapper\maven-wrapper.properties
set MAVEN_USER_HOME=%USERPROFILE%\.m2
set MAVEN_WRAPPER_CACHE=%MAVEN_USER_HOME%\wrapper\dists

@REM --- Read distributionUrl from properties file ---
for /f "tokens=1,* delims==" %%a in (%MAVEN_WRAPPER_PROPERTIES%) do (
    if "%%a"=="distributionUrl" set MAVEN_DIST_URL=%%b
)

@REM --- Derive the directory name from the URL ---
for %%f in (%MAVEN_DIST_URL%) do set MAVEN_DIST_FILE=%%~nxf
set MAVEN_DIST_DIR=%MAVEN_DIST_FILE:-bin.zip=%

set MAVEN_HOME=%MAVEN_WRAPPER_CACHE%\%MAVEN_DIST_DIR%\%MAVEN_DIST_DIR%

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Downloading Maven from %MAVEN_DIST_URL% ...
    if not exist "%MAVEN_WRAPPER_CACHE%\%MAVEN_DIST_DIR%" (
        mkdir "%MAVEN_WRAPPER_CACHE%\%MAVEN_DIST_DIR%"
    )
    set MAVEN_ZIP=%MAVEN_WRAPPER_CACHE%\%MAVEN_DIST_DIR%\%MAVEN_DIST_FILE%
    powershell -Command "Invoke-WebRequest -Uri '%MAVEN_DIST_URL%' -OutFile '!MAVEN_ZIP!' -UseBasicParsing"
    if errorlevel 1 (
        echo ERROR: Failed to download Maven.
        exit /b 1
    )
    powershell -Command "Expand-Archive -Path '!MAVEN_ZIP!' -DestinationPath '%MAVEN_WRAPPER_CACHE%\%MAVEN_DIST_DIR%' -Force"
    if errorlevel 1 (
        echo ERROR: Failed to extract Maven.
        exit /b 1
    )
    echo Maven downloaded and extracted.
)

set PATH=%MAVEN_HOME%\bin;%PATH%
call "%MAVEN_HOME%\bin\mvn.cmd" %*
