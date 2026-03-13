@Echo off
REM This script is used for updating the version of the whole plugin.
REM
REM Usage: ./update-version.sh VERSION
REM Example: ./update-version.sh 3.1.0-SNAPSHOT

REM Note that automatically updating the version only works when the versions
REM in the files have not been manually edited and actually match the current
REM version.

SET VERSION="%*"

if "%~1"=="" goto blank

call mvn org.eclipse.tycho:tycho-versions-plugin:4.0.6:set-version -DnewVersion="%VERSION%" -Dtycho.mode=maven
call mvn org.eclipse.tycho:tycho-versions-plugin:4.0.6:update-eclipse-metadata -DnewVersion="%VERSION%" -Dtycho.mode=maven
goto end

:blank
echo "update-version.cmd VERSION"

:end