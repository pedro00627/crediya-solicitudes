package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.ApplicationReviewDTO;
import co.com.pragma.model.application.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApplicationMapperDelegate {
    @Mapping(source = "statusName", target = "applicationStatus")
    @Mapping(source = "loanTypeName", target = "loanType")
    ApplicationReviewDTO toReviewDTO(Application application, String statusName, String loanTypeName);
}