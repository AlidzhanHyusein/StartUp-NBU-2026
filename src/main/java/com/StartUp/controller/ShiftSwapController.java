package com.StartUp.controller;

import com.StartUp.dtos.shiftswap.ShiftSwapDtos;
import com.StartUp.service.ShiftSwapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shift-swaps")
@RequiredArgsConstructor
@Tag(name = "Shift Swaps", description = "Swap marketplace — students hand off accepted shifts they can't make")
public class ShiftSwapController {

    private final ShiftSwapService shiftSwapService;

    @Operation(summary = "Post a shift to the swap marketplace (student)")
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ShiftSwapDtos.ShiftSwapResponse> postSwap(
            @RequestBody ShiftSwapDtos.PostSwapRequest request) {
        return ResponseEntity.ok(shiftSwapService.postSwap(request));
    }

    @Operation(summary = "Browse all open swaps (student)")
    @GetMapping("/marketplace")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ShiftSwapDtos.ShiftSwapResponse>> getMarketplace() {
        return ResponseEntity.ok(shiftSwapService.getMarketplace());
    }

    @Operation(summary = "Claim an open swap (student)")
    @PostMapping("/{id}/claim")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ShiftSwapDtos.ShiftSwapResponse> claimSwap(@PathVariable Long id) {
        return ResponseEntity.ok(shiftSwapService.claimSwap(id));
    }

    @Operation(summary = "Cancel your own open swap (student)")
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ShiftSwapDtos.ShiftSwapResponse> cancelSwap(@PathVariable Long id) {
        return ResponseEntity.ok(shiftSwapService.cancelSwap(id));
    }

    @Operation(summary = "My swaps — posted and claimed (student)")
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ShiftSwapDtos.ShiftSwapResponse>> getMy() {
        return ResponseEntity.ok(shiftSwapService.getMy());
    }

    @Operation(summary = "Swaps pending your approval (employer)")
    @GetMapping("/employer/pending")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<ShiftSwapDtos.ShiftSwapResponse>> getPendingForEmployer() {
        return ResponseEntity.ok(shiftSwapService.getPendingForEmployer());
    }

    @Operation(summary = "Approve or reject a claimed swap (employer)")
    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ShiftSwapDtos.ShiftSwapResponse> resolveSwap(
            @PathVariable Long id,
            @RequestParam boolean approve) {
        return ResponseEntity.ok(shiftSwapService.resolveSwap(id, approve));
    }
}
