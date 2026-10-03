package com.medclinic;

import com.medclinic.controller.AuthController;
import com.medclinic.util.MoneyPresentation;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ConcurrentModel;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class UiPresentationTest {
    @Test
    void monetaryPresentationPreservesNullZeroLargeAmountsAndFractionalValue() {
        MoneyPresentation money = new MoneyPresentation();
        assertEquals("-", money.vnd(null));
        assertEquals("0 đ", money.vnd(BigDecimal.ZERO));
        assertEquals("353 000 đ", money.vnd(new BigDecimal("353000.00")));
        assertEquals("999 999 999 999 999 đ", money.vnd(new BigDecimal("999999999999999")));
        assertEquals("1 000,5 đ", money.vnd(new BigDecimal("1000.50")));
    }

    @Test
    void demoLoginShortcutsAreLimitedToDevelopmentAndNeverProduction() {
        assertTrue(demoEnabled("dev"));
        assertFalse(demoEnabled("sqlserver"));
        assertFalse(demoEnabled("prod"));
        assertFalse(demoEnabled("dev", "prod"));
    }

    private boolean demoEnabled(String... profiles) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profiles);
        AuthController controller = new AuthController(null, null, null, null, null, null, environment);
        ConcurrentModel model = new ConcurrentModel();
        controller.loginPage(null, null, null, new MockHttpServletRequest(), model);
        return (Boolean) model.getAttribute("demoLoginEnabled");
    }
}
