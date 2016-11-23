@echo off

set PROJECT_DIR=Demo
set SDK_PATH=C:\Users\admin\AppData\Local\Android\sdk
set PATH=%SDK_PATH%\tools;%SDK_PATH%\build-tools\24.0.3;%PATH%
set ANDROID_JAR=%SDK_PATH%\platforms\android-24\android.jar
set HTTP_LEGACY_JAR=%SDK_PATH%\platforms\android-24\optional\org.apache.http.legacy.jar
set SDKLIB_JAR=%SDK_PATH%\tools\lib\sdklib.jar
set KEYSTORE_URL=Demo.keystore
set KEYSTORE_PASS=123456
set KEYSTORE_ALIAS=Demo.keystore

pushd %PROJECT_DIR%

rd /s /q bin
md bin

rem Compile *.java
javac -d bin -encoding utf8 -source 1.7 -target 1.7 -bootclasspath %ANDROID_JAR% -classpath %HTTP_LEGACY_JAR% src\com\example\demo\*.java

rem Build the dex
cmd /c dx --dex --output=bin\classex.dex bin

rem Build the package
aapt p -f -M AndroidManifest.xml -I %ANDROID_JAR% -F bin\Demo.ap_ --min-sdk-version 4

rem Build the apk
java -classpath %SDKLIB_JAR% com.android.sdklib.build.ApkBuilderMain bin\Demo.apk -u -z bin\Demo.ap_ -f bin\classex.dex -rf src

popd

rem Sign the apk
rem You need to generate Demo.keystore by running generate_keystore.bat
jarsigner -keystore %KEYSTORE_URL% -storepass %KEYSTORE_PASS% -signedjar Demo.apk %PROJECT_DIR%\bin\Demo.apk %KEYSTORE_ALIAS%
