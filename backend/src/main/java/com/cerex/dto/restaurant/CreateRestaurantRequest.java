package com.cerex.dto.restaurant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for creating a new restaurant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRestaurantRequest {

    @NotBlank(message = "Le nom du restaurant est obligatoire")
    @Size(max = 200)
    private String name;

    @Size(max = 2000)
    private String description;

    private String cuisineType;
    private Set<String> cuisineTags;

    // Contact
    private String phone;

    @Email
    private String email;

    private String website;

    // Location
    @NotBlank(message = "L'adresse est obligatoire")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "La ville est obligatoire")
    private String city;

    private String stateProvince;
    private String postalCode;
    private UUID countryId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double deliveryRadiusKm;
    private String timezone;

    // Media
    private String logoUrl;
    private String coverImageUrl;

    // Operating
    private Map<String, OperatingHoursInput> operatingHours;
    private BigDecimal minimumOrderAmount;
    private Integer averagePreparationTimeMin;

    // Features
    private Boolean supportsTakeaway;
    private Boolean supportsDelivery;
    private Boolean supportsDineIn;
    private Boolean acceptsReservations;
    private Boolean offersCatering;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatingHoursInput {
        private String openTime;
        private String closeTime;
        private boolean closed;
    }
}
