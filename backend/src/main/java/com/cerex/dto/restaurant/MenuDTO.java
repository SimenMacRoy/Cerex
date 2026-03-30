package com.cerex.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO for restaurant menu responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDTO {

    private UUID id;
    private String name;
    private String description;
    private String menuType;
    private Boolean isActive;
    private String availableFrom;
    private String availableUntil;
    private List<MenuItemDTO> items;
}
