package com.tzsw.utils;

import com.tzsw.support.Constant;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Author: chenpeng
 * @Description:
 * @Date: Created in 16:29 2018/11/22
 */
public class FTPUtil {

    /**
     * 连接 FTP 服务器
     *
     * @param addr     FTP 服务器 IP 地址
     * @param port     FTP 服务器端口号
     * @param username 登录用户名
     * @param password 登录密码
     * @return
     * @throws Exception
     */
    public static FTPClient connectFtpServer(String addr, int port, String username, String password, String controlEncoding) {
        FTPClient ftpClient = new FTPClient();
        try {
            /**设置文件传输的编码*/
            ftpClient.setControlEncoding(controlEncoding);

            /**连接 FTP 服务器
             * 如果连接失败，则此时抛出异常，如ftp服务器服务关闭时，抛出异常：
             * java.net.ConnectException: Connection refused: connect*/
            ftpClient.connect(addr, port);
            /**登录 FTP 服务器
             * 1）如果传入的账号为空，则使用匿名登录，此时账号使用 "Anonymous"，密码为空即可*/
            if (StringUtils.isBlank(username)) {
                ftpClient.login("Anonymous", "");
            } else {
                ftpClient.login(username, password);
            }

            /** 设置传输的文件类型
             * BINARY_FILE_TYPE：二进制文件类型
             * ASCII_FILE_TYPE：ASCII传输方式，这是默认的方式
             * ....
             */
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            /**
             * 确认应答状态码是否正确完成响应
             * 凡是 2开头的 isPositiveCompletion 都会返回 true，因为它底层判断是：
             * return (reply >= 200 && reply < 300);
             */
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                /**
                 * 如果 FTP 服务器响应错误 中断传输、断开连接
                 * abort：中断文件正在进行的文件传输，成功时返回 true,否则返回 false
                 * disconnect：断开与服务器的连接，并恢复默认参数值
                 */
                ftpClient.abort();
                ftpClient.disconnect();
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(">>>>>FTP服务器连接登录失败，请检查连接参数是否正确，或者网络是否通畅*********");
        }
        return ftpClient;
    }

    /**
     * 使用完毕，应该及时关闭连接
     * 终止 ftp 传输
     * 断开 ftp 连接
     *
     * @param ftpClient
     * @return
     */
    public static FTPClient closeFTPConnect(FTPClient ftpClient) {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.abort();
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ftpClient;
    }

