package com.medclinic;

import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.GlobalExceptionHandler;
import com.medclinic.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsNotFoundToErrorPage() {
        ConcurrentModel model = new ConcurrentModel();

        String view = handler.handleNotFound(new ResourceNotFoundException("Không thấy dữ liệu"), model);

        assertEquals("error/error", view);
        assertEquals("404 - Không tìm thấy dữ liệu", model.getAttribute("errorCode"));
        assertEquals("Không thấy dữ liệu", model.getAttribute("errorMessage"));
    }

    @Test
    void redirectsBusinessErrorBackToReferer() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Referer", "/patient/booking");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = handler.handleBusinessLogic(
                new BusinessLogicException("Khung giờ đã được đặt"), request, redirect);

        assertEquals("redirect:/patient/booking", view);
        assertEquals("Khung giờ đã được đặt", redirect.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void redirectsBusinessErrorHomeWithoutReferer() {
        String view = handler.handleBusinessLogic(
                new BusinessLogicException("Lỗi nghiệp vụ"),
                new MockHttpServletRequest(), new RedirectAttributesModelMap());

        assertEquals("redirect:/", view);
    }

    @Test
    void mapsUnexpectedExceptionTo500Page() {
        ConcurrentModel model = new ConcurrentModel();

        String view = handler.handleGeneralException(new IllegalStateException("Boom"), model);

        assertEquals("error/error", view);
        assertEquals("500 - Lỗi Hệ Thống", model.getAttribute("errorCode"));
        assertEquals("Boom", model.getAttribute("errorMessage"));
    }
}
