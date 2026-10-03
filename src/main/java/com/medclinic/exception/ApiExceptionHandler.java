package com.medclinic.exception;

import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> notFound(Exception ex) { return error(404, "NOT_FOUND", "Không tìm thấy dữ liệu."); }

    @ExceptionHandler({BusinessLogicException.class, MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            DateTimeParseException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<?> invalid(Exception ex) { return error(400, "INVALID_REQUEST", "Dữ liệu yêu cầu không hợp lệ."); }

    @ExceptionHandler({AccessDeniedException.class, SecurityException.class})
    public ResponseEntity<?> forbidden(Exception ex) { return error(403, "FORBIDDEN", "Bạn không có quyền thực hiện yêu cầu."); }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected(Exception ex) {
        org.slf4j.LoggerFactory.getLogger(ApiExceptionHandler.class).error("Unhandled API failure", ex);
        return error(500, "INTERNAL_ERROR", "Không thể xử lý yêu cầu. Vui lòng thử lại sau.");
    }

    private ResponseEntity<?> error(int status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("code", code, "message", message));
    }
}
