package com.medclinic.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserFacingErrorController implements ErrorController {

    @RequestMapping({"/error", "/error/403"})
    public Object error(HttpServletRequest request) {
        Object rawStatus = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = request.getRequestURI().endsWith("/403") ? 403 :
                rawStatus instanceof Integer code ? code : 500;
        String title = switch (status) {
            case 400 -> "400 - Dữ liệu không hợp lệ";
            case 401 -> "401 - Phiên đăng nhập đã hết hạn";
            case 403 -> "403 - Không có quyền truy cập";
            case 404 -> "404 - Không tìm thấy trang";
            default -> "500 - Không thể xử lý yêu cầu";
        };
        String message = switch (status) {
            case 400 -> "Vui lòng kiểm tra dữ liệu đã nhập và thử lại.";
            case 401 -> "Hãy đăng nhập lại để tiếp tục công việc.";
            case 403 -> "Tài khoản hiện tại không có quyền mở trang này. Hãy quay lại khu vực làm việc của bạn hoặc đăng nhập bằng tài khoản phù hợp.";
            case 404 -> "Trang này không tồn tại hoặc đã được chuyển. Hãy quay lại khu vực làm việc hoặc trang chủ.";
            default -> "Yêu cầu chưa được hoàn tất. Vui lòng thử lại sau hoặc liên hệ hỗ trợ MedClinic.";
        };
        Object original = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String uri = original instanceof String path ? path : request.getRequestURI();
        String accept = request.getHeader("Accept");
        if (uri.startsWith(request.getContextPath() + "/api/") || (accept != null && accept.contains("json"))) {
            return ResponseEntity.status(status).body(Map.of("code", "REQUEST_FAILED", "message", message));
        }
        ModelAndView view = new ModelAndView("error/error");
        view.setStatus(HttpStatus.valueOf(status));
        view.addObject("errorCode", title);
        view.addObject("errorMessage", message);
        return view;
    }
}
