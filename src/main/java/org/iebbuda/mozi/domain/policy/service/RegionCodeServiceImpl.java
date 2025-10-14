package org.iebbuda.mozi.domain.policy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.iebbuda.mozi.domain.policy.domain.RegionCodeVO;
import org.iebbuda.mozi.domain.policy.mapper.RegionCodeMapper;
import org.iebbuda.mozi.domain.policy.util.RegionCodeApiCaller;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
@Log4j2
public class RegionCodeServiceImpl implements RegionCodeService {

    private final RegionCodeMapper regionCodeMapper;
    private final RegionCodeApiCaller regionCodeApiCaller;

    // 지역명으로 zip 코드 조회
    @Override
    public List<String> getZipCodesByRegionNames(List<String> regionNames) {
        return regionCodeMapper.findZipCodesByRegionNames(regionNames);
    }

    // zip 코드로 지역명 조회
    @Override
    public List<String> getRegionNamesByZipCodes(List<String> zipCodes) {
        return regionCodeMapper.findRegionNamesByZipCodes(zipCodes);
    }

    // 전체 지역코드 목록 조회
    @Override
    public List<RegionCodeVO> getAllRegionCodes() {
        return regionCodeMapper.findAll();
    }

    // 시, 도로 조회
    @Override
    public List<String> findZipCodesBySido(String sido) {
        return regionCodeMapper.findZipCodesBySido(sido);
    }


    // zipCd API 호출 후 저장
    @Override
    public void fetchAndSaveFromApi() {
        Map<String, Map<String, String>> regionMap = regionCodeApiCaller.fetchAllZipCodes(100, 500);
        int count = 0;

        for (String sido : regionMap.keySet()) {
            Map<String, String> sigunguMap = regionMap.get(sido);
            for (Map.Entry<String, String> entry : sigunguMap.entrySet()) {
                RegionCodeVO region = new RegionCodeVO();
                region.setSido(sido);
                region.setSigungu(entry.getKey());
                region.setZipCode(entry.getValue());

                try {
                    regionCodeMapper.insertRegionCode(region);
                    count++;
                } catch (Exception e) {
                    log.warn("저장 실패: {} {} ({})", sido, entry.getKey(), entry.getValue());
                }
            }
        }

        log.info("저장 완료. 총 {}개 저장됨.", count);
    }

    @PostConstruct
    public void initRegionCodeIfEmpty() {
        List<RegionCodeVO> list = regionCodeMapper.findAll();
        if (list.isEmpty()) {
            log.info("RegionCode DB 비어 있음 → 외부 API로 자동 fetch 시작");
            fetchAndSaveFromApi();
        } else {
            log.info("RegionCode DB 이미 존재 → 자동 fetch 건너뜀");
        }
    }
}
