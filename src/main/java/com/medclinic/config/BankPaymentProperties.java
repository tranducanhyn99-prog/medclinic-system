package com.medclinic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.payment.bank")
public class BankPaymentProperties {
    private boolean enabled;
    private String bankId = "970422";
    private String bankName = "MB Bank";
    private String accountNo = "";
    private String accountName = "";
    private String qrTemplate = "compact2";
    private String callbackSecret = "";
    private long expiryMinutes = 30;


    public boolean isReady() {
        return enabled
                && bankId != null && bankId.matches("[A-Za-z0-9]{2,20}")
                && accountNo != null && accountNo.matches("\\d{6,19}")
                && accountName != null && !accountName.isBlank()
                && qrTemplate != null && qrTemplate.matches("[A-Za-z0-9_-]{2,30}")
                && callbackSecret != null && callbackSecret.length() >= 32;
    }
}
