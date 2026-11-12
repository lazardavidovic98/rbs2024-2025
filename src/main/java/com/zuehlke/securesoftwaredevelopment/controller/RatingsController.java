package com.zuehlke.securesoftwaredevelopment.controller;

import com.zuehlke.securesoftwaredevelopment.domain.Rating;
import com.zuehlke.securesoftwaredevelopment.domain.User;
import com.zuehlke.securesoftwaredevelopment.repository.RatingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class RatingsController {

    private final RatingRepository ratingRepository;

    public RatingsController(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @PostMapping(value = "/ratings", consumes = "application/json")
    @PreAuthorize("hasAuthority('RATE_BOOK')")
    public ResponseEntity<Void> createOrUpdateRating(@RequestBody Rating rating, @AuthenticationPrincipal User user) {
        // The rating always belongs to the signed-in user, whatever userId the client sends
        rating.setUserId(user.getId());
        ratingRepository.createOrUpdate(rating);
        return ResponseEntity.noContent().build();
    }
}
