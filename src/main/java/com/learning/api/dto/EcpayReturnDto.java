package com.learning.api.dto;

import lombok.Data;

@Data
public class EcpayReturnDto {
    private String MerchantTradeNo;
    private String RtnCode;      // 1 為成功
    private String TradeAmt;     // 交易金額
    private String PaymentDate;
    private String CustomField1; // 我們放的 userId
    private String TradeNo;      // 綠界流水號
}
