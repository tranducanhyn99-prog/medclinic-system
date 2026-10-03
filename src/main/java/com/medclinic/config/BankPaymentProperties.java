package com.medclinic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
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

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getBankId() { return bankId; }
    public void setBankId(String bankId) { this.bankId = bankId; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getQrTemplate() { return qrTemplate; }
    public void setQrTemplate(String qrTemplate) { this.qrTemplate = qrTemplate; }
    public String getCallbackSecret() { return callbackSecret; }
    public void setCallbackSecret(String callbackSecret) { this.callbackSecret = callbackSecret; }
    public long getExpiryMinutes() { return expiryMinutes; }
    public void setExpiryMinutes(long expiryMinutes) { this.expiryMinutes = expiryMinutes; }

    public boolean isReady() {
        return enabled
                && bankId != null && bankId.matches("[A-Za-z0-9]{2,20}")
                && accountNo != null && accountNo.matches("\\d{6,19}")
                && accountName != null && !accountName.isBlank()
                && qrTemplate != null && qrTemplate.matches("[A-Za-z0-9_-]{2,30}")
                && callbackSecret != null && callbackSecret.length() >= 32;
    }
}
