
@echo off
REM Close any previous Python HTTP server and Firefox Developer Edition
taskkill /F /IM python.exe >nul 2>&1
taskkill /F /IM firefox.exe >nul 2>&1
REM Start a simple HTTP server for Planet Game web folder
REM Requires Python installed and in PATH
REM Opens mainmenu.html in Firefox Developer Edition automatically

cd "web"
start "" "C:\Program Files\Firefox Developer Edition\firefox.exe" "http://localhost:8080/index.html"
python -m http.server 8080

REM Firefox Developer Edition will open http://localhost:8080/index.html automatically
