package co.com.pragma.model.application;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApplicationStatus {
    ;
    public static final Integer PENDING_REVIEW_ID = 1;

    public static final String PENDING = "PENDIENTE";
    public static final String PENDING_REVIEW = "EN_REVISION";
    public static final String REJECTED = "RECHAZADA";
    public static final String MANUAL_REVIEW = "Revision manual";
    public static final String APPROVED = "APROBADA";
}
