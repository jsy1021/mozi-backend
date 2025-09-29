package org.iebbuda.mozi.domain.product.service;

import lombok.RequiredArgsConstructor;

import org.iebbuda.mozi.domain.product.domain.SavingProduct;
import org.iebbuda.mozi.domain.product.dto.SavingOptionResponse;
import org.iebbuda.mozi.domain.product.dto.SavingResponse;
import org.iebbuda.mozi.domain.product.mapper.SavingMapper;
import org.iebbuda.mozi.common.response.BaseException;
import org.iebbuda.mozi.common.response.BaseResponseStatus;

import org.springframework.cache.annotation.Cacheable;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingQueryService {

    private final SavingMapper savingMapper;


    /**
     * 모든 적금 상품 반환
     */
    @Cacheable("savings")
    public List<SavingResponse>getAllSavings(){
        long start=System.currentTimeMillis();//⬅️ 시작 시간 측정
        List<SavingProduct>products=savingMapper.findAllWithOptions();
        long end=System.currentTimeMillis();// ⬅️ 종료 시간 측정
        System.out.println("getAllSavings 실행시간 "+(end-start)+" ms");

        return products.stream().map(this::toResponse).toList();
    }

    /**
     * 모든 적금 상품 반환
     */
    @Cacheable(value = "saving", key ="#id")
    public SavingResponse getSavingById(Long id){
        long start=System.currentTimeMillis();//⬅️ 시작 시간 측정
        SavingProduct product=savingMapper.findByIdWithOptions(id);
        long end=System.currentTimeMillis();

        System.out.println("getAllSavings 실행시간 "+(end-start)+" ms");

        if (product == null) {
            throw new BaseException(BaseResponseStatus.USER_NOT_FOUND); // 도메인 전용 상태가 있다면 교체 권장
        }
        return toResponse(product);
    }

    public List<SavingResponse> getTopSavingProduct(int limit) {
        List<SavingProduct> products = savingMapper.findTopSavingProduct(limit);
        return products.stream().map(this::toResponse).toList();
    }


    private SavingResponse toResponse(SavingProduct product){
        return SavingResponse.builder()
                .savingId(product.getSavingId())
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
                        .map(opt-> SavingOptionResponse.builder()
                                .intrRateType(opt.getIntrRateType())
                                .intrRateTypeNm(opt.getIntrRateTypeNm())
                                .rsrvType(opt.getRsrvType())
                                .rsrvTypeNm(opt.getRsrvTypeNm())
                                .saveTrm(opt.getSaveTrm())
                                .intrRate(opt.getIntrRate())
                                .intrRate2(opt.getIntrRate2())
                                .build())
                        .toList())
                .build();
    }

    private String formatMonth(String yyyyMM) {
        return yyyyMM.substring(0, 4) + "-" + yyyyMM.substring(4, 6);
    }


}
