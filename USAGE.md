# 使用说明

## 快速开始

### 1. 编译项目
```bash
# Windows
build.bat

# Linux/Mac
chmod +x build.sh && ./build.sh
```

### 2. 运行程序
```bash
# Windows
start.bat

# Linux/Mac
chmod +x start.sh && ./start.sh
```

## 功能说明

### 密码验证
- 程序启动后首先显示密码输入页面
- 密码格式：月份英文缩写 + LAB2025 + 月份英文缩写
- 例如10月份的密码是：`OctLAB2025Oct`
- 加密算法根据日期自动选择（Base64/MD5/SHA256）

### 主界面功能

#### 查询订单
1. 设置预期天数范围（可选）
2. 选择合同号（A-Z多选）
3. 点击"查询"按钮

#### 抢单操作
1. 在订单表格中勾选要抢的订单
2. 选择抢单方式：
   - **立即抢单**：马上执行抢单
   - **定时抢单**：设置时间后自动执行

#### 日志查看
- 实时日志显示在界面下方
- 点击"打开文件夹"查看历史日志文件
- 日志按年月/日期分类存储

## 配置说明

### application.yml 配置项
- `app.encryption.fixed-string`: 密码固定字符串
- `app.api.base-url`: API服务地址  
- `app.scheduler.token-refresh-interval`: Token刷新间隔

### 日志配置
- 日志文件位置：`logs/年月/日期.log`
- 支持控制台和文件双重输出
- 错误日志单独存储

## 开发说明

### 项目结构
```
src/main/java/com/scmp/ordergrabber/
├── OrderGrabberApplication.java    # 主启动类
├── config/AppConfig.java           # 配置类
├── service/                        # 业务服务
├── ui/                            # JavaFX界面
├── model/                         # 数据模型
└── controller/                    # REST控制器
```

### 技术栈
- Java 11+
- JavaFX 17（图形界面）
- Spring Boot 2.7（应用框架）
- Logback（日志系统）
- Maven（构建工具）