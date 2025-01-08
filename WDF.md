## 项目结构分析

### 核心类文件

#### 1. Main.java
主程序入口和GUI界面实现
- 继承自 JavaFX Application
- 实现图形用户界面
- 处理用户交互
- 整合其他组件的功能
- 显示对比结果
- 提供相似度结果的格式化显示

#### 2. Launcher.java
程序启动器
- 提供独立的主入口点
- 处理 JavaFX 运行时环境
- 确保程序正确启动
- 作为 jar 包的入口类

#### 3. FileUtil.java
文件处理工具类
- 文件夹遍历和文件收集
- 文件扩展名过滤
- MD5哈希值计算
- 重复文件分组
- 压缩包处理的协调

#### 4. ZipUtil.java
压缩文件处理工具类
- 解压缩文件
- 处理压缩包内的文件
- 临时文件管理
- 编码处理（支持中文文件名）

#### 5. Hash.java
哈希计算工具类
- 计算文件MD5值
- 提供哈希相关的通用方法
- 处理大文件的分块哈希

#### 6. SimilarityChecker.java
文件相似度检查工具类
- 实现SimHash算法
- 计算文件相似度
- 文本文件内容比较
- 相似文件分组
- 管理基准文件和相似文件的关系

#### 7. SimilarityResult.java
相似度结果数据结构类
- 存储相似度比较结果
- 提供结果访问接口
- 管理相似文件组
- 保存相似度信息
- 支持基准文件的标识

### 类之间的关系

1. **Launcher.java** 作为程序入口：
   - 调用 Main 类启动程序
   - 处理运行时环境

2. **Main.java** 作为应用程序核心：
   - 使用 FileUtil 处理文件操作
   - 使用 SimilarityChecker 进行相似度比较
   - 使用 SimilarityResult 展示结果
   - 提供用户界面和交互

3. **FileUtil.java** 作为核心处理类：
   - 调用 Hash 计算文件哈希值
   - 调用 ZipUtil 处理压缩文件
   - 向 Main 提供处理结果

4. **SimilarityChecker.java** 作为相似度处理类：
   - 生成 SimilarityResult 结果
   - 与 FileUtil 配合处理文件
   - 向 Main 提供相似度比较结果
   - 管理文件相似度的计算和分组

### 主要功能流程

1. **程序启动流程**：
   ```
   Launcher -> Main -> GUI初始化
   ```

2. **文件扫描流程**：
   ```
   Main -> FileUtil -> ZipUtil（如果需要处理压缩包）
   ```

3. **MD5对比流程**：
   ```
   Main -> FileUtil -> Hash -> 结果返回
   ```

4. **相似度对比流程**：
   ```
   Main -> SimilarityChecker -> SimilarityResult -> 结果格式化显示
   ```

### 扩展性考虑

- 可以添加新的对比算法
- 可以扩展支持的压缩格式
- 可以优化文件处理性能
- 可以添加更多的文件类型支持
- 可以自定义相似度结果的显示格式
- 可以添加更多的文件分析功能

### 打包和部署

1. **Maven 配置**：
   - 使用 maven-shade-plugin 打包
   - 包含所有必要的依赖
   - 支持 JavaFX 运行时

2. **运行方式**：
   - 支持命令行运行：`java -jar HashUtil-1.0-SNAPSHOT.jar`
   - 支持双击运行（需要正确的系统配置）

3. **依赖管理**：
   - JavaFX 依赖
   - 压缩文件处理依赖
   - 其他工具库依赖