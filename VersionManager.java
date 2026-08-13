import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.ArrayList;

public class VersionManager {
    
    private HttpClient client;
    private ArrayList<String> releaseList;
    
    public VersionManager() {
        this.client = HttpClient.newHttpClient();
        this.releaseList = new ArrayList<>();
    }
    
    // 获取版本清单并过滤正式版
    public void fetchVersions() throws Exception {
        System.out.println("📡 正在获取版本清单...");
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();
        
        int versionsStart = json.indexOf("\"versions\":[") + 12;
        int versionsEnd = json.indexOf("]", versionsStart);
        String versionsPart = json.substring(versionsStart, versionsEnd);
        String[] versionEntries = versionsPart.split("\\},\\{");
        
        releaseList.clear();
        for (String entry : versionEntries) {
            if (entry.contains("\"type\":\"release\"")) {
                releaseList.add(entry);
            }
        }
        java.util.Collections.reverse(releaseList);
        
        System.out.println("✅ 获取到 " + releaseList.size() + " 个正式版\n");
    }
    
    // 显示所有正式版
    public void displayVersions() {
        System.out.println("所有正式版：");
        System.out.println("------------------");
        for (int i = 0; i < releaseList.size(); i++) {
            String id = extractValue(releaseList.get(i), "\"id\":\"", "\"");
            System.out.println((i + 1) + ". " + id);
        }
    }
    
    // 获取版本总数
    public int getVersionCount() {
        return releaseList.size();
    }
    
    // 根据序号获取版本ID
    public String getVersionId(int index) {
        return extractValue(releaseList.get(index), "\"id\":\"", "\"");
    }
    
    // 根据序号获取版本JSON下载地址
    public String getVersionUrl(int index) {
        return extractValue(releaseList.get(index), "\"url\":\"", "\"");
    }
    
    // 提取字符串工具方法
    private String extractValue(String text, String start, String end) {
        int startIndex = text.indexOf(start) + start.length();
        int endIndex = text.indexOf(end, startIndex);
        return text.substring(startIndex, endIndex);
    }
}