package org.iebbuda.mozi.domain.scrap.controller;

import lombok.RequiredArgsConstructor;
import org.iebbuda.mozi.domain.product.dto.DepositResponse;
import org.iebbuda.mozi.domain.product.dto.SavingResponse;
import org.iebbuda.mozi.domain.scrap.dto.DepositScrapDto;
import org.iebbuda.mozi.domain.scrap.dto.FinancialScrapDto;
import org.iebbuda.mozi.domain.scrap.service.DepositScrapService;
import org.iebbuda.mozi.domain.scrap.service.FinancialScrapService;
import org.iebbuda.mozi.domain.scrap.service.SavingScrapService;
import org.iebbuda.mozi.domain.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scrap/finance")
@RequiredArgsConstructor
public class FinancialScrapController {

    private final FinancialScrapService financialScrapService;

    // 스크랩 목록 조회
    @GetMapping
    public ResponseEntity<List<FinancialScrapDto>> getUserScraps(@AuthenticationPrincipal CustomUser user) {
        long userId= (long) user.getUser().getUserId();
        System.out.println("userId체크: "+userId);
        return ResponseEntity.ok(financialScrapService.getUserScraps(userId));
    }

    // 스크랩 추가
    @PostMapping
    public ResponseEntity<Void> addScrap(@AuthenticationPrincipal CustomUser user,
                         @RequestParam String productType,
                         @RequestParam Long productId) {
        long userId= (long) user.getUser().getUserId();
        financialScrapService.addScrap(userId, productType, productId);
        return ResponseEntity.ok().build();
    }

    // 스크랩 삭제
    @DeleteMapping
    public ResponseEntity<Void> removeScrap(@AuthenticationPrincipal CustomUser user,
                            @RequestParam String productType,
                            @RequestParam Long productId) {
        long userId= (long) user.getUser().getUserId();
        financialScrapService.removeScrap(userId, productType, productId);
        return ResponseEntity.ok().build();
    }
}
