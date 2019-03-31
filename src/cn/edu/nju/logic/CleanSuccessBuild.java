package cn.edu.nju.logic;

import cn.edu.nju.util.DBUtil;
import cn.edu.nju.util.PropertiesUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static cn.edu.nju.logic.BuildExe.scp;

public class CleanSuccessBuild {

    public static void cleanSaveAfterFullMark(){
        Connection connection = DBUtil.getMySqlDBConnection(PropertiesUtil.getProperties("MYSQL"));
        Statement statement = null;
        ResultSet resultSet = null;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        DateFormat dbDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String rootPath = PropertiesUtil.getProperties("DST_PATH");
        String target_path = PropertiesUtil.getProperties("Target_PATH");
        try {
            File rootFile = new File(rootPath);
            if(rootFile.isFile()){
                System.out.println("根目录异常 : " +rootPath);
                return;
            }

            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT user_id,min(time) as end from test_result where score = 100 group by user_id");

            while (resultSet.next()){
                String user_id = resultSet.getString("user_id");
                String time = resultSet.getString("end");
                Date firstFullTime = dbDF.parse(time);

                File userRootFile = new File(rootPath + "\\" + user_id);

                if( !userRootFile.exists()||userRootFile.isFile()){
                    System.out.println("用户根目录异常 : " +userRootFile.getName());
                    return;
                }

                File[] dateDirs = userRootFile.listFiles();

                if(dateDirs.length >= 60){
                    System.out.println("保存次数过多 ："+user_id);
                }

//                for(File dateDir : dateDirs){
//                    Date dirDate = df.parse(dateDir.getName());
//                    if(dirDate.after(firstFullTime)){
//                        continue;
//                    }
//
//                    scp(dateDir.getAbsolutePath()+"\\","Main.exe",
//                            target_path +"\\" + user_id+"\\"+dateDir.getName());
//                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            try {
                if(resultSet!=null){
                    resultSet.close();
                }
                if(statement!= null){
                    resultSet.close();
                }
                if (connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        cleanSaveAfterFullMark();
    }
}
