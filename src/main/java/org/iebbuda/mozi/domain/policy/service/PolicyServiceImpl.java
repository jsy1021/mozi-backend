package org.iebbuda.mozi.domain.policy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.iebbuda.mozi.domain.policy.domain.PolicyVO;
import org.iebbuda.mozi.domain.policy.dto.PolicyDTO;
import org.iebbuda.mozi.domain.policy.dto.PolicyFilterDTO;
import org.iebbuda.mozi.domain.policy.mapper.PolicyMapper;
import org.iebbuda.mozi.domain.policy.util.ApiCaller;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class PolicyServiceImpl implements PolicyService {

    private final PolicyMapper policyMapper;
    private final ApiCaller apiCaller;

    // 전체 정책 조회(VO -> DTO 변환)
    @Override
    public List<PolicyDTO> findAll() {
        List<PolicyVO> voList = policyMapper.findAll();
        List<PolicyDTO> dtoList = new ArrayList<>();
        for (PolicyVO vo : voList) {
            dtoList.add(toDTO(vo));
        }
        return dtoList;
    }

    // ID로 정책 상세 조회
    @Override
    public PolicyDTO findById(int id) {
        PolicyVO vo = policyMapper.selectPolicyById(id);
        return toDTO(vo);
    }

    // 정책 저장 (중복 정책 생략)
    @Override
    public void saveAll(List<PolicyDTO> dtoList) {
        for (PolicyDTO dto : dtoList) {
            if (policyMapper.existsByPlcyNo(dto.getPlcyNo()) == 0) {
                policyMapper.insertPolicy(toVO(dto));
            }
        }
    }

    // 필터 조건에 따른 정책 목록 조회
    @Override
    public List<PolicyDTO> getPoliciesByFilters(PolicyFilterDTO filters) {
        return policyMapper.findByFilters(filters);
    }


    // 서버 실행 시 자동으로 정책 API에서 전체 fetch 후 DB에 저장
    @PostConstruct
    public void initPolicyIfNeeded() {
        log.info("YouthPolicy DB 자동 fetch 시작");

        String json = apiCaller.getJsonResponse();
        List<PolicyDTO> dtoList = apiCaller.parseJsonToPolicies(json);

        int before = policyMapper.count();
        saveAll(dtoList);
        int after = policyMapper.count();

        int added = after - before;
        log.info("정책 자동 저장 완료: {}건 추가됨 (총 {}건)", added, after);
    }

    // 마감 임박 정책 조회
    @Override
    public List<PolicyVO> getDeadlineSoonPolicies(int days) {
        return policyMapper.selectDeadlineSoonPolicies(days);
    }

    // 내부 변환 메서드
    private PolicyDTO toDTO(PolicyVO vo) {
        PolicyDTO dto = new PolicyDTO();
        dto.setPolicyId(vo.getPolicyId());
        dto.setPlcyNm(vo.getPlcyNm());
        dto.setPlcyNo(vo.getPlcyNo());
        dto.setPlcyExplnCn(vo.getPlcyExplnCn());
        dto.setPlcySprtCn(vo.getPlcySprtCn());
        dto.setZipCd(vo.getZipCd());
        dto.setMrgSttsCd(vo.getMrgSttsCd());
        dto.setSchoolCd(vo.getSchoolCd());
        dto.setJobCd(vo.getJobCd());
        dto.setPlcyMajorCd(vo.getPlcyMajorCd());
        dto.setSbizCd(vo.getSbizCd());
        dto.setAplyUrlAddr(vo.getAplyUrlAddr());
        dto.setBizPrdBgngYmd(vo.getBizPrdBgngYmd());
        dto.setBizPrdEndYmd(vo.getBizPrdEndYmd());
        dto.setLclsfNm(vo.getLclsfNm());
        dto.setMclsfNm(vo.getMclsfNm());
        dto.setPlcyKywdNm(vo.getPlcyKywdNm());
        dto.setSprtTrgtMinAge(vo.getSprtTrgtMinAge());
        dto.setSprtTrgtMaxAge(vo.getSprtTrgtMaxAge());
        dto.setEarnCndSeCd(vo.getEarnCndSeCd());
        dto.setEarnMinAmt(vo.getEarnMinAmt());
        dto.setEarnMaxAmt(vo.getEarnMaxAmt());
        dto.setEarnEtcCn(vo.getEarnEtcCn());
        dto.setRefUrlAddr1(vo.getRefUrlAddr1());
        dto.setSprvsnInstCdNm(vo.getSprvsnInstCdNm());
        dto.setPlcyAplyMthdCn(vo.getPlcyAplyMthdCn());
        dto.setAplyYmd(vo.getAplyYmd());
        dto.setSrngMthdCn(vo.getSrngMthdCn());
        dto.setPtcpPrpTrgtCn(vo.getPtcpPrpTrgtCn());
        dto.setSprtSclCnt(vo.getSprtSclCnt());

        return dto;

    }

    private PolicyVO toVO(PolicyDTO dto) {
        PolicyVO vo = new PolicyVO();
        vo.setPolicyId(dto.getPolicyId());
        vo.setPlcyNm(dto.getPlcyNm());
        vo.setPlcyNo(dto.getPlcyNo());
        vo.setPlcyExplnCn(dto.getPlcyExplnCn());
        vo.setPlcySprtCn(dto.getPlcySprtCn());
        vo.setZipCd(dto.getZipCd());
        vo.setMrgSttsCd(dto.getMrgSttsCd());
        vo.setSchoolCd(dto.getSchoolCd());
        vo.setJobCd(dto.getJobCd());
        vo.setPlcyMajorCd(dto.getPlcyMajorCd());
        vo.setSbizCd(dto.getSbizCd());
        vo.setAplyUrlAddr(
                dto.getAplyUrlAddr() != null && dto.getAplyUrlAddr().length() > 200
                        ? dto.getAplyUrlAddr().substring(0, 200)
                        : dto.getAplyUrlAddr()
        );

        // 날짜 공백 처리 추가
        vo.setBizPrdBgngYmd(
                dto.getBizPrdBgngYmd() != null && !dto.getBizPrdBgngYmd().trim().isEmpty()
                        ? dto.getBizPrdBgngYmd().trim()
                        : null
        );
        vo.setBizPrdEndYmd(
                dto.getBizPrdEndYmd() != null && !dto.getBizPrdEndYmd().trim().isEmpty()
                        ? dto.getBizPrdEndYmd().trim()
                        : null
        );

        vo.setLclsfNm(dto.getLclsfNm());
        vo.setMclsfNm(dto.getMclsfNm());
        vo.setPlcyKywdNm(dto.getPlcyKywdNm());
        vo.setSprtTrgtMinAge(dto.getSprtTrgtMinAge());
        vo.setSprtTrgtMaxAge(dto.getSprtTrgtMaxAge());
        vo.setEarnCndSeCd(dto.getEarnCndSeCd());
        vo.setEarnMinAmt(dto.getEarnMinAmt());
        vo.setEarnMaxAmt(dto.getEarnMaxAmt());
        vo.setEarnEtcCn(dto.getEarnEtcCn());
        vo.setRefUrlAddr1(dto.getRefUrlAddr1());
        vo.setSprvsnInstCdNm(dto.getSprvsnInstCdNm());
        vo.setPlcyAplyMthdCn(dto.getPlcyAplyMthdCn());
        vo.setAplyYmd(dto.getAplyYmd());
        vo.setSrngMthdCn(dto.getSrngMthdCn());
        vo.setPtcpPrpTrgtCn(dto.getPtcpPrpTrgtCn());
        vo.setSprtSclCnt(dto.getSprtSclCnt());

        return vo;
    }

}

