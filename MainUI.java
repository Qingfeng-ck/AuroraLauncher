import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Insets;

public class MainUI extends Application {
    @Override
    public void start(Stage stage) {
        // 标题
        Label title = new Label("✨ Aurora Launcher");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");
        
        // 版本下拉框
        ComboBox<String> versionBox = new ComboBox<>();
        versionBox.getItems().addAll("1.20.4", "1.19.4", "1.18.2", "1.17.1");
        versionBox.setValue("1.20.4");
        versionBox.setStyle("""
            -fx-background-color: rgba(255,255,255,0.15);
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-background-radius: 15px;
            -fx-padding: 8px 15px;
        """);
        
        // 启动按钮
        Button launchBtn = new Button("🚀 启动游戏");
        launchBtn.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #6dd5fa, #2980b9);
            -fx-text-fill: white;
            -fx-font-size: 18px;
            -fx-padding: 12px 40px;
            -fx-background-radius: 25px;
            -fx-cursor: hand;
        """);
        launchBtn.setOnAction(e -> {
            String selected = versionBox.getValue();
            System.out.println("🎮 启动 " + selected);
        });
        
        // 状态栏
        Label status = new Label("✅ 就绪");
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
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}