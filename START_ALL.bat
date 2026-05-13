@echo off
chcp 65001 >nul
cls
echo ==============================================
echo          一键启动 KnowPivot 所有服务
echo ==============================================
echo.

:: 1. knowpivot-agent
start "knowpivot-agent" cmd /k ^
cd /d "%~dp0knowpivot-agent" ^
& .venv\Scripts\activate.bat ^
& python main.py

:: 2. knowpivot-embedding
start "knowpivot-embedding" cmd /k ^
cd /d "%~dp0knowpivot-embedding" ^
& .venv\Scripts\activate.bat ^
& python main.py

:: 3. knowpivot-task (修复版，100%能启动)
start "knowpivot-task" cmd /k ^
cd /d "%~dp0knowpivot-task" ^
& .venv\Scripts\activate.bat ^
& python main.py

:: 4. 前端 npm run dev
start "knowpivot-web" cmd /k ^
cd /d "%~dp0knowpivot-web" ^
& npm run dev

echo 所有服务已启动
echo 关闭窗口即可停止对应服务
echo.
pause >nul