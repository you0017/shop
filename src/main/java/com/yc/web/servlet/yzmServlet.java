package com.yc.web.servlet;



import com.aliyun.captcha20230305.models.VerifyCaptchaRequest;
import com.aliyun.captcha20230305.models.VerifyCaptchaResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.yc.web.model.JsonModel;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

@WebServlet(value = "/html/yzm.action")
public class yzmServlet extends BaseServlet {
    String captchaVerifyParam;

    public void regYzm(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JsonModel jm = new JsonModel();
        // 从请求体中获取参数  字节输入流  *****其它方法获取不到信息****
        try (BufferedReader reader = req.getReader() ) {
            String line;
            int flag = 1;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) { // 跳过空行
//                    System.out.println("第"+(flag++)+"行line:"+line);
                    //具体验证信息参数
                    if (flag == 3){
                        //获取验证数据
                        captchaVerifyParam = line ;
                        break;
                    }
                    flag++;
                }
            }
        }
        //System.out.println("captchaVerifyParam:"+captchaVerifyParam);
        // ====================== 1. 初始化配置 ======================
        Config config = new Config();
        // 设置您的AccessKey ID 和 AccessKey Secret。
        // getEnvProperty只是个示例方法，需要您自己实现AccessKey ID 和 AccessKey Secret安全的获取方式。
        config.accessKeyId = "#";
        config.accessKeySecret = "#";
        //设置请求地址
        config.endpoint = "#";
        // 设置连接超时为5000毫秒
        config.connectTimeout = 5000;
        // 设置读超时为5000毫秒
        config.readTimeout = 5000;
        // ====================== 2. 初始化客户端（实际生产代码中建议复用client） ======================
        com.aliyun.captcha20230305.Client client = new com.aliyun.captcha20230305.Client(config);
        // 创建APi请求
        VerifyCaptchaRequest request = new VerifyCaptchaRequest();
        request.captchaVerifyParam = captchaVerifyParam;
        //对前端传来的数据  进行  验证    得到验证结果
        VerifyCaptchaResponse response = client.verifyCaptcha(request);
        // ====================== 3. 发起请求） ======================
        Map map = new HashMap();
        try {
            // 建议使用您系统中的日志组件，打印返回
            // 获取验证码验证结果（请注意判空），将结果返回给前端。出现异常建议认为验证通过，优先保证业务可用，然后尽快排查异常原因。
            Boolean captchaVerifyResult = response.body.result.verifyResult; //验证结果  true为通过，false为不通过
            //jm.setCode(captchaVerifyResult); //  验证  结果  true为通过，false为不通过
            map.put("code",captchaVerifyResult);
            //System.out.println("I an person:"+captchaVerifyResult);
            //System.out.println(response.body.getCode()); // 表示验证情况  Success
        } catch (TeaException error) {
            // 建议使用您系统中的日志组件，打印异常
            // 出现异常建议认为验证通过，优先保证业务可用，然后尽快排查异常原因。
            Boolean captchaVerifyResult = true;
            //jm.setCode(captchaVerifyResult);
            map.put("code",captchaVerifyResult);
            jm.setError(response.body.getCode()); //错误信息
            //System.out.println("Error message:"+jm.getError());
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 建议使用您系统中的日志组件，打印异常
            // 出现异常建议认为验证通过，优先保证业务可用，然后尽快排查异常原因。
            Boolean captchaVerifyResult = true;
            //jm.setCode(captchaVerifyResult);
            map.put("code",captchaVerifyResult);
            jm.setError(response.body.getCode()); //错误信息
            //System.out.println("Error message:"+jm.getError());
        }finally {
            super.writeObjectToJson(map,resp);
        }
    }
}
