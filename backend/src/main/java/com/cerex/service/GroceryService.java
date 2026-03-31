package com.cerex.service;

import com.cerex.domain.Grocery;
import com.cerex.domain.Grocery.GroceryStatus;
import com.cerex.domain.GroceryProduct;
import com.cerex.dto.restaurant.GroceryDTO;
import com.cerex.exception.ResourceNotFoundException;
import com.cerex.repository.GroceryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for grocery store management.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class GroceryService {

    private final GroceryRepository groceryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ─────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────

    public GroceryDTO getGroceryBySlug(String slug) {
        Grocery grocery = groceryRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Grocery", "slug", slug));
        return toDTO(grocery);
    }

    public GroceryDTO getGroceryById(UUID id) {
        Grocery grocery = groceryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Grocery", "id", id));
        return toDTO(grocery);
    }

    public Page<GroceryDTO> listActiveGroceries(Pageable pageable) {
        return groceryRepository.findActiveGroceries(pageable).map(this::toDTO);
    }

    public Page<GroceryDTO> searchGroceries(String query, Pageable pageable) {
        return groceryRepository.searchGroceries(query, pageable).map(this::toDTO);
    }

    public Page<GroceryDTO> findNearby(BigDecimal lat, BigDecimal lng, double radiusKm, Pageable pageable) {
        return groceryRepository.findNearbyGroceries(lat, lng, radiusKm, pageable).map(this::toDTO);
    }

    public Page<GroceryDTO> findByCity(String city, Pageable pageable) {
        return groceryRepository.findByCity(city, pageable).map(this::toDTO);
    }

    public Page<GroceryDTO> findOrganic(Pageable pageable) {
        return groceryRepository.findOrganicGroceries(pageable).map(this::toDTO);
    }

    public Page<GroceryDTO> getMyGroceries(UUID ownerId, Pageable pageable) {
        return groceryRepository.findByOwnerId(ownerId, pageable).map(this::toDTO);
    }

    // ─────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────

    @Transactional
    public GroceryDTO createGrocery(UUID ownerId, GroceryDTO request) {
        Grocery grocery = Grocery.builder()
            .ownerId(ownerId)
            .name(request.getName())
            .description(request.getDescription())
            .slug(request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"))
            .groceryType(request.getGroceryType() != null
                ? Grocery.GroceryType.valueOf(request.getGroceryType())
                : Grocery.GroceryType.GENERAL)
            .specialtyTags(request.getSpecialtyTags())
            .phone(request.getPhone())
            .email(request.getEmail())
            .website(request.getWebsite())
            .addressLine1(request.getAddressLine1())
            .city(request.getCity())
            .stateProvince(request.getStateProvince())
            .postalCode(request.getPostalCode())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .deliveryRadiusKm(request.getDeliveryRadiusKm() != null ? request.getDeliveryRadiusKm() : 15.0)
            .logoUrl(request.getLogoUrl())
            .coverImageUrl(request.getCoverImageUrl())
            .supportsDelivery(request.getSupportsDelivery() != null ? request.getSupportsDelivery() : true)
            .supportsPickup(request.getSupportsPickup() != null ? request.getSupportsPickup() : true)
            .minimumOrderAmount(request.getMinimumOrderAmount() != null ? request.getMinimumOrderAmount() : BigDecimal.ZERO)
            .isOrganicCertified(request.getIsOrganicCertified() != null ? request.getIsOrganicCertified() : false)
            .build();

        grocery = groceryRepository.save(grocery);
        log.info("Grocery created: {} [{}]", grocery.getName(), grocery.getId());

        kafkaTemplate.send("cerex.grocery.created", grocery.getId().toString(),
            Map.of("groceryId", grocery.getId(), "name", grocery.getName()));

        return toDTO(grocery);
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN: Verification
    // ─────────────────────────────────────────────────────────

    @Transactional
    public GroceryDTO verifyGrocery(UUID groceryId) {
        Grocery grocery = groceryRepository.findById(groceryId)
            .orElseThrow(() -> new ResourceNotFoundException("Grocery", "id", groceryId));

        grocery.verify();
        grocery = groceryRepository.save(grocery);
        log.info("Grocery verified: [{}]", grocery.getId());
        return toDTO(grocery);
    }

    // ─────────────────────────────────────────────────────────
    // DTO Mapping
    // ─────────────────────────────────────────────────────────

    private GroceryDTO toDTO(Grocery g) {
        return GroceryDTO.builder()
            .id(g.getId())
            .name(g.getName())
            .description(g.getDescription())
            .slug(g.getSlug())
            .status(g.getStatus().name())
            .groceryType(g.getGroceryType().name())
            .specialtyTags(g.getSpecialtyTags())
            .phone(g.getPhone())
            .email(g.getEmail())
            .website(g.getWebsite())
            .addressLine1(g.getAddressLine1())
            .city(g.getCity())
            .stateProvince(g.getStateProvince())
            .postalCode(g.getPostalCode())
            .latitude(g.getLatitude())
            .longitude(g.getLongitude())
            .deliveryRadiusKm(g.getDeliveryRadiusKm())
            .logoUrl(g.getLogoUrl())
            .coverImageUrl(g.getCoverImageUrl())
            .averageRating(g.getAverageRating())
            .totalReviews(g.getTotalReviews())
            .supportsDelivery(g.getSupportsDelivery())
            .supportsPickup(g.getSupportsPickup())
            .minimumOrderAmount(g.getMinimumOrderAmount())
            .isOrganicCertified(g.getIsOrganicCertified())
            .isVerified(g.getIsVerified())
            .ecoScore(g.getEcoScore())
            .productCount(g.getProducts() != null ? g.getProducts().size() : 0)
            .featuredProducts(g.getProducts() != null
                ? g.getProducts().stream()
                    .filter(p -> p.getIsInStock())
                    .limit(6)
                    .map(this::toProductDTO)
                    .toList()
                : null)
            .build();
    }

    private GroceryDTO.GroceryProductDTO toProductDTO(GroceryProduct p) {
        return GroceryDTO.GroceryProductDTO.builder()
            .id(p.getId())
            .name(p.getName())
            .description(p.getDescription())
            .category(p.getCategory())
            .brand(p.getBrand())
            .price(p.getPrice())
            .pricePerUnit(p.getPricePerUnit())
            .unit(p.getUnit())
            .imageUrl(p.getImageUrl())
            .isInStock(p.getIsInStock())
            .isOrganic(p.getIsOrganic())
            .isLocal(p.getIsLocal())
            .ecoScore(p.getEcoScore())
            .nutriScore(p.getNutriScore())
            .allergens(p.getAllergens())
            .build();
    }
}