    /**
     * 下载 FTP 服务器上指定的单个文件，而且本地存放的文件相对部分路径 会与 FTP 服务器结构保持一致
     *
     * @param ftpClient              ：连接成功有效的 FTP客户端连接
     * @param absoluteLocalDirectory ：本地存储文件的绝对路径，如 E:\gxg\ftpDownload
     * @param relativeRemotePath     ：ftpFile 文件在服务器所在的绝对路径，此方法强制路径使用右斜杠"\"，如 "\video\2018.mp4"
     * @return
     */
    public static String downloadSingleFile(FTPClient ftpClient, String absoluteLocalDirectory, String relativeRemotePath) {
        /**如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            System.out.println(">>>>>FTP服务器连接已经关闭或者连接无效*********");
            return "";
        }
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            /**没有对应路径时，FTPFile[] 大小为0，不会为null*/
            FTPFile[] ftpFiles = ftpClient.listFiles(relativeRemotePath);
            FTPFile ftpFile = null;
            if (ftpFiles.length >= 1) {
                ftpFile = ftpFiles[0];
            }
            if (ftpFile != null && ftpFile.isFile()) {
                /** ftpFile.getName():获取的是文件名称，如 123.mp4
                 * 必须保证文件存放的父目录必须存在，否则 retrieveFile 保存文件时报错
                 */
//                File localFile = new File(absoluteLocalDirectory, relativeRemotePath);
//                if (!localFile.getParentFile().exists()) {
//                    localFile.getParentFile().mkdirs();
//                }
//                OutputStream outputStream = new FileOutputStream(localFile);
                String workDir = relativeRemotePath.substring(0, relativeRemotePath.lastIndexOf("\\"));
                if (StringUtils.isBlank(workDir)) {
                    workDir = "/";
                }
                /**文件下载前，FTPClient工作目录必须切换到文件所在的目录，否则下载失败
                 * "/" 表示用户根目录*/
                ftpClient.changeWorkingDirectory(workDir);
                /**下载指定的 FTP 文件 到本地
                 * 1)注意只能是文件，不能直接下载整个目录
                 * 2)如果文件本地已经存在，默认会重新下载
                 * 3)下载文件之前，ftpClient 工作目录必须是下载文件所在的目录
                 * 4)下载成功返回 true，失败返回 false
                 */
//                ftpClient.retrieveFile(ftpFile.getName(), outputStream);

                reader = new BufferedReader(new InputStreamReader(ftpClient.retrieveFileStream(ftpFile.getName()), "utf-8"));
                String text = null;
                while ((text = reader.readLine()) != null) {
                    sb.append(text);
                }
//            fileOutputStream.close();

//                outputStream.flush();
//                outputStream.close();
//                System.out.println(">>>>>FTP服务器文件下载完毕*********" + ftpFile.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

//    public static void main(String[] args) throws Exception {
//        System.out.println("-----------------------应用启动------------------------");
//        FTPClient ftpClient = FTPUtil.connectFtpServer("10.0.14.30", 21, "Adminstrator", "xtp1215", System.getProperty("file.encoding"));
//        downloadSingleFile(ftpClient, "d:\\home\\download", "/test.txt");
//        closeFTPConnect(ftpClient);
//        System.out.println("-----------------------应用关闭------------------------");
//    }


    /**
     * 遍历 FTP 服务器指定目录下的所有文件(包含子孙文件)
     *
     * @param ftpClient        ：连接成功有效的 FTP客户端连接
     * @param remotePath       ：查询的 FTP 服务器目录，如果文件，则视为无效，使用绝对路径，如"/"、"/video"、"\\"、"\\video"
     * @param relativePathList ：返回查询结果，其中为服务器目录下的文件相对路径，如：\1.png、\docs\overview-tree.html 等
     * @return
     */
    public static List<String> loopServerPath(FTPClient ftpClient, String remotePath, List<String> relativePathList) {
        /**如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            System.out.println("ftp 连接已经关闭或者连接无效......");
            return relativePathList;
        }
        try {
            /**转移到FTP服务器根目录下的指定子目录
             * 1)"/"：表示用户的根目录，为空时表示不变更
             * 2)参数必须是目录，当是文件时改变路径无效
             * */
            ftpClient.changeWorkingDirectory(remotePath);
            /** listFiles：获取FtpClient连接的当前下的一级文件列表(包括子目录)
             * 1）FTPFile[] ftpFiles = ftpClient.listFiles("/docs/info");
             *      获取服务器指定目录下的子文件列表(包括子目录)，以 FTP 登录用户的根目录为基准，与 FTPClient 当前连接目录无关
             * 2）FTPFile[] ftpFiles = ftpClient.listFiles("/docs/info/springmvc.txt");
             *      获取服务器指定文件，此时如果文件存在时，则 FTPFile[] 大小为 1，就是此文件
             * */
            FTPFile[] ftpFiles = ftpClient.listFiles();
            if (ftpFiles != null && ftpFiles.length > 0) {
                for (FTPFile ftpFile : ftpFiles) {
                    if (ftpFile.isFile()) {
                        String relativeRemotePath = remotePath + "\\" + ftpFile.getName();
                        relativePathList.add(relativeRemotePath);
                    } else {
                        loopServerPath(ftpClient, remotePath + "\\" + ftpFile.getName(), relativePathList);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return relativePathList;
    }

//    public static void main(String[] args) throws Exception {
//
//        System.out.println("-----------------------应用启动------------------------");
//        FTPClient ftpClient = FTPUtil.connectFtpServer("192.168.43.201", 21, "Adminstrator", "xtp1215", System.getProperty("file.encoding"));
//
//        List<String> relativePathList = new ArrayList<>();
//
//        relativePathList = loopServerPath(ftpClient, "\\test", relativePathList);
//        for (String relativePath : relativePathList) {
//            System.out.println("准备下载的服务器文件：" + relativePath);
//            downloadSingleFile(ftpClient, "d:\\home\\download", relativePath);
//        }
//        closeFTPConnect(ftpClient);
//        System.out.println("-----------------------应用关闭------------------------");
//    }


    /**
     * 上传本地文件 或 目录 至 FTP 服务器----保持 FTP 服务器与本地 文件目录结构一致
     *
     * @param ftpClient 连接成功有效的 FTPClinet
     * @param xmlString 待上传数据
     * @param path      FTP服务器保存目录,如果是根目录则为“/”
     * @param fileName  文件名
     * @throws Exception
     */
    public static boolean uploadFiles(FTPClient ftpClient, String xmlString, String path, String fileName) {
        boolean result = false;
        /**如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            System.out.println(">>>>>FTP服务器连接已经关闭或者连接无效*****放弃文件上传****");
            return result;
        }
        if (StringUtils.isBlank(xmlString)) {
            System.out.println(">>>>>待上传数据不存在*****放弃文件上传****");
            return result;
        }
        try {
            /**如果被上传的是文件时*/
            InputStream input = new ByteArrayInputStream(xmlString.getBytes(System.getProperty("file.encoding")));
            /** storeFile:将本地文件上传到服务器
             * 1）如果服务器已经存在此文件，则不会重新覆盖,即不会再重新上传
             * 2）如果当前连接FTP服务器的用户没有写入的权限，则不会上传成功，但是也不会报错抛异常
             * */
            boolean change = ftpClient.changeWorkingDirectory(path);
            if (change) {
                result = ftpClient.storeFile(fileName, input);
                if (result) {
                    System.out.println(">>>>>文件上传成功****");
                }
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) throws Exception {

        System.out.println("-----------------------应用启动------------------------");
        FTPClient ftpClient = FTPUtil.connectFtpServer("10.0.15.130", 21, "Administrator", "xtp1215", "GBK");
        String xml = "<?xml version=\"1.0\" encoding=\"GBK\"?><Package>123你好啊</Package>";
//        boolean result = uploadFiles(ftpClient, xml, "/test", "SBF_JFXX101_369900990136000020180905000005.xml");
        String reslut = readFile(ftpClient, "/test", "SBF_JFXX101_369900990136000020180905000005.xml");
        System.out.println(reslut);
        closeFTPConnect(ftpClient);
        System.out.println("-----------------------应用关闭------------------------");
    }


    /**
     * 同步本地目录与 FTP 服务器目录
     * 1）约定：FTP 服务器有，而本地没有的，则下载下来；本地有，而ftp服务器没有的，则将本地多余的删除
     * 2）始终确保本地与 ftp 服务器内容一致
     * 2）让 FTP 服务器与 本地目录保持结构一致，如 服务器上是 /docs/overview-tree.html，则本地也是 localDir/docs/overview-tree.html
     *
     * @param ftpClient        连接成功有效的 FTPClinet
     * @param localSyncFileDir ：与 FTP 目录进行同步的本地目录
     */
    public static void syncLocalDir(FTPClient ftpClient, String localSyncFileDir) throws IOException {
        /**如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable() || StringUtils.isBlank(localSyncFileDir)) {
            System.out.println(">>>>>FTP服务器连接已经关闭或者连接无效*********");
            return;
        }

        /** 获取本地存储目录下的文件*/
        Collection<File> fileCollection = localListFiles(new File(localSyncFileDir));
        System.out.println(">>>>>本地存储目录共有文件数量*********" + fileCollection.size());

        /**获取 FTP 服务器下的相对路径*/
        List<String> relativePathList = new ArrayList<>();
        relativePathList = loopServerPath(ftpClient, "", relativePathList);
        System.out.println(">>>>>FTP 服务器端共有文件数量*********" + relativePathList.size());

        /**
         * 遍历 本地存储目录下的文件
         * 1）如果本地文件在 FTP 服务器上不存在，则删除它
         * 2）如果本地文件在 FTP 服务器上存在，则比较两种大小
         *    如果大小不一致，则重新下载
         */
        for (File localFile : fileCollection) {
            String localFilePath = localFile.getPath();
            String localFileSuffi = localFilePath.replace(localSyncFileDir, "");
            if (relativePathList.contains(localFileSuffi)) {
                /**本地此文件在 FTP 服务器存在
                 * 1)比较大小，如果本地文件与服务器文件大小一致，则跳过
                 * 2）如果大小不一致，则删除本地文件，重新下载
                 * 3）最后都要删除relativePathList中的此元素，减轻后一次循环的压力*/
                FTPFile[] ftpFiles = ftpClient.listFiles(localFileSuffi);
                System.out.println(">>>>>本地文件 在 FTP 服务器已存在*********" + localFile.getPath());
                if (ftpFiles.length >= 1 && localFile.length() != ftpFiles[0].getSize()) {
                    downloadSingleFile(ftpClient, localSyncFileDir, localFileSuffi);
                    System.out.println(">>>>>本地文件与 FTP 服务器文件大小不一致，重新下载*********" + localFile.getPath());
                }
                relativePathList.remove(localFileSuffi);
            } else {
                System.out.println(">>>>>本地文件在 FTP 服务器不存在，删除本地文件*********" + localFile.getPath());
                /**本地此文件在 FTP 服务器不存在
                 * 1)删除本地文件
                 * 2）如果当前文件所在目录下文件已经为空，则将此父目录也一并删除*/
                localFile.delete();
                File parentFile = localFile.getParentFile();
                while (parentFile.list().length == 0) {
                    parentFile.delete();
                    parentFile = parentFile.getParentFile();
                }
            }
        }
        for (int i = 0; i < relativePathList.size(); i++) {
            System.out.println(">>>>> FTP 服务器存在新文件，准备下载*********" + relativePathList.get(i));
            downloadSingleFile(ftpClient, localSyncFileDir, relativePathList.get(i));
        }
    }

//    public static void main(String[] args) throws Exception {
//
//        System.out.println("-----------------------应用启动------------------------");
//        FTPClient ftpClient = FTPUtil.connectFtpServer("192.168.43.201", 21, "Administrator", "xtp1215", System.getProperty("file.encoding"));
//
//        syncLocalDir(ftpClient,"d:\\home\\download");
//
//        closeFTPConnect(ftpClient);
//        System.out.println("-----------------------应用关闭------------------------");
//    }


    /**
     * 删除服务器的文件
     *
     * @param ftpClient   连接成功且有效的 FTP客户端
     * @param deleteFiles 待删除的文件或者目录，为目录时，会逐个删除，
     *                    路径必须是绝对路径，如 "/1.png"、"/video/3.mp4"、"/images/2018"
     *                    "/" 表示用户根目录,则删除所有内容
     */
    public static boolean deleteServerFiles(FTPClient ftpClient, String deleteFiles) {
        boolean result = false;
        /**如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            System.out.println(">>>>>FTP服务器连接已经关闭或者连接无效*****放弃文件上传****");
            return result;
        }
        try {
            /** 尝试改变当前工作目录到 deleteFiles
             * 1）changeWorkingDirectory：变更FTPClient当前工作目录，变更成功返回true，否则失败返回false
             * 2）如果变更工作目录成功，则表示 deleteFiles 为服务器已经存在的目录
             * 3）否则变更失败，则认为 deleteFiles 是文件，是文件时则直接删除
             */
            boolean changeFlag = ftpClient.changeWorkingDirectory(deleteFiles);
            if (changeFlag) {
                /**当被删除的是目录时*/
                FTPFile[] ftpFiles = ftpClient.listFiles();
                for (FTPFile ftpFile : ftpFiles) {
                    System.out.println("----------------::::" + ftpClient.printWorkingDirectory());
                    if (ftpFile.isFile()) {
                        boolean deleteFlag = ftpClient.deleteFile(ftpFile.getName());
                        if (deleteFlag) {
                            System.out.println(">>>>>删除服务器文件成功****" + ftpFile.getName());
                        } else {
                            System.out.println(">>>>>删除服务器文件失败****" + ftpFile.getName());
                        }
                    } else {
                        /**printWorkingDirectory：获取 FTPClient 客户端当前工作目录
                         * 然后开始迭代删除子目录
                         */
                        String workingDirectory = ftpClient.printWorkingDirectory();
                        deleteServerFiles(ftpClient, workingDirectory + "/" + ftpFile.getName());
                    }
                }
                /**printWorkingDirectory：获取 FTPClient 客户端当前工作目录
                 * removeDirectory：删除FTP服务端的空目录，注意如果目录下存在子文件或者子目录，则删除失败
                 * 运行到这里表示目录下的内容已经删除完毕，此时再删除当前的为空的目录，同时将工作目录移动到上移层级
                 * */
                String workingDirectory = ftpClient.printWorkingDirectory();
                ftpClient.removeDirectory(workingDirectory);
                ftpClient.changeToParentDirectory();
            } else {
                /**deleteFile：删除FTP服务器上的文件
                 * 1）只用于删除文件而不是目录，删除成功时，返回 true
                 * 2）删除目录时无效,方法返回 false
                 * 3）待删除文件不存在时，删除失败，返回 false
                 * */
                result = ftpClient.deleteFile(deleteFiles);
                if (result) {
                    System.out.println(">>>>>删除服务器文件成功****" + deleteFiles);
                } else {
                    System.out.println(">>>>>删除服务器文件失败****" + deleteFiles);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

//    public static void main(String[] args) throws Exception {
//
//        System.out.println("-----------------------应用启动------------------------");
//        FTPClient ftpClient = FTPUtil.connectFtpServer("10.0.15.130", 21, "Administrator", "xtp1215", System.getProperty("file.encoding"));
//        deleteServerFiles(ftpClient,"/test/hahaha.txt");
//        closeFTPConnect(ftpClient);
//        System.out.println("-----------------------应用关闭------------------------");
//    }

    /**
     * 遍历目录下的所有文件--方式1
     *
     * @param targetDir
     */
    public static Collection<File> localListFiles(File targetDir) {
        Collection<File> fileCollection = new ArrayList<>();
        if (targetDir != null && targetDir.exists() && targetDir.isDirectory()) {
            /**
             * targetDir：不要为 null、不要是文件、不要不存在
             * 第二个 文件过滤 参数如果为 FalseFileFilter.FALSE ，则不会查询任何文件
             * 第三个 目录过滤 参数如果为 FalseFileFilter.FALSE , 则只获取目标文件夹下的一级文件，而不会迭代获取子文件夹下的文件
             */
            fileCollection = FileUtils.listFiles(targetDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        }
        return fileCollection;
    }

    public static String readFile(FTPClient ftpClient, String relativeRemotePath, String filaName) {
        /**如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            System.out.println(">>>>>FTP服务器连接已经关闭或者连接无效*********");
            return "";
        }
        if (StringUtils.isBlank(relativeRemotePath)) {
            System.out.println(">>>>>下载时遇到本地存储路径或者ftp服务器文件路径为空，放弃...*********");
            return "";
        }
        String text = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            ftpClient.changeWorkingDirectory(relativeRemotePath);
            inputStream = ftpClient.retrieveFileStream(filaName);
            reader = new BufferedReader(new InputStreamReader(inputStream, System.getProperty("file.encoding")));
            StringBuffer sb = new StringBuffer();
            while ((text = reader.readLine()) != null) {
                sb.append(text);
            }
            System.out.println(">>>>>FTP服务器文件读取完毕*********");
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}