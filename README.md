# 抢单辅助程序

## 项目简介

这是一个基于Java + JavaFX开发的抢单辅助程序，支持密码验证、自动登录、订单查询和抢单功能。

## 功能特性

- 🔐 **密码验证系统**: 支持三种加密算法轮换（Base64、MD5、SHA256）
- 🚀 **自动登录**: Token自动获取和保活机制（每10分钟刷新）
- 🔍 **订单查询**: 支持按预期天数和合同号筛选
- ⚡ **立即抢单**: 选中订单后立即执行抢单
- ⏰ **定时抢单**: 支持设定时间自动抢单，智能预登录
- 📊 **日志系统**: 完整的操作日志记录和文件管理
- 🎨 **现代UI**: 基于JavaFX的直观用户界面

## 系统要求

- Java 11 或更高版本
- Windows 操作系统
- 网络连接

## 快速开始

### 方式一：直接运行exe（推荐）

1. 下载 `order-grabber.exe`
2. 双击运行即可

### 方式二：从源码构建

1. 确保安装了 Java 11+ 和 Maven
2. 克隆项目到本地
3. 运行构建脚本：
   ```bash
   # Windows
   build.bat
   
   # 或者手动执行
   mvn clean package launch4j:launch4j
   ```
4. 在 `target` 目录下找到生成的 `order-grabber.exe`

## 使用说明

### 1. 密码验证

程序启动后会显示密码输入界面：
- 输入正确的密码（根据当前日期使用不同加密算法）
- 密码格式：`月份英文缩写 + 固定字符串 + 月份英文缩写`
- 例如：`OctLAB2025Oct`（10月份的密码）

### 2. 主界面功能

登录成功后进入主界面，包含以下功能区域：

#### 查询区域
- 设置预期天数范围（大于/小于）
- 选择合同号（A-Z多选）
- 点击"查询"获取订单列表

#### 订单表格
- 显示查询结果
- 支持复选框多选
- 包含合同号、姓名、逾期天数、备注等字段

#### 日志表格
- 显示操作历史
- 包含时间、重试次数、结果等信息

#### 抢单操作
- **立即抢单**: 对选中的订单立即执行抢单
- **定时抢单**: 设定执行时间，支持智能预登录

#### 日志管理
- 实时日志显示
- "打开文件夹"按钮可查看日志文件

## 技术架构

- **前端**: JavaFX 17.0.2
- **后端**: Spring Boot 2.7.0
- **日志**: Logback
- **HTTP客户端**: Apache HttpClient
- **JSON处理**: Jackson
- **加密**: Commons Codec
- **构建工具**: Maven
- **打包工具**: Launch4j

## 日志文件结构

```
logs/
├── 2024_10/
│   ├── 2024_10_13.log
│   ├── 2024_10_14.log
│   └── ...
└── 2024_11/
    ├── 2024_11_01.log
    └── ...
```

## 配置文件

主要配置在 `src/main/resources/application.yml`：

```yaml
app:
  name: 抢单辅助程序
  version: 1.0.0
  password:
    fixed-string: LAB2025
  api:
    base-url: http://localhost:8080/api
    login: /login
    token-validate: /token/validate
    orders-query: /orders/query
    orders-grab: /orders/grab
```

## 开发说明

### 项目结构

```
src/main/java/com/scmp/ordergrabber/
├── OrderGrabberApplication.java    # 主启动类
├── config/
│   └── AppConfig.java             # 应用配置
├── service/
│   ├── EncryptionService.java     # 加密服务
│   ├── ApiService.java           # API服务
│   ├── TokenService.java         # Token管理
│   └── OrderService.java         # 订单服务
├── ui/
│   ├── LoginStage.java           # 登录界面
│   └── MainStage.java            # 主界面
└── model/
    ├── dto/                      # 数据传输对象
    └── entity/                   # 实体类
```

### 智能定时抢单特性

- 当设定时间间隔 ≥ 15分钟时，系统会在执行前13分钟自动登录
- 预登录成功后会按照设定的规则自动查询订单
- 到达设定时间时直接对查询结果执行抢单操作
- 支持倒计时显示和随时停止功能

## 故障排除

### 常见问题

1. **程序无法启动**
   - 检查是否安装了 Java 11+
   - 确认系统环境变量配置正确

2. **密码验证失败**
   - 确认当前日期
   - 检查密码格式是否正确

3. **网络连接问题**
   - 检查网络连接
   - 确认API基础URL配置正确

### 日志查看

程序运行时会在以下位置生成日志：
- 控制台输出（实时）
- 文件输出：`logs/年_月/年_月_日.log`

通过主界面的"打开文件夹"按钮可以快速访问日志目录。

## 许可证

本项目仅供内部使用。

## 联系方式

如有问题请联系开发团队。