package com.ecommerce.services;

import com.ecommerce.models.DTO.responses.SellerProfileResponse;
import com.ecommerce.exceptions.ConflictException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.entities.SellerProfile;
import com.ecommerce.models.enums.SellerStatus;
import com.ecommerce.models.enums.UserRole;
import com.ecommerce.repositories.SellerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminSellerService {

    private final SellerProfileRepository sellerProfileRepository;

    @Transactional(readOnly = true)
    public Page<SellerProfileResponse> list(SellerStatus status, Pageable pageable) {
        Page<SellerProfile> page = (status == null)
                ? sellerProfileRepository.findAll(pageable)
                : sellerProfileRepository.findByStatus(status, pageable);
        return page.map(SellerProfileResponse::from);
    }

    @Transactional
    public SellerProfileResponse approve(Long id) {
        SellerProfile profile = findPendingOrThrow(id);
        profile.setStatus(SellerStatus.APPROVED);
        profile.setRejectionReason(null);
        profile.getUser().setRole(UserRole.SELLER);
        return SellerProfileResponse.from(profile);
    }

    @Transactional
    public SellerProfileResponse reject(Long id, String reason) {
        SellerProfile profile = findPendingOrThrow(id);
        profile.setStatus(SellerStatus.REJECTED);
        profile.setRejectionReason(reason.trim());
        return SellerProfileResponse.from(profile);
    }

    @Transactional
    public SellerProfileResponse suspend(Long id) {
        SellerProfile profile = sellerProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (profile.getStatus() != SellerStatus.APPROVED) {
            throw new ConflictException("Only approved sellers can be suspended");
        }

        profile.setStatus(SellerStatus.SUSPENDED);

        return SellerProfileResponse.from(profile);
    }

    @Transactional
    public SellerProfileResponse activate(Long id) {
        SellerProfile profile = sellerProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (profile.getStatus() != SellerStatus.SUSPENDED) {
            throw new ConflictException("Only suspended sellers can be activated");
        }

        profile.setStatus(SellerStatus.APPROVED);

        return SellerProfileResponse.from(profile);
    }

    private SellerProfile findPendingOrThrow(Long id) {
        SellerProfile profile = sellerProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller application not found"));
        if (profile.getStatus() != SellerStatus.PENDING) {
            throw new ConflictException("Application is not pending");
        }
        return profile;
    }
}
