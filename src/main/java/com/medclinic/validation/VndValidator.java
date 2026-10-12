package com.medclinic.validation;

import com.medclinic.util.VndMoney;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class VndValidator implements ConstraintValidator<ValidVnd, BigDecimal> {
    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        return value == null || VndMoney.isValid(value);
    }
}
