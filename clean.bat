@REM 用于清理编译生成的文件，以及生成的gzip文件
@echo off
REM 设置当前目录为批处理脚本所在目录，避免因不同执行环境导致的路径问题
cd /d %~dp0

REM 编译Delete.java文件，假设它位于src目录下的utils文件夹中
javac ./src/utils/Delete.java

REM 检查编译是否成功
if %errorlevel% neq 0 (
    echo Compilation failed.
    exit /b %errorlevel%
)

REM 使用指定的类路径（src目录，用src表示）运行utils包下的Delete类
java -cp ./src utils.Delete