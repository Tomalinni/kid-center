@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  kid-center startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and KID_CENTER_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=-Xmx1g -Xms512m -Xss1m -XX:+UseG1GC -XX:+UseStringDeduplication -Xloggc:garbage-collection.log

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windowz variants

if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*
goto execute

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\kidcenter-0.0.1-SNAPSHOT.jar;%APP_HOME%\lib\spring-boot-starter-aop-1.3.5.RELEASE.jar;%APP_HOME%\lib\spring-boot-starter-data-jpa-1.3.5.RELEASE.jar;%APP_HOME%\lib\hibernate-entitymanager-5.0.7.Final.jar;%APP_HOME%\lib\hibernate-core-5.0.7.Final.jar;%APP_HOME%\lib\hibernate-java8-5.0.7.Final.jar;%APP_HOME%\lib\jackson-datatype-jsr310-2.6.6.jar;%APP_HOME%\lib\jackson-module-kotlin-2.6.7.jar;%APP_HOME%\lib\spring-boot-devtools-1.3.5.RELEASE.jar;%APP_HOME%\lib\spring-boot-starter-security-1.3.5.RELEASE.jar;%APP_HOME%\lib\spring-session-1.0.2.RELEASE.jar;%APP_HOME%\lib\spring-boot-starter-web-1.3.5.RELEASE.jar;%APP_HOME%\lib\kotlin-stdlib-1.0.5.jar;%APP_HOME%\lib\kotlin-reflect-1.0.5.jar;%APP_HOME%\lib\jjwt-0.6.0.jar;%APP_HOME%\lib\excelmapper-0.1.2.jar;%APP_HOME%\lib\commons-io-2.5.jar;%APP_HOME%\lib\commons-fileupload-1.3.2.jar;%APP_HOME%\lib\httpclient-4.5.2.jar;%APP_HOME%\lib\commons-lang3-3.4.jar;%APP_HOME%\lib\flyway-core-4.0.3.jar;%APP_HOME%\lib\postgresql-9.4.1208.jre7.jar;%APP_HOME%\lib\spring-boot-starter-1.3.5.RELEASE.jar;%APP_HOME%\lib\spring-aop-4.2.6.RELEASE.jar;%APP_HOME%\lib\aspectjweaver-1.8.9.jar;%APP_HOME%\lib\spring-boot-starter-jdbc-1.3.5.RELEASE.jar;%APP_HOME%\lib\javax.transaction-api-1.2.jar;%APP_HOME%\lib\spring-data-jpa-1.9.4.RELEASE.jar;%APP_HOME%\lib\spring-aspects-4.2.6.RELEASE.jar;%APP_HOME%\lib\jboss-logging-3.3.0.Final.jar;%APP_HOME%\lib\dom4j-1.6.1.jar;%APP_HOME%\lib\hibernate-commons-annotations-5.0.1.Final.jar;%APP_HOME%\lib\hibernate-jpa-2.1-api-1.0.0.Final.jar;%APP_HOME%\lib\javassist-3.18.1-GA.jar;%APP_HOME%\lib\geronimo-jta_1.1_spec-1.1.1.jar;%APP_HOME%\lib\antlr-2.7.7.jar;%APP_HOME%\lib\jandex-2.0.0.Final.jar;%APP_HOME%\lib\jackson-core-2.6.6.jar;%APP_HOME%\lib\jackson-databind-2.6.6.jar;%APP_HOME%\lib\jackson-annotations-2.6.6.jar;%APP_HOME%\lib\spring-boot-1.3.5.RELEASE.jar;%APP_HOME%\lib\spring-boot-autoconfigure-1.3.5.RELEASE.jar;%APP_HOME%\lib\spring-security-config-4.0.4.RELEASE.jar;%APP_HOME%\lib\spring-security-web-4.0.4.RELEASE.jar;%APP_HOME%\lib\spring-boot-starter-tomcat-1.3.5.RELEASE.jar;%APP_HOME%\lib\spring-boot-starter-validation-1.3.5.RELEASE.jar;%APP_HOME%\lib\spring-web-4.2.6.RELEASE.jar;%APP_HOME%\lib\spring-webmvc-4.2.6.RELEASE.jar;%APP_HOME%\lib\kotlin-runtime-1.0.5.jar;%APP_HOME%\lib\commons-beanutils-core-1.8.3.jar;%APP_HOME%\lib\poi-3.10-FINAL.jar;%APP_HOME%\lib\annotations-13.0.jar;%APP_HOME%\lib\poi-ooxml-3.10-FINAL.jar;%APP_HOME%\lib\joda-time-2.8.2.jar;%APP_HOME%\lib\httpcore-4.4.4.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\commons-codec-1.9.jar;%APP_HOME%\lib\spring-boot-starter-logging-1.3.5.RELEASE.jar;%APP_HOME%\lib\spring-core-4.2.6.RELEASE.jar;%APP_HOME%\lib\snakeyaml-1.16.jar;%APP_HOME%\lib\aopalliance-1.0.jar;%APP_HOME%\lib\spring-beans-4.2.6.RELEASE.jar;%APP_HOME%\lib\tomcat-jdbc-8.0.33.jar;%APP_HOME%\lib\spring-jdbc-4.2.6.RELEASE.jar;%APP_HOME%\lib\spring-data-commons-1.11.4.RELEASE.jar;%APP_HOME%\lib\spring-orm-4.2.6.RELEASE.jar;%APP_HOME%\lib\spring-context-4.2.6.RELEASE.jar;%APP_HOME%\lib\spring-tx-4.2.6.RELEASE.jar;%APP_HOME%\lib\slf4j-api-1.7.21.jar;%APP_HOME%\lib\jcl-over-slf4j-1.7.21.jar;%APP_HOME%\lib\xml-apis-1.0.b2.jar;%APP_HOME%\lib\spring-security-core-4.0.4.RELEASE.jar;%APP_HOME%\lib\spring-expression-4.2.6.RELEASE.jar;%APP_HOME%\lib\tomcat-embed-core-8.0.33.jar;%APP_HOME%\lib\tomcat-embed-el-8.0.33.jar;%APP_HOME%\lib\tomcat-embed-logging-juli-8.0.33.jar;%APP_HOME%\lib\tomcat-embed-websocket-8.0.33.jar;%APP_HOME%\lib\hibernate-validator-5.2.4.Final.jar;%APP_HOME%\lib\poi-ooxml-schemas-3.10-FINAL.jar;%APP_HOME%\lib\logback-classic-1.1.7.jar;%APP_HOME%\lib\jul-to-slf4j-1.7.21.jar;%APP_HOME%\lib\log4j-over-slf4j-1.7.21.jar;%APP_HOME%\lib\tomcat-juli-8.0.33.jar;%APP_HOME%\lib\validation-api-1.1.0.Final.jar;%APP_HOME%\lib\classmate-1.1.0.jar;%APP_HOME%\lib\xmlbeans-2.3.0.jar;%APP_HOME%\lib\logback-core-1.1.7.jar;%APP_HOME%\lib\stax-api-1.0.1.jar

@rem Execute kid-center
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %KID_CENTER_OPTS%  -classpath "%CLASSPATH%" com.joins.kidcenter.KidCenterApplication --spring.profiles.active=prod %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable KID_CENTER_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%KID_CENTER_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
