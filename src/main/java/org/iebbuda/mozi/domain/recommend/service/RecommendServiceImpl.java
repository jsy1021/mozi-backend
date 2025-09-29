package org.iebbuda.mozi.domain.recommend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.iebbuda.mozi.domain.account.dto.AccountResponseDTO;
import org.iebbuda.mozi.domain.account.service.AccountService;
import org.iebbuda.mozi.domain.goal.service.GoalService;
import org.iebbuda.mozi.domain.goal.domain.GoalVO;
import org.iebbuda.mozi.domain.goal.dto.GoalDTO;
import org.iebbuda.mozi.domain.recommend.dto.FinancialRecommendProductDTO;
import org.iebbuda.mozi.domain.recommend.dto.GoalRecommendationDTO;
import org.iebbuda.mozi.domain.recommend.mapper.FinancialRecommendMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class RecommendServiceImpl implements RecommendService {

    private final FinancialRecommendMapper financialrecommendMapper;
    private final GoalService goalService;
    private final AccountService accountService;

    @Override
    public List<GoalRecommendationDTO> getRecommendationsByUser(int userId) {
        List<GoalDTO> goals = goalService.getGoalListByUserId(userId);
        List<GoalRecommendationDTO> goalRecommendations = new ArrayList<>();

        for (GoalDTO goal : goals) {
            // 파생값 계산
            double achievementRate = calculateAchievementRate(goal.getGoalId(), userId);
            BigDecimal targetAmount = goal.getTargetAmount();

            long monthsLeft = ChronoUnit.MONTHS.between(LocalDate.now(), goal.getGoalDate().toLocalDate());
            monthsLeft = Math.max(1, monthsLeft); // 최소 1개월 보정

            BigDecimal totalBalance = getTotalBalance(goal.getGoalId(), userId);
            BigDecimal remaining = targetAmount.subtract(totalBalance);
            if (remaining.signum() < 0) {
                remaining = BigDecimal.ZERO;
            }
            BigDecimal monthlyNeed = remaining
                    .divide(BigDecimal.valueOf(monthsLeft), RoundingMode.CEILING);

            log.info("목표 ID: {}, 달성률: {}, 남은 개월 수: {}, 월 납입 필요액: {}",
                    goal.getGoalId(), achievementRate, monthsLeft, monthlyNeed);

            // 스코어링(예금/적금)
            int depositScore = 0;
            int savingsScore = 0;

            // 달성률 기반 점수
            if (achievementRate < 30) {
                savingsScore += 30;
            } else if (achievementRate > 70) {
                depositScore += 30;
            } else {
                depositScore += 15;
                savingsScore += 15;
            }

            // ✅ 목표 금액 기반 추가 보정
            if (targetAmount.compareTo(new BigDecimal("1000000")) <= 0) {
                savingsScore += 10;
                log.info("[targetAmount adjust] 소액 목표 → 적금 점수 +10");
            }

            // 키워드 기반 세분화(+ 적금/예금 옵션)
            GoalVO.GoalKeyword keyword = goal.getKeyword();
            String rsrvType = null;             // 적금 전용: 정액(S) / 자유(F)
            String intrRateTypeSavings = null;  // 적금: 단리(S) / 복리(M)
            String intrRateTypeDeposit = null;  // 예금: 단리(S) / 복리(M)

            if (keyword != null) {
                switch (keyword) {
                    case MARRIAGE, HOME_PURCHASE -> {
                        depositScore += 20;
                        rsrvType = "S";
                        
                        // 적금: 1년 이상이면 복리
                        intrRateTypeSavings = (monthsLeft >= 12) ? "M" : "S";
                        
                        // 예금: 24개월 이상이면 복리 (예금 최대 36개월이므로)
                        intrRateTypeDeposit = (monthsLeft >= 24) ? "M" : "S";
                        
                        log.info("[keyword-adjust] {} -> 적금: {} ({}개월), 예금: {} ({}개월)", 
                                 keyword, intrRateTypeSavings, monthsLeft, intrRateTypeDeposit, monthsLeft);
                    }
                    case TRAVEL, HOBBY -> {
                        savingsScore += 20;
                        
                        // 적금: 기간별 이자 방식 최적화 (여행/취미는 대부분 단기)
                        if (monthsLeft <= 12) {
                            intrRateTypeSavings = "S";  // 1년 이하: 단리 (대부분의 경우)
                        } else {
                            intrRateTypeSavings = "M";  // 1년 초과: 복리 (드문 경우)
                        }
                        
                        // 예금: 기간별 이자 방식 최적화
                        if (monthsLeft <= 12) {
                            intrRateTypeDeposit = "S";  // 1년 이하: 단리
                        } else {
                            intrRateTypeDeposit = "M";  // 1년 초과: 복리
                        }
                        
                        log.info("[keyword-adjust] {} -> 단기 목표 주로 단리 적용, 적금: {} ({}개월), 예금: {} ({}개월)", 
                                 keyword, intrRateTypeSavings, monthsLeft, intrRateTypeDeposit, monthsLeft);
                    }
                    case EMPLOYMENT, EDUCATION_FUND -> {
                        savingsScore += 20;
                        rsrvType = "F";
                        
                        // 적금: 기간별 이자 방식 최적화
                        if (monthsLeft <= 12) {
                            intrRateTypeSavings = "S";  // 1년 이하: 단리
                        } else {
                            intrRateTypeSavings = "M";  // 1년 초과: 복리
                        }
                        
                        log.info("[keyword-adjust] {} -> 자유적립식 + 기간별 이자 방식, 적금: {} ({}개월)", 
                                 keyword, intrRateTypeSavings, monthsLeft);
                    }
                }
            }

            // 월 납입 필요액 기반 보정 (방향 수정)
            if (monthlyNeed.compareTo(new BigDecimal("1000000")) > 0) {
                // 매달 고액을 모아야 하는 경우 → 적금 선호
                savingsScore += 10;
            } else if (monthlyNeed.signum() == 0) {
                // 이미 목표 달성 → 여유 자금 단기 예치 선호
                depositScore += 10;
            } else {
                // 기본적으로 소액 납입은 적금 우세
                savingsScore += 5;
            }

            // 극단적 단기 목표 보정 (3개월 이하 → 예금 가산)
            if (monthsLeft <= 3) {
                depositScore += 5;
            }

            // 비율 기반 개수 계산 (총 4개 보장)
            int totalScore = Math.max(1, depositScore + savingsScore);
            int savingsCount = (int) Math.round(4 * ((double) savingsScore / totalScore));
            int depositCount = 4 - savingsCount;
            log.info("추천 수량 - 예금: {}, 적금: {} (depositScore={}, savingsScore={})",
                    depositCount, savingsCount, depositScore, savingsScore);

            // 추천 조회 + Fallback
            List<FinancialRecommendProductDTO> recommendedProducts = new ArrayList<>();
            Set<Integer> usedProductIds = new HashSet<>(); // 중복 방지용

            if (savingsCount > 0) {
                List<FinancialRecommendProductDTO> savings =
                        financialrecommendMapper.findTopSavingsByOption(monthsLeft, savingsCount, rsrvType, intrRateTypeSavings);

                // 중복 제거하면서 목표 개수만큼만 추출
                savings = savings.stream()
                        .filter(s -> usedProductIds.add(s.getProductId()))
                        .limit(savingsCount)
                        .collect(Collectors.toList());

                if (savings.size() < savingsCount) {
                    int remain = savingsCount - savings.size();
                    log.info("[fallback:savings] 1차 조건: rsrv_type={}, intr_rate_type={}, 부족: {}개", 
                             rsrvType, intrRateTypeSavings, remain);
                    
                    // 1단계: rsrv_type만 조건으로 조회 (intr_rate_type 조건 제거)
                    List<FinancialRecommendProductDTO> fallback1 = new ArrayList<>();
                    if (rsrvType != null) {
                        // rsrv_type 조건으로 조회 (새로운 메서드 사용)
                        fallback1 = financialrecommendMapper.findTopSavingsByRsrvType(monthsLeft, remain, rsrvType);
                        
                        // 중복 제거
                        fallback1 = fallback1.stream()
                                .filter(f -> usedProductIds.add(f.getProductId()))
                                .limit(remain)
                                .collect(Collectors.toList());
                    }
                    
                    if (fallback1.size() < remain) {
                        // 2단계: 일반 적금으로 보완
                        int remain2 = remain - fallback1.size();
                        List<FinancialRecommendProductDTO> fallback2 =
                                financialrecommendMapper.findTopSavingsProducts(monthsLeft, remain2 * 2);
                        
                        fallback2 = fallback2.stream()
                                .filter(f -> usedProductIds.add(f.getProductId()))
                                .limit(remain2)
                                .collect(Collectors.toList());
                        
                        fallback1.addAll(fallback2);
                        log.info("[fallback:savings] 2단계 보완 완료: 1단계 {}개 + 2단계 {}개", 
                                remain - remain2, remain2);
                    }
                    
                    savings.addAll(fallback1);
                    log.info("[fallback:savings] 단계별 보완 완료, 총 {}개 추가", fallback1.size());
                }
                recommendedProducts.addAll(savings);
            }

            if (depositCount > 0) {
                if (intrRateTypeDeposit != null) {
                    List<FinancialRecommendProductDTO> deposits =
                            financialrecommendMapper.findTopDepositByOption(monthsLeft, depositCount, intrRateTypeDeposit);

                    // 중복 제거하면서 목표 개수만큼만 추출
                    deposits = deposits.stream()
                            .filter(d -> usedProductIds.add(d.getProductId()))
                            .limit(depositCount)
                            .collect(Collectors.toList());

                    if (deposits.size() < depositCount) {
                        int remain = depositCount - deposits.size();
                        log.info("[fallback:deposit] 1차 조건으로 {}개 부족, 단계별 보완 시작", remain);
                        
                        // 1단계: intr_rate_type 조건만 제거하고 조회
                        List<FinancialRecommendProductDTO> fallback1 =
                                financialrecommendMapper.findTopDepositProducts(monthsLeft, remain * 2);
                        
                        fallback1 = fallback1.stream()
                                .filter(f -> usedProductIds.add(f.getProductId()))
                                .limit(remain)
                                .collect(Collectors.toList());
                        
                        if (fallback1.size() < remain) {
                            // 2단계: 일반 예금으로 보완
                            int remain2 = remain - fallback1.size();
                            List<FinancialRecommendProductDTO> fallback2 =
                                    financialrecommendMapper.findTopDepositProducts(monthsLeft, remain2 * 2);
                            
                            fallback2 = fallback2.stream()
                                    .filter(f -> usedProductIds.add(f.getProductId()))
                                    .limit(remain2)
                                    .collect(Collectors.toList());
                            
                            fallback1.addAll(fallback2);
                            log.info("[fallback:deposit] 2단계 보완 완료: 1단계 {}개 + 2단계 {}개", 
                                    remain - remain2, remain2);
                        }
                        
                        deposits.addAll(fallback1);
                        log.info("[fallback:deposit] 단계별 보완 완료, 총 {}개 추가", fallback1.size());
                    }
                    recommendedProducts.addAll(deposits);
                } else {
                    List<FinancialRecommendProductDTO> deposits =
                            financialrecommendMapper.findTopDepositProducts(monthsLeft, depositCount * 2); // 여유분으로 조회

                    // 중복 제거하면서 목표 개수만큼만 추출
                    deposits = deposits.stream()
                            .filter(d -> usedProductIds.add(d.getProductId()))
                            .limit(depositCount)
                            .collect(Collectors.toList());

                    recommendedProducts.addAll(deposits);
                }
            }

            // 최종적으로 정확히 4개가 되도록 보정
            if (recommendedProducts.size() < 4) {
                log.warn("추천 상품이 부족합니다. 현재: {}, 목표: 4개", recommendedProducts.size());
            } else if (recommendedProducts.size() > 4) {
                recommendedProducts = recommendedProducts.subList(0, 4);
                log.info("추천 상품을 4개로 제한했습니다.");
            }

            log.info("최종 추천 상품 수: {}개 (목표: 4개)", recommendedProducts.size());

            goalRecommendations.add(new GoalRecommendationDTO(
                    goal.getGoalId(),
                    goal.getGoalName(),
                    recommendedProducts
            ));
        }

        return goalRecommendations;
    }

    private double calculateAchievementRate(int goalId, int userId) {
        BigDecimal totalBalance = getTotalBalance(goalId, userId);
        BigDecimal targetAmount = goalService.getGoal(goalId).getTargetAmount();
        double rate = goalService.calculateAchievementRate(totalBalance, targetAmount);
        log.info("총 잔액: {}, 목표 금액: {}, 계산된 달성률: {}", totalBalance, targetAmount, rate);
        return rate;
    }

    private BigDecimal getTotalBalance(int goalId, int userId) {
        @SuppressWarnings("unchecked")
        List<AccountResponseDTO> accounts = (List<AccountResponseDTO>) accountService
                .getAccountsByGoal(goalId, userId)
                .get("accountList");

        return accounts.stream()
                .map(a -> BigDecimal.valueOf(a.getBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
