/*
package com.linhei.queryuserid;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GetText implements Runnable {
    private URL url = null;     //URL
    private URLConnection urlConn = null;  //url连接
    private BufferedReader bufIn = null;    //缓冲读取器，读取网页信息

    //    private static final String TEXT_REG = "([[[^\\x00-\\xff]|\\w]&&[^u003E]])+";    //文本内容正则
    private static final String TEXT_REG = "[[\\s\\S]&&[^<>\\n ]]*";    //文本内容正则，这里有些问题，试了很久，无法排除一个空格的情况
//    private  static final String TEXT_REG = "[[\\s\\S]&&[^<>\\n ]]*";
//    <[a-zA-Z]+.*?>([\s\S]*?)</[a-zA-Z]*?>

    private String findText = null;         //要查找的内容

    private String downloadPath = null;     //保存路径
    private String fileName = null;         //文件名

    //构造，参数：url、要查找的文本、保存路径，文件名
    public GetText(String urlStr*/
/*, String findText, String downloadPath, String fileName*//*
) {
//        createFolder(downloadPath);     //创建文件夹

        try {
            url = new URL(urlStr);
            urlConn = url.openConnection();
            //设置请求属性，有部分网站不加这句话会抛出IOException: Server returned HTTP response code: 403 for URL异常
            //如：b站
            urlConn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            bufIn = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.downloadPath = downloadPath;
        this.fileName = fileName;
        this.findText = findText;
    }

    //检测路径是否存在，不存在则创建
    private void createFolder(String path) {
        File myPath = new File(path);

        if (!myPath.exists())   //不存在则创建文件夹
            myPath.mkdirs();
    }

    //下载函数
    private void Download() {
        final int N = 10;

        String line = "";
        StringBuilder textStr = new StringBuilder();

        while (line != null) {
            for (int i = 0; i < N; i++)      //将N行内容追加到textStr字符串中
                try {
                    line = bufIn.readLine();

                    if (line != null)
                        textStr.append(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            try {
                //将img标签正则封装对象再调用matcher方法获取一个Matcher对象
                final Matcher textMatcher = Pattern.compile("\"mid\":\"(\\d+)\",\"name\":\"([\\W\\w]*)\",\"approve\"").matcher(textStr.toString());

//                final FileWriter fw = new FileWriter(downloadPath + fileName, true);   //文件编写，续写文件

                while (textMatcher.find())          //查找匹配文本
                {
//                    fw.write(textMatcher.group() + "\n");  //写入到文件中
                    System.out.println(textMatcher.group());
                    print(textMatcher.group(1));     //打印一遍
                    print(textMatcher.group(2));     //打印一遍
                }

//                print(textStr);
//                fw.close();
                textStr = new StringBuilder();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //run
    @Override
    public void run() {
        Download();
    }

    //打印语句
    private void print(Object obj) {
        System.out.println(obj);
    }

    public static void main(String[] args) {

        GetText getText = new GetText("https://api.bilibili.com/x/web-interface/card?mid=12890453");
        getText.run();

    }
}
*/
