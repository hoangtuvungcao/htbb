@echo off
REM Mo cong Windows Firewall cho HTBB P2P (chay "Run as administrator"). Doi so neu sv.p2p_port / sv.port khac.
set P2P=47000
set GAME=19129
netsh advfirewall firewall add rule name="HTBB Peerova UDP %P2P%" dir=in action=allow protocol=UDP localport=%P2P%
netsh advfirewall firewall add rule name="HTBB Peerova TCP %P2P%" dir=in action=allow protocol=TCP localport=%P2P%
REM J2ME (khong P2P) van vao thang cong game - bo dong duoi neu khong con nguoi choi J2ME
netsh advfirewall firewall add rule name="HTBB game TCP %GAME% (J2ME)" dir=in action=allow protocol=TCP localport=%GAME%
REM KHONG mo 8023 (login server noi bo) va 3306 (MariaDB)
pause
