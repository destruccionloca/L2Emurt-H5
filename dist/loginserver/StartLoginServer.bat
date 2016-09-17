@echo off
color 04
:start
title L2JVC AuthServer
echo Starting L2JVC AuthServer.
echo.

SET java_opts=%java_opts% -Xms32m
SET java_opts=%java_opts% -Xmx64m

SET java_opts=%java_opts% -XX:+AggressiveOpts

SET java_settings=%java_settings% -Dfile.encoding=UTF-8
SET java_settings=%java_settings% -Djava.net.preferIPv4Stack=true

java -server %java_settings% %java_opts% -cp config/xml;../serverslibs/* l2p.loginserver.AuthServer
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
