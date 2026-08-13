import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.file.Paths;

public class Downloader {
    
    private HttpClient client;
    
    public Downloader() {
        this.client = HttpClient.newHttpClient();
    }
    
    // 下载版本JSON
    public String downloadVersionJson(String url, String versionId) throws Exception {
        System.out.println("📥 正在下载版本JSON...");
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();
        
        String filename = versionId + ".json";
        java.nio.file.Files.write(
            Paths.get(filename),
            json.getBytes()
        );
        
        System.out.println("✅ 已保存到：" + filename);
        System.out.println("📄 文件大小：" + json.length() + " 字节");
        
        return json;
    }
    
    // 打印依赖库列表
    public void parseAndDisplayLibraries(String versionJson) {
        System.out.println("\n📚 正在解析依赖库列表...");
        
        int libStart = versionJson.indexOf("\"libraries\":[") + 13;
        int libEnd = versionJson.indexOf("]", libStart);
        String libPart = versionJson.substring(libStart, libEnd);
        String[] libEntries = libPart.split("\\},\\{");
        
        System.out.println("📦 共 " + libEntries.length + " 个依赖库");
        System.out.println("\n依赖库列表：");
        System.out.println("------------------");
        
        for (int i = 0; i < libEntries.length; i++) {
            String entry = libEntries[i];
            String name = extractValue(entry, "\"name\":\"", "\"");
            System.out.println((i + 1) + ". " + name);
        }
        
        System.out.println("\n✅ 依赖库列表解析完成！");
    }
    
    // ========== 新增：下载所有依赖库 ==========
    public void downloadLibraries(String versionJson) throws Exception {
        System.out.println("\n📦 开始下载依赖库...");
        
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("libraries"));
        
        int libStart = versionJson.indexOf("\"libraries\":[") + 13;
        int libEnd = versionJson.indexOf("]", libStart);
        String libPart = versionJson.substring(libStart, libEnd);
        String[] libEntries = libPart.split("\\},\\{");
        
        int count = 0;
        for (String entry : libEntries) {
            String name = extractValue(entry, "\"name\":\"", "\"");
            if (name.isEmpty()) continue;
            
            String[] parts = name.split(":");
            if (parts.length < 3) continue;
            
            String groupId = parts[0];
            String artifactId = parts[1];
            String version = parts[2];
            
            String path = groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";
            String url = "https://repo1.maven.org/maven2/" + path;
            
            String localPath = "libraries/" + path;
            java.nio.file.Path localFile = java.nio.file.Paths.get(localPath);
            
            if (java.nio.file.Files.exists(localFile)) {
                System.out.println("⏭️  已存在：" + artifactId + "-" + version + ".jar");
                continue;
            }
            
            try {
                System.out.println("⬇️  下载：" + artifactId + "-" + version + ".jar");
                java.nio.file.Files.createDirectories(localFile.getParent());
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
                HttpResponse<java.nio.file.Path> response = client.send(
                    request, 
                    HttpResponse.BodyHandlers.ofFile(localFile)
                );
                
                if (response.statusCode() == 200) {
                    count++;
                } else {
                    System.out.println("⚠️  下载失败：" + url);
                }
            } catch (Exception e) {
                System.out.println("❌ 下载出错：" + artifactId);
            }
        }
        
        System.out.println("\n✅ 下载完成！共下载 " + count + " 个新文件");
    }
    
    private String extractValue(String text, String start, String end) {
        int startIndex = text.indexOf(start) + start.length();
        int endIndex = text.indexOf(end, startIndex);
        return text.substring(startIndex, endIndex);
    }
}