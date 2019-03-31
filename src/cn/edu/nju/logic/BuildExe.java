package cn.edu.nju.logic;

import cn.edu.nju.util.DBUtil;
import cn.edu.nju.util.FileChineseCharsetDetector;
import cn.edu.nju.util.PropertiesUtil;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BuildExe {

    /**
     * * 通过filePath查找相应用户的时间戳目录，将目录下文件调用bat文件输出Main.exe文件并将文件拷贝至dstPath
     * @param filePath 待编译的文件所在根目录
     * @param dstPath 拷贝目标目录
     * @param topUserFilter 高分学生筛选
     */
    public static void buildExe(String filePath, String dstPath, String[] topUserFilter){
        String outputName = PropertiesUtil.getProperties("EXE_NAME");
        File exam = new File(filePath);
        if (exam.isFile()){
            return;
        }
        if (exam.listFiles() == null || exam.listFiles().length == 0){
            return;
        }

        for (File user: exam.listFiles()){
            if (user.isFile()){
                continue;
            }
            boolean isTop = false;
            for (String topUser: topUserFilter){
                if (topUser.equals(user.getName())){
                    isTop = true;
                    break;
                }
            }
            if (!isTop){
                continue;
            }
            for (File date: user.listFiles()){
                if (date.isFile()){
                    continue;
                }

                if (date.listFiles() == null || date.listFiles().length == 0){
                    continue;
                }
                StringBuilder command = new StringBuilder(PropertiesUtil.getProperties("BAT_PATH")).append(" ");
                String path = date.getAbsolutePath() + "\\";
                String dstExePath = dstPath + path.substring(filePath.length());
                command.append(path).append(" ").append(path).append(" ").append(path);
                if (!exec(command.toString())){
                    System.out.println("BUILD_EXE_FILE_FAILED: " + path);
                } else {
                    scp(path, outputName, dstExePath);
                }
            }
        }
    }

    public static void scp(String srcPath, String outputName, String dstPath){
            File srcDir = new File(srcPath);
            File srcExe = new File(srcPath + outputName);
            File desExePath = new File(dstPath);
            try {
                if (!srcExe.exists()){
                    System.out.println("SRC_MAIN_EXE_NOT_FOUND: " + srcExe.getAbsolutePath());
                    return;
                }

                if (!desExePath.exists()){
                    if (!desExePath.mkdirs()){
                        System.out.println("DIR_CREATE_FAILED: " + desExePath);
                        return;
                    }
                }

                for (File srcFile: srcDir.listFiles()){
                    String fileName = srcFile.getName();
                    if (srcFile.isDirectory())
                        continue;
                    if (fileName.endsWith(".h") || fileName.endsWith(".cpp") || fileName.equals("Main.exe")){
                        File desExe = new File(dstPath + "\\" + fileName);
                        if (!desExe.exists()){
                            if (!desExe.createNewFile()){
                                System.out.println("EXE_FILE_CREATE_FAILED: " + desExe.getAbsolutePath());
                                continue;
                            }
                        }
                        FileUtils.copyFile(srcFile, desExe);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
    }
    private static boolean exec(String command){
        boolean isSuccess = true;
        try {
//            System.out.println(command);
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = br.readLine()) != null){
                if (line.contains("error") || line.contains("fatal"))
                    isSuccess = false;
            }
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            isSuccess = false;
            e.printStackTrace();
        }
        return isSuccess;
    }

    public static void main(String[] args) {
        String []topUsers = ProcessDig.getTopUsers(Integer.valueOf(PropertiesUtil.getProperties("SCORE_LIMIT")));
        String path = PropertiesUtil.getProperties("SRC_PATH");
        String dstPath = PropertiesUtil.getProperties("DST_PATH");
        BuildExe.buildExe(path, dstPath, topUsers);
//        String command = "E:\\builder.bat F:\\毕设\\OldTopScoreContent\\exam2_save\\31\\2017-12-07-09-32-58\\ F:\\毕设\\OldTopScoreContent\\exam2_save\\31\\2017-12-07-09-32-58\\ F:\\毕设\\OldTopScoreContent\\exam2_save\\31\\2017-12-07-09-32-58\\";
//        BuildExe.exec(command);
//        String rootDir = "F:\\毕设\\OldTopScoreContent\\exam2_save";
//        String resultPath =  rootDir + "\\result.txt";
//        getBuildFailed(resultPath, rootDir, topUsers);
    }
}
