package org.iebbuda.mozi.domain.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.iebbuda.mozi.domain.product.domain.SavingOption;
import org.iebbuda.mozi.domain.product.domain.SavingProduct;
import org.iebbuda.mozi.domain.product.dto.SavingApiResponse;
import org.iebbuda.mozi.domain.product.mapper.SavingSyncMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class SavingSyncService {

    private final SavingSyncMapper savingSyncMapper;
    private final FinancialApiService financialApiService;

    /**
     * 금융감독원 API에서 적금 데이터를 호출하고 DB에 저장
     */
    @Transactional
    public SyncResult fetchAndSaveSavings() {
        int pageNo = 1;
        int saved = 0;
        int errors = 0;

        while (true) {
            // API 호출
            SavingApiResponse response = financialApiService.callSavingApi(pageNo);

            if (response == null || response.getResult() == null) {
                log.error("적금 API 응답이 null입니다 (pageNo={})", pageNo);
                break;
            }

            var result = response.getResult();

            // API 호출 실패 체크
            if (!"000".equals(result.getErrCd())) {
                log.error("적금 API 호출 실패: {}", result.getErrMsg());
                break;
            }

            List<SavingApiResponse.BaseInfoDTO> baseList = result.getBaseList();
            List<SavingApiResponse.OptionDTO> optionList = result.getOptionList();

            if (baseList == null || baseList.isEmpty()) {
                log.warn("적금 API baseList가 비어있습니다 (pageNo={})", pageNo);
                break;
            }

            // 상품 및 옵션 DB 저장
            for (SavingApiResponse.BaseInfoDTO productDTO : baseList) {
                try {
                    saveOrUpdateSaving(productDTO, optionList);
                    log.info("적금 저장 완료: {}", productDTO.getFinPrdtNm());
                    saved++;
                } catch (Exception e) {
                    log.error("적금 저장 실패: {}", productDTO.getFinPrdtNm(), e);
                    errors++;
                }
            }

            if (pageNo >= result.getMaxPageNo()) {
                log.info("모든 페이지 처리 완료 (totalPage={})", result.getMaxPageNo());
                break;
            }
            pageNo++;
        }
        log.info("적금 동기화 집계 saved={}, errors={}", saved, errors);
        return new SyncResult(errors == 0, saved, errors, "saving sync finished");
    }

    /**
     * 적금 상품 및 옵션을 DB에 저장 (업데이트 or 신규 삽입)
     */
    private void saveOrUpdateSaving(SavingApiResponse.BaseInfoDTO productDTO, List<SavingApiResponse.OptionDTO> optionList) {
        // 상품 존재 여부 확인
        SavingProduct existingProduct = savingSyncMapper.findByProductCode(productDTO.getFinPrdtCd());

        if (existingProduct == null) {
            // 신규 저장
            SavingProduct newProduct = productDTO.toEntity();
            savingSyncMapper.insertProduct(newProduct);
            log.debug("신규 적금 저장: {}", newProduct.getFinPrdtNm());

            // 옵션 저장
            List<SavingOption> options = mapOptions(optionList, productDTO.getFinPrdtCd(), newProduct.getSavingId());
            for (SavingOption option : options) {
                savingSyncMapper.insertOption(option);
            }
        } else {
            // 기존 상품 업데이트
            SavingProduct updatedProduct = productDTO.toEntity();
            updatedProduct.setSavingId(existingProduct.getSavingId());
            savingSyncMapper.updateProduct(updatedProduct);
            log.debug("기존 적금 업데이트: {}", updatedProduct.getFinPrdtNm());

            // 기존 옵션 삭제 후 다시 삽입
            savingSyncMapper.deleteOptionsByProductId(existingProduct.getSavingId());
            List<SavingOption> options = mapOptions(optionList, productDTO.getFinPrdtCd(), existingProduct.getSavingId());
            for (SavingOption option : options) {
                savingSyncMapper.insertOption(option);
            }
        }
    }

    /**
     * 옵션 DTO 리스트를 엔티티로 변환 (특정 상품코드에 해당하는 옵션만 필터링)
     */
    private List<SavingOption> mapOptions(List<SavingApiResponse.OptionDTO> optionList, String finPrdtCd, Long savingId) {
        if (optionList == null) {
            return List.of();
        }

        return optionList.stream()
                .filter(option -> finPrdtCd.equals(option.getFinPrdtCd()))
                .map(optionDTO -> optionDTO.toEntity(savingId))
                .collect(Collectors.toList());
    }
}