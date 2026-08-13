import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        VersionManager vm = new VersionManager();
        vm.fetchVersions();
        vm.displayVersions();
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("\n选择版本 (1-" + vm.getVersionCount() + ")：");
        int choice = scanner.nextInt();
        scanner.close();
        
        if (choice >= 1 && choice <= vm.getVersionCount()) {
            int index = choice - 1;
            String versionId = vm.getVersionId(index);
            String versionUrl = vm.getVersionUrl(index);
            
            System.out.println("✅ 已选择：" + versionId);
            
            Downloader downloader = new Downloader();
            String versionJson = downloader.downloadVersionJson(versionUrl, versionId);
            
            // 解析并显示依赖库
            downloader.parseAndDisplayLibraries(versionJson);
            
            // 新增：下载所有依赖库
            downloader.downloadLibraries(versionJson);
        }
    }
}