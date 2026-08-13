import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // 1. 获取版本清单
        System.out.println("📡 正在获取版本清单...");
        HttpRequest listRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
            .build();
        HttpResponse<String> listResponse = client.send(listRequest, HttpResponse.BodyHandlers.ofString());
        String json = listResponse.body();

        // 2. 提取所有版本
        int versionsStart = json.indexOf("\"versions\":[") + 12;
        int versionsEnd = json.indexOf("]", versionsStart);
        String versionsPart = json.substring(versionsStart, versionsEnd);
        String[] versionEntries = versionsPart.split("\\},\\{");

        // 3. 过滤正式版
        ArrayList<String> releaseList = new ArrayList<>();
        for (String entry : versionEntries) {
            if (entry.contains("\"type\":\"release\"")) {
                releaseList.add(entry);
            }
        }
        java.util.Collections.reverse(releaseList);

        System.out.println("\n✨ Aurora Launcher");
        System.out.println("==================");
        System.out.println("📦 正式版共 " + releaseList.size() + " 个\n");
        System.out.println("所有正式版：");
        System.out.println("------------------");

        // 显示所有正式版
        for (int i = 0; i < releaseList.size(); i++) {
            String id = extractValue(releaseList.get(i), "\"id\":\"", "\"");
            System.out.println((i + 1) + ". " + id);
        }

        System.out.println();
        System.out.print("选择版本 (1-" + releaseList.size() + ")：");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        scanner.close();

        if (choice >= 1 && choice <= releaseList.size()) {
            String selectedEntry = releaseList.get(choice - 1);
            String selectedId = extractValue(selectedEntry, "\"id\":\"", "\"");
            String selectedUrl = extractValue(selectedEntry, "\"url\":\"", "\"");
            
            System.out.println("✅ 已选择：" + selectedId);
            System.out.println("📥 正在下载版本JSON...");
            
            // 4. 下载版本JSON
            HttpRequest versionRequest = HttpRequest.newBuilder()
                .uri(URI.create(selectedUrl))
                .build();
            HttpResponse<String> versionResponse = client.send(versionRequest, HttpResponse.BodyHandlers.ofString());
            String versionJson = versionResponse.body();
            
            // 5. 保存到本地
            String filename = selectedId + ".json";
            java.nio.file.Files.write(
                java.nio.file.Paths.get(filename),
                versionJson.getBytes()
            );
            
            System.out.println("✅ 已保存到：" + filename);
            System.out.println("📄 文件大小：" + versionJson.length() + " 字节");
        }
    }

    public static String extractValue(String text, String start, String end) {
        int startIndex = text.indexOf(start) + start.length();
        int endIndex = text.indexOf(end, startIndex);
        return text.substring(startIndex, endIndex);
    }
}