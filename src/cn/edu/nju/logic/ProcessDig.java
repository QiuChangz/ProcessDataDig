package cn.edu.nju.logic;

import cn.edu.nju.util.CppBuild;
import cn.edu.nju.util.DBUtil;
import cn.edu.nju.util.FileUtil;
import cn.edu.nju.util.PropertiesUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProcessDig {

    public void setAllSaveCode(String exam, String fileLocation, String[] topUserFilter) {
        Connection connection = DBUtil.getMySqlDBConnection(PropertiesUtil.getProperties("MYSQL"));
        Statement statement = null;
        ResultSet resultSet = null;
        List<Integer> users = new ArrayList<>();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        int total = 0, num = 0;
        try {
            statement = connection.createStatement();
            String getUserSql = "select distinct(user_id) from command_text";
            String getContentSql = "select content, time, name from command_text where action = 'Save'";
            resultSet = statement.executeQuery(getUserSql);
            while (resultSet != null && resultSet.next()){
                for (String topUser: topUserFilter){
                    if (topUser.equals(String.valueOf(resultSet.getInt(1)))){
                        users.add(resultSet.getInt(1));
                    }
                }
            }

            for (int id: users){
                Date pre_date = null;

                //保存用户编译时间
                List<String> buildTime = FileUtil.getDirTimes(PropertiesUtil.getProperties("BUILD_FILE_LOCATION") + "\\" + id);
                Map<String, String> headers = new HashMap<>();
                Map<String, String> cpps = new HashMap<>();
                String getUserCodeSql = getContentSql + " and user_id = " + id;
                resultSet = statement.executeQuery(getUserCodeSql);
                while (resultSet != null && resultSet.next()){
                    String code = resultSet.getString("content");
                    String time = resultSet.getString("time");
                    String name = resultSet.getString("name");

                    time = time.replace(":", "-");
                    time = time.replace(" ", "-");
                    Date date = df.parse(time);

                    total++;
                    //找到最近的编译记录
                    String currentLocation = null;
                    Date closestBuildDate = null;
                    for (int i = 0; i < buildTime.size(); i++){
                        String buildLocation = buildTime.get(i);
                        Date build = df.parse(buildLocation.substring(buildLocation.lastIndexOf("\\") + 1));

                        if(build.getTime() - date.getTime() <= 1000){
                            currentLocation = buildLocation;
                            closestBuildDate = build;
                        }else{
                            break;
                        }
                    }

                    //如果是编译刷入的，那么以编译记录为住
                    boolean needCopyPre = false;
                    boolean isPreFromBuild = false;
                    boolean isContentFromBuild = false;

                    if(closestBuildDate == null && pre_date == null){
                        //无事发生
                        needCopyPre = false;
                        isPreFromBuild = false;
                        isContentFromBuild = false;
                    }else if (closestBuildDate == null && pre_date != null){
                        needCopyPre = true;
                        isPreFromBuild = false;
                        isContentFromBuild = false;
                    }else if(closestBuildDate != null && pre_date == null){
                        needCopyPre =true;
                        isPreFromBuild = true;
                        isContentFromBuild = closestBuildDate.getTime() >= date.getTime();
                    }else{
                        if(closestBuildDate.getTime() > pre_date.getTime()){
                            needCopyPre =true;
                            isPreFromBuild = true;
                            isContentFromBuild = closestBuildDate.getTime() >= date.getTime();
                        }else{
                            needCopyPre = true;
                            isPreFromBuild = false;
                            isContentFromBuild = false;
                        }
                    }

                    if(needCopyPre){
                        if(isPreFromBuild){
                            headers.clear();
                            cpps.clear();
                            headers.putAll(getBuildFiles(currentLocation, exam, true));
                            cpps.putAll(getBuildFiles(currentLocation, exam, false));
                        }
                    }else{
                        headers.clear();
                        cpps.clear();
                    }

                    //判断是否需要使用当前的文件
                    if(!isContentFromBuild){
                        if (name.contains(".h")){
                            headers.put(name,code);
                        } else if (name.contains(".cpp")){
                            cpps.put(name, code);
                        }
                    }

                    String fileDir = fileLocation + "\\" + id + "\\" + time;
                    File file = new File(fileDir);
                    if (!file.exists()){
                        if(!file.mkdirs()){
                            System.out.println("FILE_DIR_CREATED_FAILED: " + fileDir);
                        }
                    }
                    writeToFile(headers, fileDir, true);
                    if (!writeToFile(cpps, fileDir)){
                        num++;
                        FileUtil.removeFile(fileDir);
                    }

                    //记录当前保存时间
                    pre_date = date;
                }
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        System.out.println("total file: " + total + ", num of compile failed: " + num);
    }

    //收集旧数据中编译文件夹中的头文件
    private Map<String, String> getBuildFiles(String fileLocation, String exam, boolean isHeader){
        String suffix = isHeader ? ".h" : ".cpp";
        String[] questions = {"Q102", "Q103", "Q139", "Q141"};
        String question = questions[Integer.valueOf(exam.substring(4)) - 1];
        File buildFile = new File(fileLocation);
        Map<String, String> buildFiles = new HashMap<>();
        for (File qFile: buildFile.listFiles()){
            if (!qFile.isDirectory() || !qFile.getName().equals(question)){
                continue;
            }

            for (File dir: qFile.listFiles()){
                if (dir.isFile()){
                    if (dir.getName().contains(suffix)){
                        buildFiles.put(dir.getName(), FileUtil.readFile(dir));
                    }
                    continue;
                }
                for (File file: dir.listFiles()){
                    if (!file.getName().contains(suffix))
                        continue;
                    buildFiles.put(file.getName(), FileUtil.readFile(file));
                }
            }
        }
        return buildFiles;
    }
    private boolean writeToFile(Map<String, String> fileInfo, String fileDir, boolean isHeader){
        boolean result = true;
        if (!fileInfo.isEmpty()){
            for (Map.Entry<String, String> entry: fileInfo.entrySet())   {
                String fileLocation = fileDir  + "\\" + entry.getKey();
                FileUtil.writeFile(entry.getValue(), fileLocation);
//                这里不做检查，只提文件版本
//                if (!isHeader && !CppBuild.getBuildStatus(fileDir, true)){
//                    FileUtil.removeFile(fileLocation);
//                    result = false;
//                }
            }
        }
        return result;
    }

    private boolean writeToFile(Map<String, String> fileInfo, String fileDir) {
        return writeToFile(fileInfo, fileDir, false);
    }


    public static String[] getTopUsers(String exam, int limit){
        Connection connection = DBUtil.getMySqlDBConnection(PropertiesUtil.getProperties("MYSQL"));
        Statement statement = null;
        ResultSet resultSet = null;
        List<String> users = new ArrayList<>();

        try {
            String sql = "select distinct (user_id) from test_result  GROUP BY user_id having max(score) >= " + limit;
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()){
                users.add(String.valueOf(resultSet.getInt("user_id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users.toArray(new String[0]);
    }

    public static void main(String[] args) {
        ProcessDig pd = new ProcessDig();
        String exam = PropertiesUtil.getProperties("EXAM");
//        String exams[] = {"exam1", "exam2", "exam3", "exam4"};
//        for (String exam: exams){
            String fileLocation = PropertiesUtil.getProperties("OUTPUT");
            String[] topUsers = getTopUsers(PropertiesUtil.getProperties("EXAM"), Integer.valueOf(PropertiesUtil.getProperties("SCORE_LIMIT")));
            pd.setAllSaveCode(exam, fileLocation, topUsers);
//        }
    }
}
