
package com.snail.dnslb4j.util;

 
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import jodd.io.StreamUtil;

/**
 *
 * @author pengmeng
 */
public class Jar {

    /**
     * 读取指定类所在jar包里面的文件内容,绝对路径／开头<br/>
     * 从jar根目录开始。如果没有／开头，则路径相对指定的类。
     *
     * @param clazz 类
     * @param path 文件在jar包里面的路径
     * @return
     */
    public static String getFileContents(Class clazz, String path) throws IOException {
         
        return new String(StreamUtil.readBytes(clazz.getResourceAsStream(path)));
    }

    /**
     * 读取指定类所在jar包里面的文件内容,绝对路径／开头<br/>
     * 从jar根目录开始。如果没有／开头，则路径相对指定的类。
     *
     * @param clazz 类
     * @param path 文件在jar包里面的路径
     * @param encoding 文件内容编码，例如UTF-8，GBK，GB2312
     * @return
     */
    public static String getFileContents(Class clazz, String path, String encoding) throws IOException {
        return new String(StreamUtil.readBytes(clazz.getResourceAsStream(path)),encoding);
    }

    /**
     * 获得指定类所在jar包里面的文件输入流,绝对路径／开头<br/>
     * 从jar根目录开始。如果没有／开头，则路径相对指定的类。
     *
     * @param clazz
     * @param path
     * @return
     */
    public static InputStream getInputstream(Class clazz, String path) {
        return clazz.getResourceAsStream(path);
    }

    /**
     * 获取jar包所在文件夹路经
     *
     * @param clazz jar包中任意的一个类
     * @return
     */
    public static String getPath(Class clazz) {
        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        String filePath;
        try {
            filePath = URLDecoder.decode(url.getPath(), "utf-8");// 转化为utf-8编码
        } catch (UnsupportedEncodingException e) {
            return "";
        }
        if (filePath.endsWith(".jar")) {// 可执行jar包运行的结果里包含".jar"
            // 截取路径中的jar包名
            filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
        }

        File file = new File(filePath); 
        filePath = file.getAbsolutePath(); 

        return filePath;
    }
}
