#!/bin/bash
echo "编译项目..."
mvn clean package -DskipTests
echo "编译完成！"