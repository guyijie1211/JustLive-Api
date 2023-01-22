#!/bin/bash
#当前路径，使用第一个参数
currentPath=$1
#如果第一个参数为空，则pwd(运行脚本的当前目录)作为当前路径
if [ ! -n "$currentPath" ]; then
  currentPath=$PWD
fi
#打印当前路径
echo $currentPath
#存放pid的文件路径配置
pidFile="$currentPath/test.pid"
#jar包的路径配置
jarPath="$currentPath/target/mixed-live-back.jar"
#当pid文件存在时，读出pid文件的内容，kill掉该pid的进程
if [ -f $pidFile ]; then
    echo "后台正在运行!"
    PID=$(cat $pidFile)           # 将PID从文件中读取，并作为一个变量
    echo "上次启动进程:$PID"
   #杀掉该进程
    kill -9 $PID
    echo "exec kill $PID success!"
fi
echo "执行启动java程序指令"
#cd进去currentPath目录，在该目录下执行java -jar指令
echo "cd currentPath: $currentPath"
cd $currentPath
#部署jar包的指令，nohup是指忽略挂起  > /dev/null是把nohup的输入丢到一个空设备，即直接丢弃的意思
# 2>&1 即所有类型日志的输出 &表示后台运行，后面>表示把pid保存到当前目录的test.pid文件中
#便于后续杀进程重启
echo "$jarPath"
nohup java -jar $jarPath --spring.profiles.active=prod > /dev/null 2>&1 & echo $! >"$currentPath/test.pid"
echo "执行启动java程序指令完成!"
