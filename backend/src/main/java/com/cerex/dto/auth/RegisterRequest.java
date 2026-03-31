package com.cerex.dto.auth;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 320)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100)
    private String lastName;

    /** Optional — derived from firstName + lastName if not provided. */
    @Size(max = 100)
    private String displayName;

    @Size(max = 10)
    private String preferredLanguage;

    @Size(max = 3)
    private String preferredCurrency;

    private boolean gdprConsent;
    private boolean marketingConsent;

    /** Returns displayName, falling back to "firstName lastName". */
    public String getResolvedDisplayName() {
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }
        return (firstName + " " + lastName).trim();
    }
}
