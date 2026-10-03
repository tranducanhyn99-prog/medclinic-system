package com.medclinic.util;

import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;

public final class PublicErrorMessages {
    private PublicErrorMessages() { }

    public static String forException(Exception exception) {
        if (exception instanceof BusinessLogicException || exception instanceof ResourceNotFoundException) {
            return exception.getMessage();
        }
        org.slf4j.LoggerFactory.getLogger(PublicErrorMessages.class).error("Request failed", exception);
        return "Không thể xử lý yêu cầu. Vui lòng thử lại sau.";
    }
}
