@echo off
color 04
title L2JVC: Game Server Registration...
:start
echo Starting Game Server Registration.
echo.
java -server -Xms64m -Xmx64m -cp config/xml;../serverslibs/*; l2p.loginserver.GameServerRegister

pause
