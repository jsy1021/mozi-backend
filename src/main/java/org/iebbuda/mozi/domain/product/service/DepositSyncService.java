package org.iebbuda.mozi.domain.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.iebbuda.mozi.domain.product.domain.DepositOption;
import org.iebbuda.mozi.domain.product.domain.DepositProduct;
import org.iebbuda.mozi.domain.product.dto.DepositApiResponse;
import org.iebbuda.mozi.domain.product.mapper.DepositSyncMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class DepositSyncService {

    private final DepositSyncMapper depositSyncMapper;
    private final FinancialApiService financialApiService;

    /**
     * 금융감독원 API에서 정기예금 데이터를 호출하고 DB에 저장
     */
    @Transactional
    public SyncResult fetchAndSaveDeposits() {
        int pageNo = 1;
        int totalPageNo=1;
        int saved = 0;
        int errors = 0;

        while (pageNo <= totalPageNo) {
            // API 호출
            DepositApiResponse response = financialApiService.callDepositApi(pageNo);

            if (response == null || response.getResult() == null) {
                log.error("정기예금 API 응답이 null입니다 (pageNo={})", pageNo);
                break;
            }

            var result = response.getResult();

            // API 호출 실패 체크
            if (!"000".equals(result.getErrCd())) {
                log.error("정기예금 API 호출 실패: {}", result.getErrMsg());
                break;
            }

            List<DepositApiResponse.BaseInfoDTO> baseList = result.getBaseList();
            List<DepositApiResponse.OptionDTO> optionList = result.getOptionList();

            if (baseList == null || baseList.isEmpty()) {
                log.warn("정기예금 API baseList가 비어있습니다 (pageNo={})", pageNo);
                break;
            }

            // 상품 및 옵션 DB 저장
            for (DepositApiResponse.BaseInfoDTO productDTO : baseList) {
                try {
                    saveOrUpdateDeposit(productDTO, optionList);
                    log.info("정기예금 저장 완료: {}", productDTO.getFinPrdtNm());
                    saved++;
                } catch (Exception e) {
                    log.error("정기예금 저장 실패: {}", productDTO.getFinPrdtNm(), e);
                    errors++;
                }
            }

            if (pageNo >= result.getMaxPageNo()) {
                log.info("모든 페이지 처리 완료 (totalPage={})", result.getMaxPageNo());
                break;
            }
            pageNo++;
        }
        log.info("정기예금 동기화 집계 saved={}, errors={}", saved, errors);
        return new SyncResult(errors == 0, saved, errors, "deposit sync finished");
    }

    /**
     * 정기예금 상품 및 옵션을 DB에 저장 (업데이트 or 신규 삽입)
     */
    private void saveOrUpdateDeposit(DepositApiResponse.BaseInfoDTO productDTO, List<DepositApiResponse.OptionDTO> optionList) {
        // 상품 존재 여부 확인
        DepositProduct existingProduct = depositSyncMapper.findByProductCode(productDTO.getFinPrdtCd());

        if (existingProduct == null) {
            // 신규 저장
            DepositProduct newProduct = productDTO.toEntity();
            depositSyncMapper.insertProduct(newProduct);
            log.debug("신규 정기예금 저장: {}", newProduct.getFinPrdtNm());

            // 옵션 저장
            List<DepositOption> options = mapOptions(optionList, productDTO.getFinPrdtCd(), newProduct.getDepositId());
            for (DepositOption option : options) {
                depositSyncMapper.insertOption(option);
            }
        } else {
            // 기존 상품 업데이트
            DepositProduct updatedProduct = productDTO.toEntity();
            updatedProduct.setDepositId(existingProduct.getDepositId());
            depositSyncMapper.updateProduct(updatedProduct);
            log.debug("기존 정기예금 업데이트: {}", updatedProduct.getFinPrdtNm());

            // 기존 옵션 삭제 후 다시 삽입
            depositSyncMapper.deleteOptionsByProductId(existingProduct.getDepositId());
            List<DepositOption> options = mapOptions(optionList, productDTO.getFinPrdtCd(), existingProduct.getDepositId());
            for (DepositOption option : options) {
                depositSyncMapper.insertOption(option);
            }
        }
    }

    /**
     * 옵션 DTO 리스트를 엔티티로 변환 (특정 상품코드에 해당하는 옵션만 필터링)
     */
    private List<DepositOption> mapOptions(List<DepositApiResponse.OptionDTO> optionList, String finPrdtCd, Long depositId) {
        if (optionList == null) {
            return List.of();
        }

        return optionList.stream()
                .filter(option -> finPrdtCd.equals(option.getFinPrdtCd()))
                .map(optionDTO -> optionDTO.toEntity(depositId))
                .collect(Collectors.toList());
    }
}