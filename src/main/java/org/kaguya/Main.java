package org.kaguya;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Main extends Application {

    private String path;
    private Slider thresholdSlider;
    private CheckBox processNonTextCheckBox;

    @Override
    public void start(Stage primaryStage) {
        // 首先初始化成员变量
        thresholdSlider = new Slider(0.5, 1.0, 0.8);  // 初始化滑动条
        thresholdSlider.setShowTickLabels(true);
        thresholdSlider.setShowTickMarks(true);
        thresholdSlider.setMajorTickUnit(0.1);
        thresholdSlider.setBlockIncrement(0.05);

        processNonTextCheckBox = new CheckBox("处理非文本文件");  // 初始化复选框
        processNonTextCheckBox.setSelected(false);

        // 然后创建布局
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // 创建水平布局
        HBox pathBox = new HBox(10);
        HBox extensionBox = new HBox(10);
        HBox controlBox = new HBox(10);
        pathBox.setPadding(new Insets(10));
        extensionBox.setPadding(new Insets(10));
        controlBox.setPadding(new Insets(10));

        // 文件路径选择部分
        TextField pathField = new TextField();
        pathField.setPrefWidth(400);
        Button selectButton = new Button("选择文件夹");
        
        // 扩展名过滤部分
        TextField extensionField = new TextField();
        extensionField.setPrefWidth(200);
        extensionField.setPromptText("输入扩展名，用逗号分隔");
        
        // 创建算法选择下拉框
        ComboBox<String> algorithmChoice = new ComboBox<>();
        algorithmChoice.getItems().addAll("MD5对比", "SimHash对比");
        algorithmChoice.setValue("MD5对比");
        algorithmChoice.setPrefWidth(120);

        // 相似度设置部分
        HBox thresholdBox = new HBox(10);
        Label thresholdLabel = new Label("相似度阈值：");
        Label thresholdValue = new Label(String.format("%.0f%%", thresholdSlider.getValue() * 100));
        thresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            thresholdValue.setText(String.format("%.0f%%", newVal.doubleValue() * 100)));
        thresholdBox.getChildren().addAll(thresholdLabel, thresholdSlider, thresholdValue);

        // 创建相似度设置容器
        VBox similaritySettingsBox = new VBox(10);
        similaritySettingsBox.setPadding(new Insets(10));
        similaritySettingsBox.getChildren().addAll(thresholdBox, processNonTextCheckBox);
        similaritySettingsBox.setVisible(false);

        // 根据算法选择显示/隐藏相似度设置
        algorithmChoice.valueProperty().addListener((obs, oldVal, newVal) -> 
            similaritySettingsBox.setVisible(newVal.equals("SimHash对比")));

        // 其他按钮
        Button enterButton = new Button("确认");
        CheckBox invertButton = new CheckBox("反选");
        CheckBox zipButton = new CheckBox("处理压缩包");

        // 组装布局
        pathBox.getChildren().addAll(pathField, selectButton);
        extensionBox.getChildren().addAll(extensionField, invertButton, zipButton);
        controlBox.getChildren().addAll(algorithmChoice, enterButton);

        // 添加所有组件到根布局
        root.getChildren().addAll(
            pathBox,
            extensionBox,
            controlBox,
            similaritySettingsBox
        );

        // Path显示输入框
        pathField.setEditable(false);
        pathField.setPrefWidth(300); // 设置文本框宽度
        
        // 创建选择文件按钮
        selectButton.setPrefWidth(200); // 设置按钮宽度
        selectButton.setOnAction(e -> {
            //FileChooser fileChooser = new FileChooser();
            DirectoryChooser directoryChooser = new DirectoryChooser();
            //File selectedFile = fileChooser.showOpenDialog(primaryStage);
            File selectedFile = directoryChooser.showDialog(primaryStage);
            if (selectedFile != null) {
                pathField.setText(selectedFile.getAbsolutePath());
                path = selectedFile.getAbsolutePath();
            }
        });

        // 添加反选按钮
        invertButton.setPrefWidth(100);
        
        // 用于跟踪当前是否为反选状态
        final boolean[] isInverted = {false};
        
        invertButton.setOnAction(e -> {
            isInverted[0] = !isInverted[0];
            invertButton.setText(isInverted[0] ? "反选中" : "反选");
        });

        // 添加压缩包处理按钮
        zipButton.setPrefWidth(100);
        
        // 用于跟踪是否处理压缩包
        final boolean[] processZip = {false};
        
        zipButton.setOnAction(e -> {
            processZip[0] = !processZip[0];
            zipButton.setText(processZip[0] ? "处理压缩包中" : "处理压缩包");
        });

        enterButton.setOnAction(e -> {
            if (path == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("警告");
                alert.setHeaderText("未选择文件夹！");
                alert.showAndWait();
                return;
            }
            try {
                String[] extensions = extensionField.getText().trim().isEmpty() ?
                        new String[0] : extensionField.getText().split(",");
                FileUtil fileUtil = new FileUtil();
                List<File> files = fileUtil.readFilesFromFolder(path, extensions, isInverted[0], processZip[0]);

                // 根据选择的算法执行不同的比较
                if (algorithmChoice.getValue().equals("MD5对比")) {
                    Map<String, List<String>> result = fileUtil.checkFolder(files);
                    showMD5Result(result);
                } else {
                    SimilarityChecker similarityChecker = new SimilarityChecker();
                    similarityChecker.setSimilarityThreshold(thresholdSlider.getValue());
                    similarityChecker.setProcessNonText(processNonTextCheckBox.isSelected());
                    Map<String, SimilarityResult.SimilarityGroup> result =
                        similarityChecker.findSimilarFiles(files);
                    showSimilarityResult(result);
                }
            } catch (Exception error) {
                error.printStackTrace();
                throw new RuntimeException("检查文件夹时发生错误", error);
            }
        });
        
        Scene scene = new Scene(root, 600, 300);
        primaryStage.setTitle("文件对比工具");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // 显示结果的方法
    private void showResult(Map<String, List<String>> result) {
        if (result.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("结果");
            alert.setHeaderText("未找到重复/相似文件");
            alert.showAndWait();
            return;
        }

        Stage resultStage = new Stage();
        VBox resultRoot = new VBox(10);
        resultRoot.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane();
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            TextArea groupText = new TextArea();
            groupText.setEditable(false);
            groupText.setPrefRowCount(entry.getValue().size() + 1);
            
            StringBuilder content = new StringBuilder();
            content.append("----------------------\n");
            content.append(entry.getKey()).append("\n");
            for (String fileName : entry.getValue()) {
                content.append(fileName).append("\n");
            }
            groupText.setText(content.toString());
            
            contentBox.getChildren().add(groupText);
        }

        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(400);

        resultRoot.getChildren().add(scrollPane);

        Scene resultScene = new Scene(resultRoot, 500, 500);
        resultStage.setTitle("对比结果");
        resultStage.setScene(resultScene);
        resultStage.show();
    }

    private void showMD5Result(Map<String, List<String>> result) {
        showResultWindow("MD5对比结果", result, (key, files) -> {
            StringBuilder content = new StringBuilder();
            content.append("----------------------\n");
            content.append(key).append("\n");
            for (String fileName : files) {
                content.append(fileName).append("\n");
            }
            return content.toString();
        });
    }

    private void showSimilarityResult(Map<String, SimilarityResult.SimilarityGroup> result) {
        showResultWindow("相似度对比结果", result, (key, group) -> {
            StringBuilder content = new StringBuilder();
            content.append("----------------------\n");
            
            // 找出基准文件（相似度为1.0的文件）
            SimilarityResult.FileWithSimilarity baseFile = group.getFiles().stream()
                .filter(f -> Math.abs(f.getSimilarity() - 1.0) < 0.0001) // 使用近似相等来处理浮点数
                .findFirst()
                .orElse(group.getFiles().get(0));

            // 先输出基准文件
            content.append("基准文件: ").append(baseFile.getFileName()).append("\n");
            content.append("相似文件列表：\n");
            
            // 输出其他文件及其与基准文件的相似度，按相似度降序排序
            group.getFiles().stream()
                .filter(f -> !f.getFileName().equals(baseFile.getFileName()))
                .sorted((f1, f2) -> Double.compare(f2.getSimilarity(), f1.getSimilarity()))
                .forEach(file -> {
                    content.append(String.format("- %s (与基准文件相似度: %.2f%%)\n", 
                        file.getFileName(), 
                        file.getSimilarity() * 100));
                });
            
            return content.toString();
        });
    }

    private <T> void showResultWindow(String title, Map<String, T> result, 
            BiFunction<String, T, String> contentFormatter) {
        if (result.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("结果");
            alert.setHeaderText("未找到重复/相似文件");
            alert.showAndWait();
            return;
        }

        Stage resultStage = new Stage();
        VBox resultRoot = new VBox(10);
        resultRoot.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane();
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        for (Map.Entry<String, T> entry : result.entrySet()) {
            TextArea groupText = new TextArea();
            groupText.setEditable(false);
            groupText.setPrefRowCount(8);
            groupText.setWrapText(true);
            
            String content = contentFormatter.apply(entry.getKey(), entry.getValue());
            groupText.setText(content);
            
            contentBox.getChildren().add(groupText);
        }

        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(600);

        resultRoot.getChildren().add(scrollPane);

        Scene resultScene = new Scene(resultRoot, 600, 800);
        resultStage.setTitle(title);
        resultStage.setScene(resultScene);
        resultStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
