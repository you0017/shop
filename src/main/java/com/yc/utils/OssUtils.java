package com.yc.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.PutObjectResult;
import org.apache.commons.lang3.SerializationUtils;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.UUID;


public class OssUtils {


    public static void creatOssSpace() throws Exception {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "#";
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "#";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);

        try {
            // 创建存储空间。
            ossClient.createBucket(bucketName);

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }


    /**
     * 实现上传图片到OSS
     */
    public String upload(Part file) throws IOException, com.aliyuncs.exceptions.ClientException {
        String endpoint = "#";
        String accessKeyId = "#";
        String accessKeySecret = "#";
        String bucketName = "#";


        // 获取上传的文件的输入流
        InputStream inputStream = file.getInputStream();

        // 避免文件覆盖
        String originalFilename = file.getSubmittedFileName();
        String fileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));
        //String fileName = file.getOriginalFilename();

        //上传文件到 OSS
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, fileName, inputStream);

        //文件访问路径
        String url = endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + fileName;
        // 关闭ossClient
        ossClient.shutdown();
        return url;// 把上传到oss的路径返回
    }



    // 上传到oss里面(图片)
    public Boolean UploadImageToOSS(String objectName, InputStream inputStream) throws Exception {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "#";
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        //EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        //获取阿里云OSS参数

        String accessKeyId = "#";
        String accessKeySecret = "#";


        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "#";
        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
//        String objectName = "sh-hengyang/a.txt";

        // 创建OSSClient实例。
        //OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
        try {
            PutObjectResult result = ossClient.putObject(bucketName, objectName, inputStream);
            if (result != null) {
                return true;
            }

        } catch (OSSException oe) {
            System.out.println("ossutil有问题");
        } finally {
            if (ossClient != null) {
//                System.out.println("没问题");
                ossClient.shutdown();
            }
        }
        return false;
    }

    // 删除oss里面的文件(图片)
    public static void deleteImage() throws Exception {

        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "#";
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "#";
        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
        String objectName = "#";


        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);

        try {
            // 删除文件。
            ossClient.deleteObject(bucketName, objectName);
            System.out.println("删除成功");
        } catch (OSSException oe) {

            System.out.println("删除失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }


    public static byte[] objectToByteArray(Object obj) throws IOException {
        byte[] byteArray = null;
        try {
            byteArray = SerializationUtils.serialize((Serializable) obj);
            // 这里可以使用转换后的byteArray
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteArray;
    }
}




