package com.ecommerce.services;

import com.ecommerce.models.DTO.StoreRequest;
import com.ecommerce.exceptions.ConflictException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.DTO.StoreResponse;
import com.ecommerce.models.entities.SellerProfile;
import com.ecommerce.models.entities.Store;
import com.ecommerce.models.enums.SellerStatus;
import com.ecommerce.repositories.SellerProfileRepository;
import com.ecommerce.repositories.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final SellerProfileRepository sellerProfileRepository;

    @Transactional
    public StoreResponse create(Long userId, StoreRequest req) {
        SellerProfile seller = approvedSeller(userId);

        if (storeRepository.existsBySellerId(seller.getId())) {
            throw new ConflictException("You already have a store");
        }

        Store store = new Store();
        store.setSeller(seller);
        fill(store, req);

        return StoreResponse.from(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public StoreResponse getMine(Long userId) {
        return StoreResponse.from(findMine(userId));
    }

    @Transactional
    public StoreResponse updateMine(Long userId, StoreRequest req) {
        Store store = findMine(userId);
        fill(store, req);
        return StoreResponse.from(store);
    }

    @Transactional(readOnly = true)
    public StoreResponse getById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        return StoreResponse.from(store);
    }

    private Store findMine(Long userId) {
        SellerProfile seller = approvedSeller(userId);
        return storeRepository.findBySellerId(seller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You have no store yet"));
    }

    private SellerProfile approvedSeller(Long userId) {
        SellerProfile seller = sellerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));

        if (seller.getStatus() != SellerStatus.APPROVED) {
            throw new AccessDeniedException("Seller is not approved");
        }
        return seller;
    }

    private void fill(Store store, StoreRequest req) {
        store.setName(req.name().trim());
        store.setDescription(req.description());
    }
}
