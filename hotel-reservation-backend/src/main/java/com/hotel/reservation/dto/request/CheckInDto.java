package com.hotel.reservation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Check-in process request data - HU009")
public class CheckInDto {

    @Schema(description = "Reservation ID or confirmation number", 
            example = "CONF73829456", required = true)
    @NotBlank(message = "Reservation ID is required")
    private String reservationId;

    @Schema(description = "Guest document type", example = "PASSPORT", required = true,
            allowableValues = {"PASSPORT", "NATIONAL_ID", "DRIVER_LICENSE", "OTHER"})
    @NotBlank(message = "Document type is required")
    private String documentType;

    @Schema(description = "Guest document number", example = "AB123456789", required = true)
    @NotBlank(message = "Document number is required")
    @Size(min = 5, max = 20, message = "Document number must be between 5 and 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Document number can only contain letters and numbers")
    private String documentNumber;

    @Schema(description = "Document issuing country", example = "CO", required = false)
    @Size(min = 2, max = 3, message = "Country code must be 2 or 3 characters")
    private String documentCountry;

    @Schema(description = "Actual number of adults checking in", example = "2", required = false)
    private Integer actualAdults;

    @Schema(description = "Actual number of children checking in", example = "1", required = false)
    private Integer actualChildren;

    @Schema(description = "Room assignment preference", example = "201", required = false)
    private String preferredRoomNumber;

    @Schema(description = "Estimated arrival time", example = "2025-11-25T15:30:00", required = false)
    private LocalDateTime estimatedArrival;

    @Schema(description = "Special requests during check-in", 
            example = "Late checkout requested, extra towels", required = false)
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @Schema(description = "Vehicle information for parking", 
            example = "ABC-123 - Toyota Corolla", required = false)
    @Size(max = 100, message = "Vehicle info must not exceed 100 characters")
    private String vehicleInfo;

    @Schema(description = "Emergency contact name", example = "María García", required = false)
    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    private String emergencyContactName;

    @Schema(description = "Emergency contact phone", example = "+57 300 123 9876", required = false)
    @Pattern(regexp = "^[\\+]?[1-9][\\d\\s\\-\\(\\)]{8,15}$", 
             message = "Emergency contact phone format is invalid")
    private String emergencyContactPhone;

    @Schema(description = "Guest signature confirmation", example = "true", required = false)
    @Builder.Default
    private Boolean signatureConfirmed = false;

    @Schema(description = "Terms and conditions accepted", example = "true", required = false)
    @Builder.Default
    private Boolean termsAccepted = false;

    @Schema(description = "Damage deposit collected", example = "true", required = false)
    @Builder.Default
    private Boolean depositCollected = false;

    @Schema(description = "Key cards issued count", example = "2", required = false)
    @Builder.Default
    private Integer keyCardsIssued = 1;

    @Schema(description = "Additional services requested", 
            example = "Breakfast,WiFi,Parking", required = false)
    private String additionalServices;

    /**
     * Check if document information is complete
     */
    public boolean hasCompleteDocumentInfo() {
        return documentType != null && documentNumber != null && 
               !documentType.trim().isEmpty() && !documentNumber.trim().isEmpty();
    }

    /**
     * Check if emergency contact is provided
     */
    public boolean hasEmergencyContact() {
        return emergencyContactName != null && emergencyContactPhone != null &&
               !emergencyContactName.trim().isEmpty() && !emergencyContactPhone.trim().isEmpty();
    }

    /**
     * Check if all mandatory check-in requirements are met
     */
    public boolean isMandatoryInfoComplete() {
        return hasCompleteDocumentInfo() && 
               (signatureConfirmed != null && signatureConfirmed) &&
               (termsAccepted != null && termsAccepted);
    }
}