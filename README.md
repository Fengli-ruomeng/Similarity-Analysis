# Similarity analysis

一个基于JavaFX的文件对比工具，支持MD5对比和文本相似度对比。

## 功能特点

- 支持文件夹递归扫描
- 支持两种对比模式：
  - MD5精确对比：查找完全相同的文件
  - 相似度对比：查找内容相似的文本文件
- 支持文件扩展名过滤
- 支持压缩包处理
- 可调节的相似度阈值（50%-100%）
- 支持处理非文本文件选项

## 系统要求

- Java 17 或更高版本
- Maven 3.6 或更高版本

## 构建和运行

1. 克隆项目：
git clone ???

2. 进入项目目录：
cd HashUtil

3. 使用Maven构建：
mvn clean package

4. 运行程序：
mvn javafx:run


## 使用说明

1. 选择文件夹：点击"选择文件夹"按钮选择要扫描的目录
2. 设置扩展名过滤（可选）：
   - 在文本框中输入要过滤的文件扩展名，用逗号分隔
   - 勾选"反选"可以排除指定扩展名的文件
3. 选择对比模式：
   - MD5对比：查找完全相同的文件
   - 相似度对比：查找内容相似的文件
4. 相似度设置（仅在相似度对比模式下可用）：
   - 使用滑动条调节相似度阈值（50%-100%）
   - 可选择是否处理非文本文件
5. 其他选项：
   - 勾选"处理压缩包"可以对压缩包内的文件进行对比
6. 点击"确认"开始对比
7. 查看结果：
   - 对比结果会在新窗口中显示
   - MD5对比模式显示完全相同的文件组
   - 相似度对比模式显示相似文件组及其相似度百分比

## 注意事项

- 处理大量文件或大文件时可能需要较长时间
- 相似度对比目前使用SimHash,主要适用于文本文件
- 处理压缩包可能会占用较多临时磁盘空间

## 技术栈

- Java 17
- JavaFX
- Maven
- Apache Commons Compress（用于处理压缩包）
