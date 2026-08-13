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
    
    public String downloadVersionJson(String url, String versionId) throws Exception {
        System.out.println("下载版本JSON中...");
        
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
        
        System.out.println("已保存：" + filename);
        return json;
    }
    
    public void downloadLibraries(String versionJson) throws Exception {
        System.out.println("下载依赖库中...");
        
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
                System.out.println("已存在：" + artifactId);
                continue;
            }
            
            try {
                System.out.println("下载中：" + artifactId);
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
                    System.out.println("下载失败：" + url);
                }
            } catch (Exception e) {
                System.out.println("下载出错：" + artifactId);
            }
        }
        
        System.out.println("下载完成，共 " + count + " 个新文件");
    }
    
    public void downloadClient(String versionJson, String versionId) throws Exception {
        System.out.println("下载游戏核心中...");
        
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("versions/" + versionId));
        
        String clientUrl = extractValue(versionJson, "\"client\":{\"sha1\":\"[^\"]*\",\"size\":[0-9]*,\"url\":\"", "\"");
        if (clientUrl.isEmpty()) {
            int clientStart = versionJson.indexOf("\"client\"");
            if (clientStart != -1) {
                int urlStart = versionJson.indexOf("\"url\":\"", clientStart) + 7;
                int urlEnd = versionJson.indexOf("\"", urlStart);
                clientUrl = versionJson.substring(urlStart, urlEnd);
            }
        }
        
        if (clientUrl.isEmpty()) {
            System.out.println("未找到client.jar下载地址");
            return;
        }
        
        String localPath = "versions/" + versionId + "/" + versionId + ".jar";
        java.nio.file.Path localFile = java.nio.file.Paths.get(localPath);
        
        if (java.nio.file.Files.exists(localFile)) {
            System.out.println("已存在：" + versionId + ".jar");
            return;
        }
        
        try {
            System.out.println("下载中：" + versionId + ".jar");
            java.nio.file.Files.createDirectories(localFile.getParent());
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(clientUrl))
                .build();
            HttpResponse<java.nio.file.Path> response = client.send(
                request, 
                HttpResponse.BodyHandlers.ofFile(localFile)
            );
            
            if (response.statusCode() == 200) {
                long size = java.nio.file.Files.size(localFile);
                System.out.println("下载完成，大小：" + size + " 字节");
            } else {
                System.out.println("下载失败，状态码：" + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("下载出错：" + e.getMessage());
        }
    }
    
    public void launchGame(String versionId, String versionJson) throws Exception {
        System.out.println("启动 Minecraft " + versionId + "...");
        
        String mainClass = extractValue(versionJson, "\"mainClass\":\"", "\"");
        if (mainClass.isEmpty()) {
            mainClass = "net.minecraft.client.main.Main";
        }
        System.out.println("主类：" + mainClass);
        
        StringBuilder classpath = new StringBuilder();
        classpath.append("versions/").append(versionId).append("/").append(versionId).append(".jar");
        
        java.nio.file.Path libDir = java.nio.file.Paths.get("libraries");
        if (java.nio.file.Files.exists(libDir)) {
            java.nio.file.Files.walk(libDir)
                .filter(p -> p.toString().endsWith(".jar"))
                .forEach(p -> {
                    classpath.append(java.io.File.pathSeparator).append(p.toString());
                });
        }
        
        java.util.ArrayList<String> command = new java.util.ArrayList<>();
        command.add("java");
        command.add("-cp");
        command.add(classpath.toString());
        command.add(mainClass);
        command.add("--gameDir");
        command.add(".");
        command.add("--version");
        command.add(versionId);
        command.add("--assetIndex");
        command.add(extractValue(versionJson, "\"assetIndex\":{\"id\":\"", "\""));
        command.add("--accessToken");
        command.add("AuroraLauncher");
        
        System.out.println("启动命令：" + String.join(" ", command));
        System.out.println("正在启动游戏...");
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        pb.directory(java.nio.file.Paths.get(".").toFile());
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        System.out.println("游戏已退出，退出码：" + exitCode);
    }
    
    private String extractValue(String text, String start, String end) {
        int startIndex = text.indexOf(start) + start.length();
        int endIndex = text.indexOf(end, startIndex);
        return text.substring(startIndex, endIndex);
    }
}