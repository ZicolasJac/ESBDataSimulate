package com.dc.esb.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class FileTool {

    /**
     * 浠ュ瓧鑺備负鍗曚綅璇诲彇鏂囦欢锛岄�氬父鐢ㄤ簬璇诲彇浜岃繘鍒舵枃浠讹紝濡傚浘鐗�
     * @param path
     * @return
     */
    public static String readByBytes(String path) {
        String content = null;

        try {
            InputStream inputStream = new FileInputStream(path);
            StringBuffer sb = new StringBuffer();
            int c = 0;
            byte[] bytes = new byte[1024];

            while ((c = inputStream.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, c, "utf-8"));
            }

            content = sb.toString();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    public static String readByLines(String path) {
        String content = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "utf-8"));
            StringBuffer sb = new StringBuffer();
            String temp = null;
            while ((temp = bufferedReader.readLine()) != null) {
                sb.append(temp+"\n");
            }
            content = sb.toString();
            bufferedReader.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static String readByChars(String path) {
        String content = null;
        try {
            Reader reader = new InputStreamReader(new FileInputStream(path), "utf-8");
            StringBuffer sb = new StringBuffer();
            char[] tempchars = new char[1024];
            while (reader.read(tempchars) != -1) {
                sb.append(tempchars);
            }
            content = sb.toString();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }


    public static boolean saveAs(String content, String path) {
        FileWriter fw = null;
        try {
         
            fw = new FileWriter(new File(path), false);
            if (content != null) {
                fw.write(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fw != null) {
                try {
                    fw.flush();
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
