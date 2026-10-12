package com.medclinic;

import com.medclinic.dto.AdminDoctorCreateDto;
import com.medclinic.dto.AdminMedicineDto;
import com.medclinic.dto.UserRegisterDto;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.util.PhoneNumbers;
import com.medclinic.util.VndMoney;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PhoneAndVndValidationTest {
    @Test
    void bothFormsRejectPipeAndAcceptEveryAllowedPhonePrefix() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (String prefix : new String[]{"03", "05", "07", "08", "09"}) {
                String phone = prefix + "12345678";
                assertTrue(validator.validateValue(UserRegisterDto.class, "phone", phone).isEmpty());
                assertTrue(validator.validateValue(AdminDoctorCreateDto.class, "phone", phone).isEmpty());
                assertEquals(phone, PhoneNumbers.normalize(" " + phone + " ", true));
            }
            for (String phone : new String[]{"0|12345678", "09123abcd8", "091234567", "09123456789", "0212345678"}) {
                assertFalse(validator.validateValue(UserRegisterDto.class, "phone", phone).isEmpty());
                assertFalse(validator.validateValue(AdminDoctorCreateDto.class, "phone", phone).isEmpty());
                assertThrows(BusinessLogicException.class, () -> PhoneNumbers.normalize(phone, true));
            }
        }
        assertNull(PhoneNumbers.normalize(" ", false));
        var patient = new UserRegisterDto(); patient.setPhone(" 0912345678 ");
        var doctor = new AdminDoctorCreateDto(); doctor.setPhone(" 0912345678 ");
        assertEquals("0912345678", patient.getPhone());
        assertEquals(patient.getPhone(), doctor.getPhone());
        assertNull(PhoneNumbers.normalize(null, false));
        assertThrows(BusinessLogicException.class, () -> PhoneNumbers.normalize(" ", true));
    }

    @Test
    void moneyAcceptsIntegralTrailingZerosButRejectsFractionsAndSchemaOverflow() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (String value : new String[]{"0", "1000.00", "9999999999.00"}) {
                BigDecimal amount = new BigDecimal(value);
                assertTrue(VndMoney.isValid(amount));
                assertTrue(validator.validateValue(AdminMedicineDto.class, "unitPrice", amount).isEmpty());
                assertTrue(validator.validateValue(AdminDoctorCreateDto.class, "consultationFee", amount).isEmpty());
            }
            for (String value : new String[]{"-1", "1000.50", "10000000000", "0.01"}) {
                BigDecimal amount = new BigDecimal(value);
                assertFalse(validator.validateValue(AdminMedicineDto.class, "unitPrice", amount).isEmpty());
                assertFalse(validator.validateValue(AdminDoctorCreateDto.class, "consultationFee", amount).isEmpty());
                assertThrows(BusinessLogicException.class, () -> VndMoney.requireValid(amount, "Giá"));
            }
            assertFalse(validator.validateValue(AdminMedicineDto.class, "unitPrice", null).isEmpty());
            assertFalse(validator.validateValue(AdminDoctorCreateDto.class, "consultationFee", null).isEmpty());
        }
    }
}
