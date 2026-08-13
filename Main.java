import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
        // 1. 获取版本清单
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();

        // 2. 提取所有版本
        int versionsStart = json.indexOf("\"versions\":[") + 12;
        int versionsEnd = json.indexOf("]", versionsStart);
        String versionsPart = json.substring(versionsStart, versionsEnd);
        String[] versionEntries = versionsPart.split("\\},\\{");
        
        // 3. 过滤出正式版（type为"release"）
        ArrayList<String> releaseList = new ArrayList<>();
        for (String entry : versionEntries) {
            if (entry.contains("\"type\":\"release\"")) {
                releaseList.add(entry);
            }
        }
        
        // 正式版默认按时间从旧到新排列，翻转一下让最新版在最前面
        java.util.Collections.reverse(releaseList);
        
        System.out.println("✨ Aurora Launcher");
        System.out.println("==================");
        System.out.println("📦 正式版共 " + releaseList.size() + " 个");
        System.out.println();
        System.out.println("所有正式版：");
        System.out.println("------------------");
        
        // 显示所有正式版
        for (int i = 0; i < releaseList.size(); i++) {
            String entry = releaseList.get(i);
            String id = extractValue(entry, "\"id\":\"", "\"");
            System.out.println((i + 1) + ". " + id);
        }
        
        System.out.println();
        System.out.print("选择版本 (1-" + releaseList.size() + ")：");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        scanner.close();
        
        if (choice >= 1 && choice <= releaseList.size()) {
            String selected = extractValue(releaseList.get(choice - 1), "\"id\":\"", "\"");
            System.out.println("✅ 已选择：" + selected);
        }
    }
    
    public static String extractValue(String text, String start, String end) {
        int startIndex = text.indexOf(start) + start.length();
        int endIndex = text.indexOf(end, startIndex);
        return text.substring(startIndex, endIndex);
    }
}