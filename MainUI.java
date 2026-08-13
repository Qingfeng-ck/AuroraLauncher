import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.concurrent.Task;
import javafx.application.Platform;

public class MainUI extends Application {
    
    private ComboBox<String> versionBox;
    private Label status;
    private Button launchBtn;
    private VersionManager vm;
    private Downloader downloader;
    
    @Override
    public void start(Stage stage) throws Exception {
        // 初始化后端
        vm = new VersionManager();
        downloader = new Downloader();
        
        // 标题
        Label title = new Label("✨ Aurora Launcher");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");
        
        // 版本下拉框
        versionBox = new ComboBox<>();
        versionBox.setStyle("""
            -fx-background-color: rgba(255,255,255,0.15);
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-background-radius: 15px;
            -fx-padding: 8px 15px;
        """);
        
        // 启动按钮
        launchBtn = new Button("🚀 启动游戏");
        launchBtn.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #6dd5fa, #2980b9);
            -fx-text-fill: white;
            -fx-font-size: 18px;
            -fx-padding: 12px 40px;
            -fx-background-radius: 25px;
            -fx-cursor: hand;
        """);
        launchBtn.setOnAction(e -> launchGame());
        launchBtn.setDisable(true);
        
        // 状态栏
        status = new Label("⏳ 正在加载版本列表...");
        status.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 13px;");
        
        // 布局
        VBox root = new VBox(15);
        root.setStyle("""
            -fx-background-color: rgba(255,255,255,0.10);
            -fx-background-radius: 30px;
            -fx-padding: 40px 50px 35px 50px;
            -fx-border-color: rgba(255,255,255,0.25);
            -fx-border-width: 1.5px;
            -fx-border-radius: 30px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 10);
        """);
        root.getChildren().addAll(title, versionBox, launchBtn, status);
        
        Scene scene = new Scene(root, 420, 350);
        scene.setFill(Color.TRANSPARENT);
        
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Aurora Launcher");
        stage.setScene(scene);
        stage.show();
        
        // 后台加载版本列表
        loadVersions();
    }
    
    private void loadVersions() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> status.setText("📡 正在获取版本清单..."));
                vm.fetchVersions();
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            versionBox.getItems().clear();
            for (int i = 0; i < vm.getVersionCount(); i++) {
                versionBox.getItems().add(vm.getVersionId(i));
            }
            versionBox.setValue(vm.getVersionId(0));
            launchBtn.setDisable(false);
            status.setText("✅ 就绪，共 " + vm.getVersionCount() + " 个版本");
        });
        task.setOnFailed(e -> {
            status.setText("❌ 加载失败，请检查网络");
            launchBtn.setDisable(true);
        });
        new Thread(task).start();
    }
    
    private void launchGame() {
        String selected = versionBox.getValue();
        if (selected == null) return;
        
        launchBtn.setDisable(true);
        status.setText("⏳ 正在准备启动 " + selected + "...");
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // 获取版本URL
                    String url = "";
                    for (int i = 0; i < vm.getVersionCount(); i++) {
                        if (vm.getVersionId(i).equals(selected)) {
                            url = vm.getVersionUrl(i);
                            break;
                        }
                    }
                    
                    Platform.runLater(() -> status.setText("📥 下载版本JSON..."));
                    String json = downloader.downloadVersionJson(url, selected);
                    
                    Platform.runLater(() -> status.setText("📦 下载依赖库..."));
                    downloader.downloadLibraries(json);
                    
                    Platform.runLater(() -> status.setText("🎮 下载游戏核心..."));
                    downloader.downloadClient(json, selected);
                    
                    Platform.runLater(() -> status.setText("🚀 启动游戏..."));
                    downloader.launchGame(selected, json);
                    
                    Platform.runLater(() -> status.setText("✅ 游戏已启动！"));
                } catch (Exception e) {
                    Platform.runLater(() -> status.setText("❌ 启动失败：" + e.getMessage()));
                    e.printStackTrace();
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            launchBtn.setDisable(false);
            status.setText("✅ 游戏已退出");
        });
        task.setOnFailed(e -> {
            launchBtn.setDisable(false);
            status.setText("❌ 启动失败");
        });
        new Thread(task).start();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}