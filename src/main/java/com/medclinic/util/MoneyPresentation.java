package com.medclinic.util;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** One presentation rule for the website and the exported invoice. */
@Component("money")
public class MoneyPresentation {
    public String vnd(BigDecimal amount) {
        return amount == null ? "-" : InvoiceMoneyFormat.number(amount) + " đ";
    }
}
