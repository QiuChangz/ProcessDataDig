package cn.edu.nju.logic;

import cn.edu.nju.util.DBUtil;
import cn.edu.nju.util.FileUtil;
import cn.edu.nju.util.PropertiesUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

import static cn.edu.nju.logic.ProcessDig.getTopUsers;

public class extractBuildTestFile {

    public void extracBuildFile(String[] topUserFilter) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        String inputFilePath = PropertiesUtil.getProperties("EXTRACT_BUILD_INPUT");
        String outputFilePath = PropertiesUtil.getProperties("EXTRACT_BUILD_OUT");
        Date exam_start_time = null;
        try {
            for (String id : topUserFilter) {
                //把找到对应用户的编译
                File user_root = new File(inputFilePath + "\\" + id);

                if (!user_root.exists() || user_root.isFile()) {
                    System.out.println("user root error : " + id);
                    continue;
                }

                File[] buildFiles = user_root.listFiles();

                int buildCount = 0;
                Date user_start_time = null;
                boolean tooLong = false;
                for (File buildFile : buildFiles) {
                    if (buildFile.isFile()) {
                        System.out.println("build file error : " + id + " :" + buildFile.getName());
                        continue;
                    }

                    Date buildDate = df.parse(buildFile.getName());

                    if(exam_start_time == null) {
                        String[] timeArray = buildFile.getName().split("-");
                        exam_start_time = df.parse(timeArray[0]+"-" + timeArray[1]+"-"+timeArray[2] + "-07-50-00");
                    }

                    if(buildDate.before(exam_start_time)){
                        continue;
                    }

                    if(user_start_time == null){
                        user_start_time = buildDate;
                    }
                    if(!tooLong){
                        long gap = (buildDate.getTime() - user_start_time.getTime())/1000;

                        tooLong = gap >= 7200;
                    }

                    File buildTargetDir = new File(outputFilePath+"\\" + id + "\\" + buildFile.getName());
                    if(!buildTargetDir.getParentFile().exists()){
                         buildTargetDir.getParentFile().mkdirs();
                    }

                    FileUtils.copyDirectory(buildFile,buildTargetDir);
                    buildCount+= 1;
                }
                if(tooLong){
                    System.out.println("warning time too long : "+ id);
                }

                if (buildCount == 0) {
                    System.out.println("not build file : " + id);
                    continue;
                }else{
                    System.out.println("user : "+id  +" : count : " + buildCount);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void purify_build_file(){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String exam = PropertiesUtil.getProperties("EXAM");

        String[] qidArray = {"Q102", "Q103", "Q139", "Q141"};
        String qid = qidArray[Integer.parseInt(exam.substring(4)) - 1];

        String outputFilePath = PropertiesUtil.getProperties("EXTRACT_BUILD_OUT");
        Date early_warning_time = null;
        Date gap_time = null;
        Date late_warning_time = null;
        try {
            File rootFile =  new File(outputFilePath);
            if(!rootFile.exists() || rootFile.isFile()){
                System.out.println("rootFile error");
                return;
            }

            for(File userRootFile : rootFile.listFiles() ){
                if(userRootFile.isFile()){
                    System.out.println("user root error :" + userRootFile.getName());
                    continue;
                }

                boolean needCheck = true;
                for(File dateFile : userRootFile.listFiles()){
                    if(dateFile.isFile()){
                        System.out.println("date root error : "+userRootFile.getName()+" : " + dateFile.getName());
                        continue;
                    }
                    if(needCheck){
                        //找到那些很早开始的人
                        Date buildDate = df.parse(dateFile.getName());
                        String[] timeArray = dateFile.getName().split("-");
                        if (early_warning_time == null){
                            early_warning_time = df.parse(timeArray[0]+"-" + timeArray[1]+"-"+timeArray[2] + "-08-00-00");
                            gap_time = df.parse(timeArray[0]+"-" + timeArray[1]+"-"+timeArray[2] + "-09-50-00");
                            late_warning_time = df.parse(timeArray[0]+"-" + timeArray[1]+"-"+timeArray[2] + "-10-10-00");
                        }

                        boolean needWaring = false;
                        if(buildDate.before(gap_time)){
                            needCheck = buildDate.before(early_warning_time);
                        }else{
                            needCheck = buildDate.before(late_warning_time);
                        }
                        if(needWaring){
                            System.out.println("user start early : "+userRootFile.getName()+" :" + userRootFile.getName());
                        }
                        needCheck = false;
                    }
                    //在项目中找到那些不是指定名称的
                    for(File projectRootFile : dateFile.listFiles()){
                        if(projectRootFile.isFile()){
                            System.out.println("project root error : "+userRootFile.getName()+" : " + dateFile.getName() +" : " + projectRootFile.getName());
                            continue;
                        }
                        if(!projectRootFile.getName().equals(qid)){
                            System.out.println("project root not match : "+userRootFile.getName()+" : " + dateFile.getName() +" : " + projectRootFile.getName());
                        }
                    }

                }

            }


        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        extractBuildTestFile pd = new extractBuildTestFile();
//        String exam = PropertiesUtil.getProperties("EXAM");
//        String[] topUsers = getTopUsers(Integer.valueOf(PropertiesUtil.getProperties("SCORE_LIMIT")));
//        pd.extracBuildFile(topUsers);

        pd.purify_build_file();

    }
}
