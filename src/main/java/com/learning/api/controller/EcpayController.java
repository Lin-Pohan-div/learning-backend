package com.learning.api.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.WebSocket.util.EcpayUtil;
import com.learning.api.dto.EcpayReturnDto;
import com.learning.api.entity.User;
import com.learning.api.repo.UserRepo;
import com.learning.api.security.JwtService;
import com.learning.api.service.WalletLogsService;

import jakarta.servlet.http.HttpServletRequest;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/ecpay")
public class EcpayController {
	@Autowired
	private JwtService jwtService;
	@Autowired
    private WalletLogsService walletLogsService;
	@Autowired
	private UserRepo userRepo;
	
    private final String MERCHANT_ID = "3002607";
    private final String HASH_KEY = "pwFHCqoQZGmho4w6";
    private final String HASH_IV = "EkRm7iFT261dpevs";
    private final String ECPAY_URL
    =
            "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5";

    // =========================
    // 付款入口
    // =========================

    
    @PostMapping("/pay")
    public String pay(@RequestParam String ecpayprice, HttpServletRequest request) throws Exception {
    	String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("No token");
        }
        String token = authHeader.substring(7);
        String email = jwtService.email(token); // 從 token 拿 email
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long userId = user.getId(); // 正確的使用者ID
        System.out.println("價格: " + ecpayprice);
        System.out.println("userId: " + userId);
    	
        Map<String, String> params = new LinkedHashMap<>();

        params.put("CustomField1", String.valueOf(userId));
        //商店代號
        params.put("MerchantID", MERCHANT_ID);
        //訂單編號
        params.put("MerchantTradeNo", "TEST" + System.currentTimeMillis());
        //交易時間
        params.put("MerchantTradeDate",
                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        //付款型態(規定aio)
        params.put("PaymentType", "aio");
        //金額
        params.put("TotalAmount", ecpayprice);
        //交易描述
        params.put("TradeDesc", "儲值");
        //商品名稱
        params.put("ItemName", "儲值交易");
        //背景通知 URL
        params.put("ReturnURL",
                "https://subjugable-uncreditably-ignacia.ngrok-free.dev/api/ecpay/return");
        //前端返回 URL
        params.put("ClientBackURL",
                "https://subjugable-uncreditably-ignacia.ngrok-free.dev/ecpay.html");
        //付款方式
        params.put("ChoosePayment", "ALL");
        //加密方式
        params.put("EncryptType", "1");

        // 最後才算 CheckMacValue（⚠️ 一定要最後）
        String checkMacValue = EcpayUtil.generate(params, HASH_KEY, HASH_IV);
        params.put("CheckMacValue", checkMacValue);

        // 回傳自動送出的 HTML
        return buildAutoSubmitForm(ECPAY_URL, params);
    }

    // =========================
    // 組綠界付款表單
    // =========================
    private String buildAutoSubmitForm(String action, Map<String, String> params) {

        StringBuilder sb = new StringBuilder();
        sb.append("<form id='ecpay' method='post' action='")
          .append(action)
          .append("'>");

        params.forEach((k, v) ->
            sb.append("<input type='hidden' name='")
              .append(k)
              .append("' value='")
              .append(v)
              .append("'/>")
        );

        sb.append("</form>");
        sb.append("<script>document.getElementById('ecpay').submit();</script>");

        return sb.toString();
    }

    // 綠界前端給我
    @PostMapping("/return")
    public String returnUrl(@RequestParam Map<String, String> data) throws Exception {
    	boolean isValid = EcpayUtil.verify(data, HASH_KEY, HASH_IV);
        
        if (!isValid) {
            System.out.println("警告：檢查碼驗證失敗！此請求可能是偽造的。");
            return "0|CheckMacValueVerifyFail"; 
        }
        String userId = data.get("CustomField1");
        String RtnCode = data.get("RtnCode");   // 1 = 成功
        String RtnMsg = data.get("RtnMsg"); // 交易成功
        String MerchantTradeNo = data.get("MerchantTradeNo"); //綠界單號（一筆booking 一筆單號）merchant_trade_no
        String PaymentDate = data.get("PaymentDate"); //付款成功時間created_at
        String TradeAmt = data.get("TradeAmt");//支付總金額total_amount

        System.out.println("訂單id: "+ MerchantTradeNo);
        System.out.println("回傳狀態: " + RtnCode + " - " + RtnMsg);
        System.out.println("交易時間: "+ PaymentDate);
        System.out.println("訂單金額: "+ TradeAmt);
        System.out.println("使用者id: "+ userId);

        EcpayReturnDto dto = new EcpayReturnDto();
        dto.setMerchantTradeNo(data.get("MerchantTradeNo"));
        dto.setRtnCode(data.get("RtnCode"));
        dto.setTradeAmt(data.get("TradeAmt"));
        dto.setCustomField1(data.get("CustomField1"));
        if ("1".equals(dto.getRtnCode())) {
            try {
                walletLogsService.processWalletDeposit(dto);
            } catch (Exception e) {
                return "0|ErrorMessage: " + e.getMessage();
            }
        }

        // ⚠️ 綠界規定一定要回
        return "1|OK";
    }
}
