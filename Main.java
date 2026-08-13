import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // 1. 获取版本列表
        VersionManager vm = new VersionManager();
        vm.fetchVersions();
        vm.displayVersions();
        
        // 2. 用户选择版本
        Scanner scanner = new Scanner(System.in);
        System.out.print("\n选择版本 (1-" + vm.getVersionCount() + ")：");
        int choice = scanner.nextInt();
        scanner.close();
        
        if (choice >= 1 && choice <= vm.getVersionCount()) {
            int index = choice - 1;
            String versionId = vm.getVersionId(index);
            String versionUrl = vm.getVersionUrl(index);
            
            System.out.println("✅ 已选择：" + versionId);
            
            // 3. 下载版本JSON
            Downloader downloader = new Downloader();
            String versionJson = downloader.downloadVersionJson(versionUrl, versionId);
            
            // 4. 解析并显示依赖库
            downloader.parseAndDisplayLibraries(versionJson);
            
            System.out.println("\n🎯 下一步：下载依赖库jar文件");
        }
    }
}