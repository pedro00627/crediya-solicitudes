package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.ApplicationReviewDTO;
import co.com.pragma.model.application.Application;

public interface ApplicationMapper {
    ApplicationReviewDTO toReviewDTO(Application application, String statusName, String loanTypeName);
}