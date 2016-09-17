@echo off
color 04
title L2JVC GameServer
:start
echo Starting L2JVC GameServer.
echo.
REM одинаковый размер памяти для Xms и Xmx, JVM пытается удержать размер heap'а минимальным, и если его нужно меньше, чем в Xmx - гоняет GC понапрасну
SET java_opts=%java_opts% -Xms2048m
SET java_opts=%java_opts% -Xmx2048m
REM Альтернативные настройки самой JVM.
SET java_opts=%java_opts% -XX:+DoEscapeAnalysis
REM Циклы заполнения/копирования массивов заменяются на прямые машинные инструкции для ускорения работы.
SET java_opts=%java_opts% -XX:+OptimizeFill
REM Опция, устраняет лишние блокировки путем их объединения.
SET java_opts=%java_opts% -XX:+EliminateLocks
REM Позволяет расширить диапазон кешируемых значений для целых типов при старте виртуальной машины.
SET java_opts=%java_opts% -XX:AutoBoxCacheMax=65536

SET java_opts=%java_opts% -XX:+UseConcMarkSweepGC
SET java_opts=%java_opts% -XX:+CMSClassUnloadingEnabled
SET java_opts=%java_opts% -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses

SET java_settings=%java_settings% -Dfile.encoding=UTF-8
SET java_settings=%java_settings% -Djava.net.preferIPv4Stack=true

java -version:1.8 -server %java_settings% %java_opts% -cp config/xml;../serverslibs/*; l2p.gameserver.GameServer

REM Debug ...
REM java -Dfile.encoding=UTF-8 -cp config;./* -Xmx1G -Xnoclassgc -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=7456 l2p.gameserver.GameServer

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Server restarted ...
echo.
goto start
:error
echo.
echo Server terminated abnormaly ...
echo.
:end
echo.
echo Server terminated ...
echo.

pause
