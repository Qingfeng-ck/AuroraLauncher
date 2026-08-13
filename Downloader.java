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
        System.out.println("Downloading version json...");
        
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
        
        System.out.println("Saved to: " + filename);
        return json;
    }
    
    public void parseAndDisplayLibraries(String versionJson) {
        System.out.println("Parsing libraries...");
        
        int libStart = versionJson.indexOf("\"libraries\":[") + 13;
        int libEnd = versionJson.indexOf("]", libStart);
        String libPart = versionJson.substring(libStart, libEnd);
        String[] libEntries = libPart.split("\\},\\{");
        
        System.out.println("Total libraries: " + libEntries.length);
        for (int i = 0; i < libEntries.length; i++) {
            String entry = libEntries[i];
            String name = extractValue(entry, "\"name\":\"", "\"");
            System.out.println((i + 1) + ". " + name);
        }
    }
    
    public void downloadLibraries(String versionJson) throws Exception {
        System.out.println("Downloading libraries...");
        
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
                System.out.println("Already exists: " + artifactId);
                continue;
            }
            
            try {
                System.out.println("Downloading: " + artifactId);
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
                    System.out.println("Failed: " + url);
                }
            } catch (Exception e) {
                System.out.println("Error downloading: " + artifactId);
            }
        }
        
        System.out.println("Downloaded " + count + " new files");
    }
    
    public void downloadClient(String versionJson, String versionId) throws Exception {
        System.out.println("Downloading client jar...");
        
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
            System.out.println("Client jar url not found");
            return;
        }
        
        String localPath = "versions/" + versionId + "/" + versionId + ".jar";
        java.nio.file.Path localFile = java.nio.file.Paths.get(localPath);
        
        if (java.nio.file.Files.exists(localFile)) {
            System.out.println("Already exists: " + versionId + ".jar");
            return;
        }
        
        try {
            System.out.println("Downloading: " + versionId + ".jar");
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
                System.out.println("Downloaded, size: " + size + " bytes");
            } else {
                System.out.println("Failed, status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    public void launchGame(String versionId, String versionJson) throws Exception {
        System.out.println("Launching Minecraft " + versionId + "...");
        
        String mainClass = extractValue(versionJson, "\"mainClass\":\"", "\"");
        if (mainClass.isEmpty()) {
            mainClass = "net.minecraft.client.main.Main";
        }
        System.out.println("Main class: " + mainClass);
        
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
        
        System.out.println("Command: " + String.join(" ", command));
        System.out.println("Starting game...");
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        pb.directory(java.nio.file.Paths.get(".").toFile());
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        System.out.println("Game exited with code: " + exitCode);
    }
    
    private String extractValue(String text, String start, String end) {
        int startIndex = text.indexOf(start) + start.length();
        int endIndex = text.indexOf(end, startIndex);
        return text.substring(startIndex, endIndex);
    }
}