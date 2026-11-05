package com.ecommerce.services;

import com.ecommerce.models.DTO.SellerApplicationRequest;
import com.ecommerce.models.DTO.SellerProfileResponse;
import com.ecommerce.exceptions.ConflictException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.entities.SellerProfile;
import com.ecommerce.models.enums.SellerStatus;
import com.ecommerce.repositories.SellerProfileRepository;
import com.ecommerce.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public SellerProfileResponse apply(Long userId, SellerApplicationRequest req) {
        SellerProfile profile = sellerProfileRepository.findByUserId(userId).orElse(null);

        if (profile != null && profile.getStatus() != SellerStatus.REJECTED) {
            throw new ConflictException("You already have a seller application");
        }

        if (profile == null) {
            profile = new SellerProfile();
            profile.setUser(userRepository.getReferenceById(userId));
        }

        fill(profile, req);
        profile.setStatus(SellerStatus.PENDING);
        profile.setRejectionReason(null);

        return SellerProfileResponse.from(sellerProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public SellerProfileResponse getMine(Long userId) {
        return SellerProfileResponse.from(findByUserOrThrow(userId));
    }

    @Transactional
    public SellerProfileResponse updateMine(Long userId, SellerApplicationRequest req) {
        SellerProfile profile = findByUserOrThrow(userId);

        if (profile.getStatus() == SellerStatus.REJECTED) {
            throw new ConflictException("Your application was rejected, apply again");
        }

        if (profile.getStatus() == SellerStatus.APPROVED) {
            profile.setBusinessPhone(req.businessPhone());
            profile.setBusinessAddress(req.businessAddress());
        } else {
            fill(profile, req);
        }

        return SellerProfileResponse.from(profile);
    }

    private SellerProfile findByUserOrThrow(Long userId) {
        return sellerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller application not found"));
    }

    private void fill(SellerProfile profile, SellerApplicationRequest req) {
        profile.setNipt(req.nipt().trim());
        profile.setBusinessPhone(req.businessPhone());
        profile.setBusinessAddress(req.businessAddress());
    }
}
