package com.test.core.util.ftp;

import java.io.IOException;

import com.test.core.util.PropertiesUtil;

/**
 * FTP工具类
 */
public class FTPFactory {


    //获取一个实例
    public static FTPUtil getInstance( ) throws IOException {

        String host = PropertiesUtil.getValueByKey("config.properties","FTP.host");
        if (host != null) {
            int port = Integer.parseInt(PropertiesUtil.getValueByKey("config.properties","FTP.port"));
            String username = PropertiesUtil.getValueByKey("config.properties","FTP.username");
            String password =PropertiesUtil.getValueByKey("config.properties", "FTP.password");
            String remoteDir =PropertiesUtil.getValueByKey("config.properties","FTP.remoteDir");
            String localDir = PropertiesUtil.getValueByKey("config.properties", "FTP.localDir");
            String Encoding = PropertiesUtil.getValueByKey("config.properties", "FTP.Encoding");
            boolean passiveMode = new Boolean(PropertiesUtil.getValueByKey("config.properties","FTP.passiveMode")).booleanValue();
            FTPVo vo = new FTPVo(host, port, username, password, remoteDir, localDir, Encoding, passiveMode);
            return new FTPUtilImpl(vo);
        } else {
            throw new IOException("config error");
        }
    }
}
