package co.com.pragma.model.application;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApplicationStatus {
    public static final Integer PENDING_REVIEW_ID = 1;

    public static final String PENDING_REVIEW = "Pendiente de revision";
    public static final String REJECTED = "Rechazadas";
    public static final String MANUAL_REVIEW = "Revision manual";
    public static final String APPROVED = "Aprobada";
}
