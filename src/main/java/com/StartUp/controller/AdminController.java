package com.StartUp.controller;

import com.StartUp.dtos.admin.AdminDtos;
import com.StartUp.entity.Category;
import com.StartUp.entity.User;
import com.StartUp.enums.UserStatus;
import com.StartUp.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminDtos.DashboardStatsResponse> getStatus(){
        return ResponseEntity.ok(adminService.getDashBoardStatus());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(@PageableDefault(size = 20)Pageable pageable){
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @GetMapping("/users/filter")
    public ResponseEntity<Page<User>> getUsersByStatus(@RequestParam UserStatus status,@PageableDefault(size = 20)Pageable pageable){
        return ResponseEntity.ok(adminService.getUsersByStatus(status,pageable));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long id, @Valid @RequestBody AdminDtos.UpdateUserStatusRequest request){
        adminService.updateUserStatus(id,request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long id){
        adminService.blockUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories(){
        return ResponseEntity.ok(adminService.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestParam @Valid AdminDtos.CategoryRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestParam @Valid AdminDtos.CategoryRequest request){
        return ResponseEntity.ok(adminService.updateCategory(id,request));
    }

    @PatchMapping("/categories/{id}/toggle")
    public ResponseEntity<Void> toggleCategories(@PathVariable Long id){
        adminService.toggleCategory(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id){
        adminService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
