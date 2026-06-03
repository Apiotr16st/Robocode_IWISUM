@echo off
set PROJ_DIR=%~dp0
set ROBO_DIR=C:\robocode
set DATA_DIR=%PROJ_DIR%data\AdaptiveBot.data

if not exist "%ROBO_DIR%\robots\mybots" mkdir "%ROBO_DIR%\robots\mybots"
copy /Y "%PROJ_DIR%target\classes\mybots\*.class" "%ROBO_DIR%\robots\mybots\" >nul

if exist "%DATA_DIR%\qtable.dat" (
    if not exist "%ROBO_DIR%\robots\mybots\AdaptiveBot.data" mkdir "%ROBO_DIR%\robots\mybots\AdaptiveBot.data"
    copy /Y "%DATA_DIR%\qtable.dat" "%ROBO_DIR%\robots\mybots\AdaptiveBot.data\" >nul
)

powershell -Command "Get-ChildItem '%ROBO_DIR%\robots\mybots\*.class' | ForEach-Object { $_.LastWriteTime = [DateTime]::Now }" >nul 2>&1

java -Xmx512M -cp "%ROBO_DIR%\libs\*" --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.desktop/javax.swing.text=ALL-UNNAMED --add-opens=java.desktop/sun.awt=ALL-UNNAMED robocode.Robocode -cwd "%ROBO_DIR%" -battle "%PROJ_DIR%battles\evaluation.battle" -results "%PROJ_DIR%evaluation_results.txt"