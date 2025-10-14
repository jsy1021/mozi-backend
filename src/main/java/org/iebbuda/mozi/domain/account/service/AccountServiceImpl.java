package org.iebbuda.mozi.domain.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.iebbuda.mozi.domain.account.domain.AccountVO;
import org.iebbuda.mozi.domain.account.domain.BankLoginVO;
import org.iebbuda.mozi.domain.account.dto.AccountResponseDTO;
import org.iebbuda.mozi.domain.account.dto.BankLoginRequestDTO;
import org.iebbuda.mozi.domain.account.dto.BankSummaryDTO;
import org.iebbuda.mozi.domain.account.external.ExternalApiClient;
import org.iebbuda.mozi.domain.account.mapper.AccountMapper;
import org.iebbuda.mozi.domain.account.mapper.BankLoginMapper;
import org.iebbuda.mozi.domain.user.mapper.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Log4j2
public class AccountServiceImpl implements AccountService {
    private final AuthService authService;
    private final ConnectionService connectionService;

    private final ExternalApiClient externalApiClient;

    private final AccountMapper accountMapper;
    private final BankLoginMapper bankLoginMapper;
    private final UserMapper userMapper;


    @Override
    public List<AccountVO> fetchAccounts(String bankCode, String connectedId) {
        List<AccountVO> accountList = new ArrayList<>();

        // 1. Access Token 발급
        String accessToken = authService.getAccessToken();

        // 2. API 요청 URL 및 Header
        String url = "https://development.codef.io/v1/kr/bank/p/account/account-list";
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Authorization", "Bearer " + accessToken
        );

        // 3. Request Body
        Map<String, String> requestBody = Map.of(
                "organization", bankCode,
                "connectedId", connectedId,
                "withdrawAccountNo", "",
                "withdrawAccountPassword", ""
        );

        // 4. 외부 API 호출
        ResponseEntity<String> response = externalApiClient.post(url, headers, requestBody, String.class);
        String decoded = URLDecoder.decode(response.getBody(), StandardCharsets.UTF_8);

        try {
            // 5. JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> resultMap = objectMapper.readValue(decoded, Map.class);
            Map<String, Object> data = (Map<String, Object>) resultMap.get("data");

            String[] keys = {"resDepositTrust", "resFund", "resLoan"};

            for (String key : keys) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) data.get(key);
                if (items == null) continue;

                for (Map<String, Object> accountData : items) {
                    AccountVO dto = new AccountVO();
                    dto.setAccountNumber((String) accountData.get("resAccountDisplay"));
                    dto.setAccountName(accountData.getOrDefault("resAccountName", "").toString());
                    dto.setBalance(Double.parseDouble(accountData.get("resAccountBalance").toString()));
                    dto.setCurrency((String) accountData.get("resAccountCurrency"));
                    dto.setProductType(Integer.parseInt(accountData.get("resAccountDeposit").toString()));

                    if ("resDepositTrust".equals(key)) {
                        dto.setIsMinus("1".equals(String.valueOf(accountData.get("resOverdraftAcctYN"))));
                    } else if ("resLoan".equals(key)) {
                        dto.setIsMinus(true);
                    } else {
                        dto.setIsMinus(false);
                    }

                    accountList.add(dto);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("계좌 리스트 파싱 중 오류", e);
        }

