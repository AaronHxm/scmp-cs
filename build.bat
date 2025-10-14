@echo off
echo 正在编译和打包抢单辅助程序...
echo.

echo [1/3] 清理之前的构建...
call mvn clean

echo.
echo [2/3] 编译项目并创建jar包...
call mvn compile package -DskipTests

echo.
echo [3/3] 生成exe可执行文件...
call mvn launch4j:launch4j

echo.
echo 打包完成！
echo 生成的文件位于 target 目录下：
echo - order-grabber-1.0.0-standalone.jar (包含所有依赖的jar包)
echo - order-grabber.exe (Windows可执行文件)
echo.
pause