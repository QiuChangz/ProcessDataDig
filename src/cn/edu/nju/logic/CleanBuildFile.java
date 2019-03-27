package cn.edu.nju.logic;

import cn.edu.nju.util.FileUtil;
import cn.edu.nju.util.PropertiesUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static cn.edu.nju.logic.ProcessDig.getBuildFiles;
import static cn.edu.nju.logic.ProcessDig.writeToFile;

public class CleanBuildFile {

    //当用户没有保存事件的时候 以用户的build文件版本为主
    public static void cleanBuildFile(){

        String clean_root_path = PropertiesUtil.getProperties("BUILD_FILES_TO_CLEAN");
        String exam = PropertiesUtil.getProperties("EXAM");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        File rootFile = new File(clean_root_path);

        if(rootFile.isFile()){
            System.out.println("根目录错误：" + clean_root_path);
            return;
        }


        try {
            Date startTime = df.parse("2017-12-21-07-50-00");
            Date endTime = df.parse("2017-12-21-12-30-00");
            for(File user_root : rootFile.listFiles()){
                if (user_root.isFile()) {
                    System.out.println("用户根目录错误：" + user_root.getName());
                    continue;
                }
                String user_id = user_root.getName();
                List<String> buildTime = FileUtil.getDirTimes(clean_root_path + "\\" + user_id);

                for(String buildDateStr : buildTime){
                    Date buildDate = df.parse(buildDateStr.substring(buildDateStr.lastIndexOf("\\") + 1));

                    if (buildDate.before(startTime) || buildDate.after(endTime)){
                        FileUtils.deleteDirectory(new File(buildDateStr));
                        continue;
                    }
                    Map<String,String> headers = new HashMap<>();
                    Map<String,String> cpps = new HashMap<>();

//                    System.err.print(buildFileRootPath);
                    headers.putAll(getBuildFiles( buildDateStr, exam, true));
                    cpps.putAll(getBuildFiles(buildDateStr, exam, false));

                    File targetFile = new File(buildDateStr);
                    FileUtils.deleteDirectory(targetFile);

                    if (!targetFile.exists()){
                        if(!targetFile.mkdirs()){
                            System.out.println("FILE_DIR_CREATED_FAILED: " + targetFile);
                        }
                    }
                    writeToFile(headers, buildDateStr, true);
                    writeToFile(cpps, buildDateStr , false);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args){
        cleanBuildFile();
    }
}
