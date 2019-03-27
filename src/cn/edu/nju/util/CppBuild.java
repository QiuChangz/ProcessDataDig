package cn.edu.nju.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CppBuild {
    private static Runtime rt = Runtime.getRuntime();

    public static boolean getBuildStatus(String fileLocation, boolean containsHeader) {
        File dir = new File(fileLocation);
        List<String> cppFiles = new ArrayList<>();
        for (File file: dir.listFiles()){
            if (file.getName().contains(".cpp")){
                cppFiles.add(file.getAbsolutePath());
            }
        }
        StringBuilder command = new StringBuilder("gcc -c ");
        if (containsHeader){
            command.append("-I . ");
        }

        for (String cpp: cppFiles){
            command.append(cpp).append(" ");
        }
        boolean buildStatus = true;
        try {
            Process process = rt.exec(command.toString());
            String line;
            BufferedInputStream error = new BufferedInputStream(process.getErrorStream());
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(error));
            int i = 0;
            while ((line = errorReader.readLine()) != null) {
                if (line.contains("error") || line.contains("fatal"))
                    buildStatus = false;
//                System.out.println(line);
            }
            process.waitFor();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (!buildStatus){
            System.out.println("FILE_BUILD_FAILED: " + fileLocation);
        }
        return buildStatus;
    }

    public static boolean getBuildStatus(String cppFile) {
        String command = "gcc -c " + cppFile;
        boolean buildStatus = true;
        try {
            Process process = rt.exec(command);
            BufferedInputStream bis = new BufferedInputStream(
                    process.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(bis));
            String line;
            BufferedInputStream error = new BufferedInputStream(process.getErrorStream());
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(error));
//            BufferedOutputStream bos = new BufferedOutputStream(process.getOutputStream());
//            bos.write(commandInfo, 0, commandInfo.length);
//            bos.flush();
            int i = 0;
            while ((line = errorReader.readLine()) != null) {
//                if (line.contains("error") || line.contains("fatal"))
//                    buildStatus = false;
                System.out.println(line);
            }
//            int result = process.exitValue();
//            System.out.println(result);
            process.waitFor();
            process.destroy();
            br.close();
            bis.close();
//            bos.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (!buildStatus){
            System.out.println(cppFile);
        }
        return buildStatus;
    }

    public static void main(String[] args) {
        CppBuild.getBuildStatus("F:\\毕设\\OldTopScoreContent\\OldData\\cleanBuild\\exam_exam2\\629\\2017-12-07-08-53-37\\Q103\\源文件\\area.cpp");
    }
}
