import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("✨ Aurora Launcher");
        System.out.println("==================");

        // 版本列表
        String[] versions = {"1.20.4", "1.19.4", "1.18.2"};
        for (int i = 0; i < versions.length; i++) {
            System.out.println((i + 1) + ". " + versions[i]);
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("请选择版本 (1-3)：");
        int choice = scanner.nextInt();

        if (choice >= 1 && choice <= versions.length) {
            String selected = versions[choice - 1];
            System.out.println("✅ 已选择：Minecraft " + selected);
            System.out.println("⬇️ 正在下载...");

            // 30步完整进度条
            int total = 30;
            for (int i = 0; i <= total; i++) {
                int percent = i * 100 / total;
                // 生成进度条：30个字符宽
                String bar = "[" + "#".repeat(i) + ".".repeat(total - i) + "]";
                System.out.print("\r" + bar + " " + percent + "%");
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("\n✅ 下载完成！");
            System.out.println("🚀 启动 Minecraft " + selected + "...");
        } else {
            System.out.println("❌ 无效选择");
        }

        scanner.close();
    }
}