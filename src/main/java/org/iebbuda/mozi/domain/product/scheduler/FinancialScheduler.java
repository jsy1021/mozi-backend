package org.iebbuda.mozi.domain.product.scheduler;

import lombok.extern.log4j.Log4j2;
import org.iebbuda.mozi.domain.product.service.DepositQueryService;
import org.iebbuda.mozi.domain.product.service.DepositSyncService;
import org.iebbuda.mozi.domain.product.service.SavingQueryService;
import org.iebbuda.mozi.domain.product.service.SavingSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Log4j2
@Component
public class FinancialScheduler {

    private final DepositSyncService depositSyncService;
    private final SavingSyncService savingSyncService;
    private final DepositQueryService depositQueryService;
    private final SavingQueryService savingQueryService;

    public FinancialScheduler(DepositSyncService depositSyncService, SavingSyncService savingSyncService, DepositQueryService depositQueryService, SavingQueryService savingQueryService) {
        this.depositSyncService = depositSyncService;
        this.savingSyncService = savingSyncService;
        this.depositQueryService=depositQueryService;
        this.savingQueryService = savingQueryService;
    }

    // 매일 새벽 2시 정기예금/적금 동기화
    // FinancialScheduler
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void syncFinancialProductsDaily() {
        log.info("동기화 시작");
        try {
            var depositResult = depositSyncService.fetchAndSaveDeposits();
            depositQueryService.getAllDeposits();
            if (depositResult.isSuccess()) {
                log.info("예금 동기화 완료 saved={}, errors={}", depositResult.getSavedCount(), depositResult.getErrorCount());
            } else {
                log.warn("예금 동기화 부분 실패 saved={}, errors={}", depositResult.getSavedCount(), depositResult.getErrorCount());
            }
        } catch (Exception e) {
            log.error("예금 동기화 실패(치명적)", e);
        }

        try {
            var savingResult = savingSyncService.fetchAndSaveSavings();
            // 적금 캐시 워밍
            savingQueryService.getAllSavings();
            if (savingResult.isSuccess()) {
                log.info("적금 동기화 완료 saved={}, errors={}", savingResult.getSavedCount(), savingResult.getErrorCount());
            } else {
                log.warn("적금 동기화 부분 실패 saved={}, errors={}", savingResult.getSavedCount(), savingResult.getErrorCount());
            }
        } catch (Exception e) {
            log.error("적금 동기화 실패(치명적)", e);
        }
    }
//    @PostConstruct
//    public void init() {
//        log.info("서버 시작 시 금융감독원 데이터 동기화 시작");
//
//        // 정기예금 데이터 동기화
//        try {
//            var depositResult = depositSyncService.fetchAndSaveDeposits();
//            depositQueryService.getAllDeposits();
//            if (depositResult.isSuccess()) {
//                log.info("정기예금 동기화 완료 saved={}, errors={}", depositResult.getSavedCount(), depositResult.getErrorCount());
//            } else {
//                log.warn("정기예금 동기화 부분 실패 saved={}, errors={}", depositResult.getSavedCount(), depositResult.getErrorCount());
//            }
//        } catch (Exception e) {
//            log.error("정기예금 동기화 실패(치명적)", e);
//        }
//
//        // 적금 데이터 동기화
//        try {
//            var savingResult = savingSyncService.fetchAndSaveSavings();
//            if (savingResult.isSuccess()) {
//                log.info("적금 동기화 완료 saved={}, errors={}", savingResult.getSavedCount(), savingResult.getErrorCount());
//            } else {
//                log.warn("적금 동기화 부분 실패 saved={}, errors={}", savingResult.getSavedCount(), savingResult.getErrorCount());
//            }
//        } catch (Exception e) {
//            log.error("적금 동기화 실패(치명적)", e);
//        }
//    }


}
