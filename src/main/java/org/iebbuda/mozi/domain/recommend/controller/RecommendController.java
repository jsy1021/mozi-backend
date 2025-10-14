package org.iebbuda.mozi.domain.recommend.controller;

import lombok.RequiredArgsConstructor;
import org.iebbuda.mozi.domain.recommend.dto.GoalRecommendationDTO;
import org.iebbuda.mozi.domain.recommend.service.RecommendService;
import org.iebbuda.mozi.domain.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend/finance")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping
    public ResponseEntity<List<GoalRecommendationDTO>> getRecommendations(@AuthenticationPrincipal CustomUser user)
    {
        int userId=user.getUser().getUserId();
        return ResponseEntity.ok(recommendService.getRecommendationsByUser(userId));
    }
}
