import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.model.FileListing;
import com.qiniu.util.Auth;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Sync {

    public static void main(String[] args) {
        //设置需要操作的账号的AK和SK
        String ACCESS_KEY = "SRivNTTGWtF6kQbgJtRl3gaBTaqOAz3aI84WPVMl";
        String SECRET_KEY = "DtEk5NwK_3tjdu1Iypeg9D9-plE-lYxx3MgWN7W2";
        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

        Zone z = Zone.zone0();
        Configuration c = new Configuration(z);

        //实例化一个BucketManager对象
        BucketManager bucketManager = new BucketManager(auth, c);

        //要列举文件的空间名
        String bucket = "yujunyi";

        //构造私有空间的需要生成的下载的链接
        String URL = "http://pbfj03m4a.bkt.clouddn.com";

        // 本地地址
        String syncPath = "D:/微云同步助手/373948112/weiyun/blog/pic";

        String maker = null;

        boolean flag = true;

        while (flag) {
            try {
                //调用listFiles方法列举指定空间的指定文件
                //参数一：bucket    空间名
                //参数二：prefix    文件名前缀
                //参数三：marker    上一次获取文件列表时返回的 marker
                //参数四：limit     每次迭代的长度限制，最大1000，推荐值 100
                //参数五：delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
                FileListing fileListing = bucketManager.listFiles(bucket, null, maker, 10, null);
                maker = fileListing.marker;
                if (maker == null) {
                    flag = false;
                }
                FileInfo[] items = fileListing.items;
                File file = null;
                String[] fInfo = null;
                for (FileInfo fileInfo : items) {
//                System.out.println(fileInfo.key);
                    fInfo = fileInfo.key.toString().split("/");
                    file = new File(syncPath + "\\" + fileInfo.key);

                    if (file.exists()) {
                        continue;
                    }
                    downLoadFromUrl(URL + "/" + fileInfo.key, fInfo[1], syncPath + "\\" + fInfo[0]);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从网络Url中下载文件
     *
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    public static void downLoadFromUrl(String urlStr, String fileName, String savePath) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3 * 1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取自己数组
        byte[] getData = readInputStream(inputStream);

        //文件保存位置
        File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        File file = new File(saveDir + File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if (fos != null) {
            fos.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }


        System.out.println("info:" + url + " download success");

    }

    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }
}
