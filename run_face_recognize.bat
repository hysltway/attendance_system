@echo off
REM 创建日志文件
echo 开始执行批处理脚本，参数：%1 > face_recognize_log.txt
echo 当前工作目录：%CD% >> face_recognize_log.txt

REM 激活conda环境并执行人脸识别脚本
call D:\Software\Development\anaconda3\Scripts\activate.bat face >> face_recognize_log.txt 2>&1

echo conda环境激活完成，开始执行Python脚本 >> face_recognize_log.txt
echo 脚本路径：%CD%\face_recognition\recognize_face.py >> face_recognize_log.txt
echo 图像路径：%1 >> face_recognize_log.txt

REM 检查脚本文件是否存在
if not exist "%CD%\face_recognition\recognize_face.py" (
    echo 错误：脚本文件不存在 >> face_recognize_log.txt
    exit /b 1
)

REM 切换到face_recognition目录
cd face_recognition
echo 切换到目录：%CD% >> ..\face_recognize_log.txt

REM 检查数据文件是否存在
if not exist "data\data_dlib\shape_predictor_68_face_landmarks.dat" (
    echo 错误：shape_predictor_68_face_landmarks.dat文件不存在 >> ..\face_recognize_log.txt
    cd ..
    exit /b 1
)

REM 创建临时文件保存Python输出
set TEMP_OUTPUT=%TEMP%\py_output_%RANDOM%.txt

REM 执行Python脚本，使用绝对路径，将输出保存到临时文件
D:\Software\Development\anaconda3\envs\face\python.exe "recognize_face.py" "%1" > "%TEMP_OUTPUT%" 2>> ..\face_recognize_log.txt
set RESULT=%ERRORLEVEL%

REM 显示Python输出以便捕获
type "%TEMP_OUTPUT%"

REM 为了调试，将Python输出也记录到日志
echo Python输出内容： >> ..\face_recognize_log.txt
type "%TEMP_OUTPUT%" >> ..\face_recognize_log.txt

REM 删除临时输出文件
del "%TEMP_OUTPUT%"

REM 返回原目录
cd ..
echo 返回目录：%CD% >> face_recognize_log.txt

echo Python脚本执行完成，退出代码：%RESULT% >> face_recognize_log.txt
exit /b %RESULT% 