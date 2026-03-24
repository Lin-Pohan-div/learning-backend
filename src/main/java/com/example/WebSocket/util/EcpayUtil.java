package com.example.WebSocket.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.stream.Collectors;

public class EcpayUtil {

    public static String generate(Map<String, String> params,
                                  String hashKey, String hashIV) throws Exception {

        String query = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        String raw = "HashKey=" + hashKey + "&" + query + "&HashIV=" + hashIV;
        raw = URLEncoder.encode(raw, StandardCharsets.UTF_8)
                .toLowerCase()
                .replace("%21", "!")
                .replace("%2a", "*")
                .replace("%28", "(")
                .replace("%29", ")");

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest).toUpperCase();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    public static boolean verify(Map<String, String> data, String hashKey, String hashIV) throws Exception {
        // 1. 先取出綠界傳過來的檢查碼
        String ecpayCheckMacValue = data.get("CheckMacValue");
        if (ecpayCheckMacValue == null) return false;

        // 2. 移除 Map 中的 CheckMacValue 欄位（因為計算時不能包含它自己）
        Map<String, String> verifyParams = data.entrySet().stream()
                .filter(e -> !e.getKey().equals("CheckMacValue"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 3. 使用你原本的 generate 方法重新算一次
        String calculatedValue = generate(verifyParams, hashKey, hashIV);

        // 4. 比對重新算出來的是否跟綠界傳來的一樣
        return calculatedValue.equals(ecpayCheckMacValue);
    }
}