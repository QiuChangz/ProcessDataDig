package cn.edu.nju.util;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

import java.io.*;

/*
这里假设中文字符只有UTF-8和GB2312这两种情况
 */

public class FileChineseCharsetDetector {
    private boolean found = false;
    private String encoding = null;

    public static void convert(File file,String sourceCharset,String targetCharset) throws IOException {
        if(!sourceCharset.equals(targetCharset)) {
            System.out.println(sourceCharset + " "+ targetCharset);
            InputStreamReader isr = new InputStreamReader(new FileInputStream(
                    file), sourceCharset.toLowerCase());
            BufferedReader br = new BufferedReader(isr);
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                // 注意写入换行符
//                line = URLEncoder.encode(line, targetCharset);
                sb.append(line + "\r\n");//windows 平台下 换行符为 \r\n
            }
            br.close();
            isr.close();
            System.out.println(sb.toString());
            File targetFile = new File(file.getPath());
            OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream(targetFile),targetCharset);
            osw.write(sb.toString());
//            BufferedWriter bw = new BufferedWriter(osw);
//            // 以字符串的形式一次性写入
//            bw.write(sb.toString());
//            bw.close();
            osw.close();

            System.out.println("Deal:" + file.getPath() + ": from : " + sourceCharset +" : to : " + targetCharset );
        }
    }

    public static void main(String[] argv) throws Exception {
        File file1 = new File("C:\\Users\\yuegu\\Desktop\\test\\HandleClipBoard.cs");

        String encoding = new FileChineseCharsetDetector().guessFileEncoding(file1);

        convert(file1,encoding,"UTF-8");
    }

    /**
     * 传入一个文件(File)对象，检查文件编码
     * 这里假设只存在GB2312和UTF-8两种情况
     * @param file
     *            File对象实例
     * @return 文件编码，若无，则返回null
     * @throws FileNotFoundException
     * @throws IOException
     */
    public String guessFileEncoding(File file) throws FileNotFoundException, IOException {
        return guessFileEncoding(file, new nsDetector(3));
    }



    /**
     * 获取文件的编码
     *
     * @param file
     * @param det
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String guessFileEncoding(File file, nsDetector det) throws FileNotFoundException, IOException {
        // Set an observer...
        // The Notify() will be called when a matching charset is found.
        det.Init(new nsICharsetDetectionObserver() {
            public void Notify(String charset) {
                encoding = charset;
                found = true;
            }
        });

        BufferedInputStream imp = new BufferedInputStream(new FileInputStream(file));
        byte[] buf = new byte[1024];
        int len;
        boolean done = false;
        boolean isAscii = false;

        while ((len = imp.read(buf, 0, buf.length)) != -1) {
            // Check if the stream is only ascii.
            if(isAscii){
                isAscii = det.isAscii(buf, len);
            }

            // DoIt if non-ascii and not done yet.
            done = det.DoIt(buf, len, false);
            if (!isAscii && !done)
                done = det.DoIt(buf,len, false);
        }
        imp.close();
        det.DataEnd();

        if (isAscii) {
            encoding = "ASCII";
            found = true;
        }

        if (!found) {
            String[] prob = det.getProbableCharsets();
            boolean isUTF8 = false;
            for(String probCharset : prob){
                if(prob[0] == "UTF-8"){
                    isUTF8 = true;
                }
            }
            return isUTF8 ? "UTF-8" : "GB2312";
        }
        return encoding;
    }
}