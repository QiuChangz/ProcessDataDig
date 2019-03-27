package cn.edu.nju.util;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropertiesUtil {

    public static String getProperties(String name){
        InputStreamReader inputStream = null;
        Properties properties = new Properties();
        try{
            inputStream = new InputStreamReader(new FileInputStream("Resource\\Properties.properties"), "utf-8");
            properties.load(inputStream);
        }catch (IOException ioE){
            ioE.printStackTrace();
        }finally{
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties.getProperty(name);
    }

    public static void main(String[] args) {
        String info = PropertiesUtil.getProperties("outputFile");
        System.out.println(info);
    }
}
