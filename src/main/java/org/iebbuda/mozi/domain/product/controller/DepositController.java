package org.iebbuda.mozi.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.iebbuda.mozi.domain.product.dto.DepositResponse;
import org.iebbuda.mozi.domain.product.service.DepositQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
public class DepositController {
    private final DepositQueryService depositQueryService;

    //전체 정기예금 목록 조회
    @GetMapping
    public ResponseEntity<List<DepositResponse>> getAllDepositProduct(){
        return ResponseEntity.ok(depositQueryService.getAllDeposits());
    }

    //특정 정기예금 상세조회
    @GetMapping("/{id}")
    public ResponseEntity<DepositResponse> getDepositProductById(@PathVariable Long id){
        return ResponseEntity.ok(depositQueryService.getDepositById(id));
    }
    @GetMapping("/top")
    public ResponseEntity<List<DepositResponse>> getTopDepositProduct(){
        return ResponseEntity.ok(depositQueryService.getTopDepositProduct(2));
    }
}
