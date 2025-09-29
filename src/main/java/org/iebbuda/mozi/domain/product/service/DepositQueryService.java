package org.iebbuda.mozi.domain.product.service;

import lombok.RequiredArgsConstructor;

import org.iebbuda.mozi.domain.product.domain.DepositProduct;
import org.iebbuda.mozi.domain.product.domain.SavingProduct;
import org.iebbuda.mozi.domain.product.dto.DepositOptionResponse;
import org.iebbuda.mozi.domain.product.dto.DepositResponse;
import org.iebbuda.mozi.domain.product.dto.SavingResponse;
import org.iebbuda.mozi.domain.product.mapper.DepositMapper;
import org.iebbuda.mozi.common.response.BaseException;
import org.iebbuda.mozi.common.response.BaseResponseStatus;

import org.springframework.cache.annotation.Cacheable;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepositQueryService {

    private final DepositMapper depositMapper;


    /**
     * 모든 예금 상품 반환 (JOIN)
     */
    @Cacheable("deposits")
    public List<DepositResponse> getAllDeposits() {
        long start = System.currentTimeMillis(); // ⬅️ 시작 시간 측정
        System.out.println("DB or API 호출 발생");
        List<DepositProduct> products = depositMapper.findAllWithOptions();

        long end = System.currentTimeMillis();   // ⬅️ 종료 시간 측정

        System.out.println("getAllDeposits 실행 시간: " + (end - start) + "ms");

        return products.stream().map(this::toResponse).toList();
    }

    /**
     * 단일 예금 상품 반환 (JOIN)
     */
    @Cacheable(value = "deposit", key = "#id")
    public DepositResponse getDepositById(Long id) {
        long start = System.currentTimeMillis(); // 시작 시간 측정
        DepositProduct product = depositMapper.findByIdWithOptions(id);
        long end = System.currentTimeMillis();   // 종료 시간 측정

        System.out.println("getDepositById 실행 시간: " + (end - start) + "ms");

        if (product == null) {
            throw new BaseException(BaseResponseStatus.USER_NOT_FOUND); // 도메인 전용 상태가 있다면 교체 권장
        }
        return toResponse(product);
    }

    public List<DepositResponse> getTopDepositProduct(int limit) {
        List<DepositProduct> products = depositMapper.findTopDepositProduct(limit);
        return products.stream().map(this::toResponse).toList();
    }

    private DepositResponse toResponse(DepositProduct product) {
        return DepositResponse.builder()
                .depositId(product.getDepositId())
                .bankCode(product.getFinCoNo())
                .bankName(product.getKorCoNm())
                .productName(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .joinDeny(product.getJoinDeny())
                .joinMember(product.getJoinMember())
                .specialCondition(product.getSpclCnd())
                .etcNote(product.getEtcNote())
                .maxLimit(product.getMaxLimit())
                .disclosureMonth(formatMonth(product.getDclsMonth()))
                .disclosureStartDate(product.getDclsStrtDay())
                .disclosureEndDate(product.getDclsEndDay())
                .options(product.getOptions().stream()
                        .map(opt -> DepositOptionResponse.builder()
                                .intrRateType(opt.getIntrRateType())
                                .intrRateTypeNm(opt.getIntrRateTypeNm())
                                .saveTrm(opt.getSaveTrm())
                                .intrRate(opt.getIntrRate())
                                .intrRate2(opt.getIntrRate2())
                                .build())
                        .toList())
                .build();
    }

    private String formatMonth(String yyyyMM) {
        return yyyyMM != null ? yyyyMM.substring(0, 4) + "-" + yyyyMM.substring(4, 6) : null;
    }


}