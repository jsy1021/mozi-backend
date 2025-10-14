package org.iebbuda.mozi.domain.product.service;

/**
 * 동기화 집계 결과
 */
public class SyncResult {
    private final boolean success;
    private final int savedCount;
    private final int errorCount;
    private final String message;

    public SyncResult(boolean success, int savedCount, int errorCount, String message) {
        this.success = success;
        this.savedCount = savedCount;
        this.errorCount = errorCount;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getSavedCount() {
        return savedCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public String getMessage() {
        return message;
    }
}


