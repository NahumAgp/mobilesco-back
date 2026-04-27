@echo off
setlocal

set "MAVEN_USER_HOME=%~dp0.mvn-home"

if "%~1"=="" (
  call "%~dp0mvnw.cmd" test
) else (
  call "%~dp0mvnw.cmd" %*
)

exit /b %ERRORLEVEL%
