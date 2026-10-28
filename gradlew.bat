@echo off
setlocal
set "JAVA_HOME=C:\Users\SatishN3\.jdks\ms-17.0.18"
set "GRADLE_HOME=%USERPROFILE%\.gradle\wrapper\dists\gradle-8.14-bin\38aieal9i53h9rfe7vjup95b9\gradle-8.14"

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo Compatible JDK not found at "%JAVA_HOME%".
  exit /b 1
)

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  echo Local Gradle 8.14 distribution not found at "%GRADLE_HOME%".
  exit /b 1
)

call "%GRADLE_HOME%\bin\gradle.bat" %*
endlocal
