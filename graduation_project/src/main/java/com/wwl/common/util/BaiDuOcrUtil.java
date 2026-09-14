package com.wwl.common.util;

import com.baidu.aip.ocr.AipOcr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 浣滆?咃細鐜嬫枃鏋?
 * 鏃堕棿锛?026 2026/5/9 涓嬪崍1:20
 * 鎻忚堪锛?
 */
@Component
public class BaiDuOcrUtil {

    @Value("${baidu.ocr.app-id}")
    private String appId;

    @Value("${baidu.ocr.api-key}")
    private String apiKey;

    @Value("${baidu.ocr.secret-key}")
    private String secretKey;

    public AipOcr getOcrClient(){
        return new AipOcr(appId,apiKey,secretKey);
    }
}
