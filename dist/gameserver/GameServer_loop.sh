#!/bin/bash

# ======== JVM settings =======
# Set heap min/max to same size for consistent results
# одинаковый размер памяти для Xms и Xmx, JVM пытается удержать размер heap'а минимальным, и если его нужно меньше, чем в Xmx - гоняет GC понапрасну
javaopts="$javaopts -Xms5048m"
javaopts="$javaopts -Xmx5048m"

# The important setting in 64-bits with the Sun JVM is -XX:+UseCompressedOops as it saves memory and improves performance
javaopts="$javaopts -XX:+UseCompressedOops"
javaopts="$javaopts -XX:+UseFastAccessorMethods"
javaopts="$javaopts -XX:+UseConcMarkSweepGC"

# Logging
# javaopts="$javaopts -XX:+PrintGCDetails"
# javaopts="$javaopts -XX:+PrintGCDateStamps"
# javaopts="$javaopts -XX:+PrintGCApplicationStoppedTime"
# javaopts="$javaopts -XX:+PrintGCTimeStamps"
# javaopts="$javaopts -XX:+PrintGC"
# javaopts="$javaopts -Xloggc:./log/game/garbage_collector.log"

# не изменять
java_settings=" -Dfile.encoding=UTF-8"
java_settings="$java_settings -Djava.net.preferIPv4Stack=true"

# Java profiler (jvisualVM)
#java_settings="$java_settings -Djava.rmi.server.hostname=IP -Dcom.sun.management.jmxremote.port=PORT -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"

# ==========================================

# exit codes of GameServer:
#  0 normal shutdown
#  2 reboot attempt


while :;
do
	java -server $java_settings $javaopts -cp ../serverslibs/smrt-core.jar:../serverslibs/smrt.jar:config/xml:../serverslibs/*: ru.akumu.smartguard.SmartGuard l2p.gameserver.GameServer > log/stdout.log 2>&1
	[ $? -ne 2 ] && break
	sleep 30;
done