# WebServer
计算机网络课设-Web服务器的设计与实现

## 设计相关

### 设计目的

1. 熟悉开发工具(Visual Studio、C/C++、Java 等)的基本操作。
2. 掌握 http 协议的工作原理。
3. 掌握多线程编程。
4. 对于 Socket 编程建立初步的概念。
5. 掌握对文件的网络传输操作。

### 设计要求

1. 不限平台，熟悉 Socket API 主要函数的使用。
2. 实现一个简单的基于 http 协议的 WEB 服务器。
3. 实现对服务器运行状态的监控。

### 设计内容

**请注意:**
1. 此处 web 服务器，只是对 HTTP 请求予以应答:IE浏览器访问本服务器，请求当前服务器中的某静态网页文件(html 或 htm 文件等)，服务器端查询服务器端相应的路径下该网页是否存在，如存在，则利用当前的 TCP 连接传递该网页文件,如果不存在,则返回 404 错误提示。
2. 不涉及动态网页的解析，如.asp、aspx.、.php、.jsp 等。
3. 应考虑服务器的多客户端访问问题，参见:多线程机制、异步的套接字 I/0机制或套接字链表等等。

### 思考题

1. 该服务器的工作模式是什么？
2. 如何对其进行测试，还有哪些可以完善的功能？
3. 有什么办法可以提高它的性能？

## git相关

### git 配置工具

1. git config的使用帮助信息
   - 查看git config的命令列表使用说明
     ```sh
     git config
     ```
     
   - 查看git config的详细说明文档
     ```sh
     git config --help
     ```

2. git 配置举例
   - 配置用户和电子邮件信息
     ```sh
     git config --global user.name "user"
     git config --global user.email "email"
     ```

3. 删除配置项
   - 删除配置
     ```sh
     git config --local --unset user.name
     git config --local --unset user.email
     ```

### git配置层级
每一个级别会覆盖上一级别的配置，如当前仓库下的 .git/config 的配置变量会覆盖 ~/.gitconfig 中的配置变量。

1. system级：对所有用户普遍适用的配置
   ```sh
   git config --system
   ```

2. global 级别：对当前用户适用的配置文件
   ```sh
   git config --global
   ```

3. local级别： 对当前仓库适用的配置文件  
   ```sh
   git config --local
   ```

### git 配置查看
1. 查看git的配置列表
   ```sh
   git config --list
   ```

    
2. 查看特定作用域的配置
   ```sh
   git config --global --list
   ```

3. 查看配置文件及文件中的配置项
   ```sh
   git config --list --show-origin
   ```

4. 直接查看某个环境变量的设定
   ```sh
   git config user.name
   ```

### 常用配置
1. 用户信息
   ```sh
   git config --global user.name "xiaoyuzhou1994"
   git config --global user.email "xiaoyuzhou1994@sina.com"
   ```

2. 文本编辑器
   ```sh
   git config --system core.editor "code --wait"
   ```

3. 差异分析工具
   ```sh
   git mergetool --tool-help
   git config --global merge.tool vimdiff
   ```

4. 中文显示
   ```sh
   git config --global core.quotepath false
   git config --global gui.encoding utf-8
   git config --global i18n.commit.encoding utf-8
   git config --global i18n.logoutputencoding utf-8
   export LESSCHARSET=utf-8
   ```

### 遇到的报错

1. 当出现could'nt connect to server时，输入
```sh
git config --global --unset http.proxy
git config --global --unset https.proxy
```
即可

2. 当运行 Delete.java 的时候，如果产生了`错误: 找不到或无法加载主类 utils.Delete`的报错，是由于 JVM 会按照默认的类路径设置来查找类，可能不包含 Delete.java 所在目录，所以 JVM 无法找到它，因此会报找不到的错误。
解决办法是：切换到 `src` 目录中，运行 `java -classpath . utils.Delete`。
为此，我们编写了一个batch脚本clean.bat便于删除编译出来的文件。