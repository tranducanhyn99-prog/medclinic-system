package com.medclinic.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import java.net.URI;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleMissingPage(Model model) {
        model.addAttribute("errorCode", "404 - Không tìm thấy trang");
        model.addAttribute("errorMessage", "Trang này không tồn tại hoặc đã được chuyển. Hãy quay lại trang chủ hoặc khu vực làm việc của bạn.");
        return "error/error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorCode", "404 - Không tìm thấy dữ liệu");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(BusinessLogicException.class)
    public String handleBusinessLogic(BusinessLogicException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + safeReferer(referer, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception ex, Model model) {
        LoggerFactory.getLogger(GlobalExceptionHandler.class).error("Unhandled request failure", ex);
        model.addAttribute("errorCode", "500 - Lỗi Hệ Thống");
        model.addAttribute("errorMessage", "Không thể xử lý yêu cầu. Vui lòng thử lại sau.");
        return "error/error";
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(Exception ex, Model model) {
        model.addAttribute("errorCode", "403 - Không có quyền truy cập");
        model.addAttribute("errorMessage", "Bạn không có quyền thực hiện yêu cầu.");
        return "error/error";
    }

    @ExceptionHandler({org.springframework.validation.BindException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidInput(Exception ex, Model model) {
        model.addAttribute("errorCode", "400 - Dữ liệu không hợp lệ");
        model.addAttribute("errorMessage", "Vui lòng kiểm tra dữ liệu đã nhập.");
        return "error/error";
    }

    private String safeReferer(String referer, HttpServletRequest request) {
        if (referer == null || referer.contains("\\") || referer.contains("\r") || referer.contains("\n")) return "/";
        try {
            URI uri = URI.create(referer);
            if (uri.getRawAuthority() != null) {
                int port = uri.getPort() == -1 ? ("https".equals(uri.getScheme()) ? 443 : 80) : uri.getPort();
                if (!request.getScheme().equalsIgnoreCase(uri.getScheme())
                        || !request.getServerName().equalsIgnoreCase(uri.getHost())
                        || request.getServerPort() != port || uri.getUserInfo() != null) return "/";
            } else if (uri.isAbsolute() || !referer.startsWith("/")) return "/";
            String path = uri.getRawPath();
            if (path == null || !path.startsWith("/") || path.startsWith("//")
                    || path.toLowerCase(java.util.Locale.ROOT).matches(".*%(?:2f|5c|0d|0a).*")) return "/";
            return path + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        } catch (IllegalArgumentException exception) { return "/"; }
    }
}

