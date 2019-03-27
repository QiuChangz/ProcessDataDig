package cn.edu.nju.util;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    private static String encoding = "utf-8";
    private static FileChineseCharsetDetector charsetDetector = new FileChineseCharsetDetector();

    public static void removeFile(String fileLocation){
        File file = new File(fileLocation);

        if (file.isDirectory()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }  else {
            if (!file.delete()){
                System.out.println("FILE_DELETE_FAILED: " + fileLocation);
            }
        }
    }
    public static void writeFile(String content, String path){
        writeFile(content, path, false);
    }

    public static void writeFile(String content, String path, boolean add){
        File file = new File(path);
        try {
            if (!file.exists()){
                if(!file.createNewFile()){
                    System.out.println("FILE_CREATE_FAILED: " + path);
                    return;
                }
            }
            FileWriter writer = new FileWriter(file, add);
            writer.write(content);
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    static void copyFile(String srcFile, String dstFile, boolean append){
        try{
            File file = new File(dstFile);
            if (!file.exists()){
                if(!file.createNewFile()){
                    throw new Exception("FILE_CREATE_FAILED: " + dstFile);
                }
            }

            FileOutputStream fos = new FileOutputStream(dstFile);
            //创建搬运工具
            byte datas[] = new byte[1024*8];
            //创建长度
            int len = 0;

            if (append){
                FileInputStream fis = new FileInputStream(dstFile);
                //循环读取数据
                while((len = fis.read(datas))!=-1){
                    fos.write(datas,0,len);
                }
                fis.close();
            }
            FileInputStream fis = new FileInputStream(srcFile);
            len = 0;
            //循环读取数据
            while((len = fis.read(datas))!=-1){
                fos.write(datas,0,len);
            }
            //3.释放资源
            fis.close();
            fos.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    static void copyFile(String srcFile, String dstFile){
        copyFile(srcFile, dstFile, false);
    }

    public static String readFile(File file){
        StringBuilder content = new StringBuilder();
        BufferedReader reader =null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetDetector.guessFileEncoding(file)));
            String tmp = null;
            while ((tmp = reader.readLine()) != null){
                content.append(tmp).append("\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return content.toString();
    }

    public static List<String> getDirTimes(String dirLocation){
        File file = new File(dirLocation);
        List<String> dates = new ArrayList<>();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        if (file.listFiles() == null || file.listFiles().length == 0){
            System.out.println("INCORRECT_FILE_DIR: " + file.getAbsolutePath());
            return new ArrayList<>();
        }
        for (File dir: file.listFiles()){
            try {
                df.parse(dir.getName());
                dates.add(dir.getAbsolutePath());
            } catch (ParseException e) {
                System.out.println("UNKNOWN_DATE_DIR_FOUND: " + dir.getAbsolutePath());
            }
        }
        return dates;
    }
}
