package com.yc.config;

import java.io.FileWriter;
import java.io.IOException;

public class AlipayConfig {

	public static String app_id = "#";

    public static String merchant_private_key = "#";
    public static String alipay_public_key = "#";

	//public static String notify_url = "index.html?status=1";
	//public static String notify_url = "http://localhost:8080/shop_war/html/index.html?status=1";

	//public static String return_url = "index.html?status=1";
	//public static String return_url = "http://localhost:8080/shop_war/html/index.html?status=1";

	public static String sign_type = "RSA2";

	public static String charset = "utf-8";



	public static String log_path = "F:\\";



    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            System.out.println("1");
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

