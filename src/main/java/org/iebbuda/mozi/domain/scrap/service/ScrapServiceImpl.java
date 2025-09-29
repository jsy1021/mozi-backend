package org.iebbuda.mozi.domain.scrap.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.iebbuda.mozi.domain.policy.domain.PolicyVO;
import org.iebbuda.mozi.domain.scrap.domain.PolicyScrapVO;
import org.iebbuda.mozi.domain.scrap.mapper.DepositScrapMapper;
import org.iebbuda.mozi.domain.scrap.mapper.PolicyScrapMapper;
import org.iebbuda.mozi.domain.scrap.mapper.SavingScrapMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ScrapServiceImpl implements ScrapService {

    private final PolicyScrapMapper policyScrapMapper;
    private final DepositScrapMapper depositScrapMapper;
    private final SavingScrapMapper savingScrapMapper;


    @Override
    public void scrapPolicy(int userId, String plcyNo) {
        if (!policyScrapMapper.existsScrap(userId, plcyNo)) {
            PolicyScrapVO vo = new PolicyScrapVO();
            vo.setUserId(userId);
            vo.setPlcyNo(plcyNo);
            policyScrapMapper.insertScrap(vo);
            log.info("스크랩 시도 - userId: {}, plcyNo: {}", userId, plcyNo);
        }
    }

    @Override
    public void cancelScrapPolicy(int userId, String plcyNo) {
        policyScrapMapper.deleteScrap(userId, plcyNo);
    }

    @Override
    public boolean isScrapedPolicy(int userId, String plcyNo) {
        return policyScrapMapper.existsScrap(userId, plcyNo);
    }

    @Override
    public List<String> getScrapedPolicyNos(int userId) {
        log.debug("getScrapedPolicyNos 호출됨 - userId: {}", userId);
        return policyScrapMapper.getScrapPlcyNos(userId);
    }

    @Override
    public List<PolicyVO> getScrapedPolicies(int userId) {
        return policyScrapMapper.getScrapedPolicies(userId);
    }

    @Override
    public int getTotalScrapCountByUserId(int userId) {
        log.info("전체 스크랩 개수 조회 - userId: {}", userId);

        int policyCount = policyScrapMapper.countByUserId(userId);
        int depositCount = depositScrapMapper.countByUserId(userId);
        int savingCount = savingScrapMapper.countByUserId(userId);

        int totalCount = policyCount + depositCount + savingCount;
        log.info("스크랩 개수 조회 완료 - userId: {}, 총 {}개", userId, totalCount);

        return totalCount;
    }

    @Override
    @Transactional
    public void deleteAllScrapsByUserId(int userId) {
        log.info("모든 스크랩 삭제 시작 - userId: {}", userId);

        int deletedPolicy = policyScrapMapper.deleteByUserId(userId);
        int deletedDeposit = depositScrapMapper.deleteByUserId(userId);
        int deletedSaving = savingScrapMapper.deleteByUserId(userId);

        log.info("스크랩 삭제 완료 - userId: {}, 정책: {}개, 예금: {}개, 적금: {}개",
                userId, deletedPolicy, deletedDeposit, deletedSaving);

    }
}
