# Chạy peerova server như dịch vụ nền trên Windows (khởi động cùng máy, tự chạy lại khi lỗi).
# PowerShell (Run as Administrator):  .\install-windows.ps1 -Dir C:\peerova
param([string]$Dir = "C:\peerova", [int]$Port = 47000)
$ErrorActionPreference = "Stop"
New-Item -ItemType Directory -Force -Path $Dir | Out-Null
Copy-Item -Force "$PSScriptRoot\peerova.exe" "$Dir\peerova.exe"
if (-not (Test-Path "$Dir\peerova.json")) { Copy-Item "$PSScriptRoot\peerova.json" "$Dir\peerova.json" }
# Firewall: only the peerova port (server is 'dark': silent to anything without a valid ticket).
New-NetFirewallRule -DisplayName "peerova UDP $Port" -Direction Inbound -Protocol UDP -LocalPort $Port -Action Allow -ErrorAction SilentlyContinue | Out-Null
New-NetFirewallRule -DisplayName "peerova TCP $Port (fallback)" -Direction Inbound -Protocol TCP -LocalPort $Port -Action Allow -ErrorAction SilentlyContinue | Out-Null
$action  = New-ScheduledTaskAction -Execute "$Dir\peerova.exe" -Argument "server --config `"$Dir\peerova.json`"" -WorkingDirectory $Dir
$trigger = New-ScheduledTaskTrigger -AtStartup
$settings = New-ScheduledTaskSettingsSet -RestartCount 999 -RestartInterval (New-TimeSpan -Minutes 1) -ExecutionTimeLimit ([TimeSpan]::Zero) -AllowStartIfOnBatteries
Register-ScheduledTask -TaskName "peerova" -Action $action -Trigger $trigger -Settings $settings -User "SYSTEM" -RunLevel Highest -Force | Out-Null
Start-ScheduledTask -TaskName "peerova"
Write-Host "peerova đang chạy. Ticket: $Dir\ticket.txt (nếu peerova.json có ticket_file). Gỡ: Unregister-ScheduledTask -TaskName peerova"
