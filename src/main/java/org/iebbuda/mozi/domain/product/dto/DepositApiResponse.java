package org.iebbuda.mozi.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.iebbuda.mozi.domain.product.domain.DepositOption;
import org.iebbuda.mozi.domain.product.domain.DepositProduct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Data
@Log4j2
public class DepositApiResponse {

    @JsonProperty("result")
    private Result result;

    @Data
    public static class Result {
        @JsonProperty("prdt_div")
        private String prdtDiv; // 상품 구분 (D: 예금)

        @JsonProperty("total_count")
        private int totalCount;

        @JsonProperty("max_page_no")
        private int maxPageNo;

        @JsonProperty("now_page_no")
        private int nowPageNo;

        @JsonProperty("err_cd")
        private String errCd;

        @JsonProperty("err_msg")
        private String errMsg;

        @JsonProperty("baseList")
        private List<BaseInfoDTO> baseList;

        @JsonProperty("optionList")
        private List<OptionDTO> optionList;
    }

    @Data
    public static class BaseInfoDTO {
        @JsonProperty("dcls_month")
        private String dclsMonth;

        @JsonProperty("fin_co_no")
        private String finCoNo;

        @JsonProperty("kor_co_nm")
        private String korCoNm;

        @JsonProperty("fin_prdt_cd")
        private String finPrdtCd;

        @JsonProperty("fin_prdt_nm")
        private String finPrdtNm;

        @JsonProperty("join_way")
        private String joinWay;

        @JsonProperty("mtrt_int")
        private String mtrtInt;

        @JsonProperty("spcl_cnd")
        private String spclCnd;

        @JsonProperty("join_deny")
        private String joinDeny;

        @JsonProperty("join_member")
        private String joinMember;

        @JsonProperty("etc_note")
        private String etcNote;

        @JsonProperty("max_limit")
        private BigDecimal maxLimit;

        @JsonProperty("dcls_strt_day")
        private String dclsStrtDay;

        @JsonProperty("dcls_end_day")
        private String dclsEndDay;

        @JsonProperty("fin_co_subm_day")
        private String finCoSubmDay;

        /**
         * Entity 변환 메소드 (DB 저장용)
         */
        public DepositProduct toEntity() {
            DepositProduct product = new DepositProduct();
            product.setFinPrdtCd(this.finPrdtCd);
            product.setFinCoNo(this.finCoNo);
            product.setKorCoNm(this.korCoNm);
            product.setFinPrdtNm(this.finPrdtNm);
            product.setJoinWay(this.joinWay);
            product.setJoinDeny(this.joinDeny);
            product.setJoinMember(this.joinMember);
            product.setSpclCnd(this.spclCnd);
            product.setEtcNote(this.etcNote);
            product.setMaxLimit(this.maxLimit);

            // dclsMonth 그대로 저장
            if (this.dclsMonth != null) {
                product.setDclsMonth(this.dclsMonth);
            }

            //  dclsStrtDay 처리
            if (this.dclsStrtDay != null && !this.dclsStrtDay.isBlank()) {
                product.setDclsStrtDay(
                        LocalDate.parse(this.dclsStrtDay, DateTimeFormatter.ofPattern("yyyyMMdd"))
                );
            }

            //  dclsEndDay 처리
            if (this.dclsEndDay != null && !this.dclsEndDay.isBlank()) {
                product.setDclsEndDay(
                        LocalDate.parse(this.dclsEndDay, DateTimeFormatter.ofPattern("yyyyMMdd"))
                );
            }

            //  finCoSubmDay 처리 (yyyyMMdd vs yyyyMMddHHmm 구분)
            if (this.finCoSubmDay != null && !this.finCoSubmDay.isBlank()) {
                if (this.finCoSubmDay.length() == 8) {
                    // yyyyMMdd 형식
                    product.setFinCoSubmDay(
                            LocalDate.parse(this.finCoSubmDay, DateTimeFormatter.ofPattern("yyyyMMdd"))
                                    .atStartOfDay()
                    );
                } else if (this.finCoSubmDay.length() == 12) {
                    // yyyyMMddHHmm 형식
                    product.setFinCoSubmDay(
                            LocalDateTime.parse(this.finCoSubmDay, DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
                    );
                } else {
                    // 알 수 없는 형식 경고
                    log.warn("알 수 없는 날짜 형식: {}", this.finCoSubmDay);
                }
            }

            return product;
        }
    }

    @Data
    public static class OptionDTO {
        @JsonProperty("intr_rate_type")
        private String intrRateType;

        @JsonProperty("intr_rate_type_nm")
        private String intrRateTypeNm;

        @JsonProperty("save_trm")
        private int saveTrm;

        @JsonProperty("intr_rate")
        private BigDecimal intrRate;

        @JsonProperty("intr_rate2")
        private BigDecimal intrRate2;

        @JsonProperty("fin_prdt_cd")
        private String finPrdtCd; // 옵션과 상품 연결용

        /**
         * Entity 변환 메소드 (DB 저장용)
         */
        public DepositOption toEntity(Long depositId) {
            DepositOption option = new DepositOption();
            option.setDepositId(depositId);
            option.setIntrRateType(this.intrRateType);
            option.setIntrRateTypeNm(this.intrRateTypeNm);
            option.setSaveTrm(this.saveTrm);
            option.setIntrRate(this.intrRate);
            option.setIntrRate2(this.intrRate2);
            return option;
        }
    }
}