        return accountList;
    }

    @Override
    public Map<String, Object> getAccounts(Integer userId){
        List<AccountResponseDTO> list=accountMapper.getAccountByUserID(userId);
        return Map.of("accountList", list);

    }

    @Override
    public boolean addAccounts(BankLoginRequestDTO req, Integer userId) {
        // 1. connectedId 요청 (ID/PW 인증)
        String connectedId = connectionService.connect(req);
        if(connectedId.equals("")) return false;

        // 2. DB에 연결 정보 저장
        BankLoginVO login = BankLoginVO.builder()
                .bankCode(req.getBankCode())
                .userId(userId)
                .connectedId(connectedId)
                .build();
        bankLoginMapper.add(login);  // 자동으로 bankLoginId 부여됨

        // 3. 계좌 목록 조회 (내부에서 accessToken 자동 발급)
        List<AccountVO> fetchedAccounts = fetchAccounts(req.getBankCode(), connectedId);

        // 4. 중복 계좌 제거 후 DB 저장
        List<String> existingAccountNumbers = accountMapper.getAccountNumberByBankLoginId(login.getBankLoginId());
        List<AccountVO> addedAccounts = new ArrayList<>();

        for (AccountVO account : fetchedAccounts) {
            if (!existingAccountNumbers.contains(account.getAccountNumber())) {
                account.setBankLoginId(login.getBankLoginId());
                accountMapper.add(account);
                addedAccounts.add(account);
            }
        }

        return true;
    }

    @Override
    public Map<String, Object> refreshAccounts(Integer userId) {
        List<BankLoginVO> bankLogins = bankLoginMapper.getByUserID(userId);
        List<AccountVO> resultList = new ArrayList<>();

        for (BankLoginVO login : bankLogins) {
            // 내부에서 accessToken 발급
            List<AccountVO> fetchedAccounts = fetchAccounts(login.getBankCode(), login.getConnectedId());
            List<String> existingAccountNumbers = accountMapper.getAccountNumberByBankLoginId(login.getBankLoginId());

            for (AccountVO account : fetchedAccounts) {
                account.setBankLoginId(login.getBankLoginId());

                if (existingAccountNumbers.contains(account.getAccountNumber())) {
                    accountMapper.updateBalance(account);
                } else {
                    accountMapper.add(account);
                }

                resultList.add(account);
            }
        }

        return Map.of("accountList", resultList);
    }

    @Override
    public Map<String, Object> deleteAccounts(List<String> bankCodeList, Integer userId) {
        String mainBankCode = userMapper.getMainBankCodeByUserId(userId);
        log.debug("mainBankCode: {}", mainBankCode);

        List<BankLoginVO> bankLogins = bankLoginMapper.getByUserID(userId);
        List<String> deletedBanks = new ArrayList<>();

        for (BankLoginVO login : bankLogins) {
            if (bankCodeList.contains(login.getBankCode())) {
                int bankLoginId = login.getBankLoginId();
                accountMapper.deleteByBankLoginId(bankLoginId);
                bankLoginMapper.deleteById(bankLoginId);
                deletedBanks.add(login.getBankCode());
                if (login.getBankCode().equals(mainBankCode)) {
                    userMapper.clearMainBankByUserId(userId);
                    log.debug("main_bank NULL 설정 (userId: {})", userId);
                }
            }
        }

        return Map.of("deletedBankCodeList",deletedBanks);
    }
    //계좌없을 때도 처리함
    //일단 "원"만 고려
    @Override
    public Map<String, Object> getBankSummary(Integer userId) {
        String mainBankCode = userMapper.getMainBankCodeByUserId(userId);

        List<BankSummaryDTO> list = accountMapper.getBankSummaryByUserId(userId);
        Double totalBalance = 0.0;
        boolean isConnected = false;
        if (list != null && !list.isEmpty()) {
            isConnected = true;

            for (BankSummaryDTO summary : list) {
                summary.setTotalBalance(summary.getTotalBalance() != null ? summary.getTotalBalance() : 0.0);
                summary.setRepresentativeAccountName(
                        summary.getRepresentativeAccountName() != null ? summary.getRepresentativeAccountName() : ""
                );
                totalBalance += summary.getTotalBalance();
            }
            list.sort((a, b) -> {
                if (a.getBankCode().equals(mainBankCode)) return -1;
                if (b.getBankCode().equals(mainBankCode)) return 1;
                return 0;
            });

        }

        return Map.of(
                "totalBalance", totalBalance,
                "BankSummaryList", list,
                "isConnected", isConnected,
                "mainBankCode", mainBankCode
        );
    }
    @Override
    public Map<String, Object> getMainBankSummary(Integer userId) {
        Map<String, Object> result = getBankSummary(userId);
        List<BankSummaryDTO> summaryList = (List<BankSummaryDTO>) result.get("BankSummaryList");

        String mainBankCode = userMapper.getMainBankCodeByUserId(userId);

        BankSummaryDTO mainSummary = null;

        // 주거래 은행 찾기
        if (!mainBankCode.equals("")) {
            for (BankSummaryDTO dto : summaryList) {
                if (mainBankCode.equals(dto.getBankCode())) {
                    mainSummary = dto;
                    break;
                }
            }
        }

        // 주거래 은행 없거나 요약 없을 경우 → 잔액 많은 은행 선택
        if (mainSummary == null && !summaryList.isEmpty()) {
            mainSummary = Collections.max(summaryList, Comparator.comparingDouble(BankSummaryDTO::getTotalBalance));
        }

        boolean success = (mainSummary != null);
        if(mainSummary == null) {
            mainSummary=new BankSummaryDTO();
        }
        return Map.of(
                "mainBankCode", mainBankCode,
                "success", success,
                "mainBankSummary", mainSummary
        );
    }

    //비어있을 때는 빈 리스트
    @Override
    public Map<String, Object> getAccountsByBank(String bankCode, Integer userId) {
        return Map.of("accountList",accountMapper.getAccountsByUserIdAndBank(userId, bankCode));
    }

    @Override
    public Map<String, Object> getBank(Integer userId){
        //수정 userMapper로
        String mainBankCode = userMapper.getMainBankCodeByUserId(userId);
        //String mainBankCode="";
        List<String> list=bankLoginMapper.getBankCodeByUserId(userId);
        return Map.of("mainBankCode", mainBankCode,"bankList", list);
    }
    @Override
    public Map<String, Object> getAccountsByGoal(Integer goalId, Integer userId) {
        List<AccountResponseDTO> list=accountMapper.findAccountsByUserAndGoal(userId, goalId);
        return Map.of("accountList", list);
    }

    @Override
    public Map<String, Object> updateMainBankCode(String bankCode, Integer userId) {
        if (bankCode == null || bankCode.isEmpty()) {
            userMapper.clearMainBankByUserId(userId); // main_bank = null
        } else {
            userMapper.updateMainBankCodeByUserId(bankCode, userId);
        }
        return Map.of("success", true);

    }

    @Override
    public Map<String, Object> updateAccountsByGoal(List<String> accountNumberList, Integer goalId, Integer userId) {
        accountMapper.clearGoalFromAccounts(goalId, userId);

        if (accountNumberList != null && !accountNumberList.isEmpty()) {
            for (String accountNumber : accountNumberList) {
                accountMapper.assignGoalToAccount(accountNumber, goalId, userId);
            }
        }

        return Map.of("success", true);
    }
    public Map<String, Object> getConnectedBanks(Integer userId) {
        List<String> connectedBankCodes=bankLoginMapper.getBankCodeByUserId(userId);
        return Map.of("bankCodeList", connectedBankCodes);
    }

    @Override
    @Transactional
    public void deleteAllAccountsByUserId(int userId) {
        log.info("사용자 계좌 데이터 삭제 시작 - userId: {}", userId);
        accountMapper.deleteAllAccountsByUserId(userId);
        log.info("사용자 계좌 데이터 삭제 완료 - userId: {}", userId);
    }

    @Override
    @Transactional
    public void deleteAllBankLoginsByUserId(int userId) {
        log.info("사용자 은행 연동 데이터 삭제 시작 - userId: {}", userId);
        bankLoginMapper.deleteAllBankLoginsByUserId(userId);
        log.info("사용자 은행 연동 데이터 삭제 완료 - userId: {}", userId);
    }
}

