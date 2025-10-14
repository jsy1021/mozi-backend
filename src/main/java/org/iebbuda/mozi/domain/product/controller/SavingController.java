package org.iebbuda.mozi.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.iebbuda.mozi.domain.product.dto.SavingResponse;
import org.iebbuda.mozi.domain.product.service.SavingQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
public class SavingController {

    private final SavingQueryService savingQueryService;

    //전체 적금 목록 조회
    @GetMapping
    public ResponseEntity<List<SavingResponse>> getAllSavingProduct(){
        return ResponseEntity.ok(savingQueryService.getAllSavings());
    }

    //특정 적금 상세조회
    @GetMapping("/{id}")
    public ResponseEntity<SavingResponse> getSavingProductById(@PathVariable Long id){
        return ResponseEntity.ok(savingQueryService.getSavingById(id));
    }

    @GetMapping("/top")
    public ResponseEntity<List<SavingResponse>> getTopSavingProduct(){
        return ResponseEntity.ok(savingQueryService.getTopSavingProduct(2));
    }
}
