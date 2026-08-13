import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String json = response.body();
        
        // 提取最新版本的ID和URL（用简单方法）
        String latestId = extractValue(json, "\"id\":\"", "\"");
        String latestUrl = extractValue(json, "\"url\":\"", "\"");
        
        System.out.println("最新版本：" + latestId);
        System.out.println("版本JSON地址：" + latestUrl);
    }
    
    // 简单提取字符串
    public static String extractValue(String text, String start, String end) {
        int startIndex = text.indexOf(start) + start.length();
        int endIndex = text.indexOf(end, startIndex);
        return text.substring(startIndex, endIndex);
    }
}