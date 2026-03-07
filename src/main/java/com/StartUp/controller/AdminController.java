package com.StartUp.controller;

import com.StartUp.dtos.admin.AdminDtos;
import com.StartUp.entity.Category;
import com.StartUp.entity.User;
import com.StartUp.enums.UserStatus;
import com.StartUp.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin dashboard — user management and category management")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Get dashboard stats", description = "Returns platform-wide statistics such as total users, students and employers")
    @GetMapping("/stats")
    public ResponseEntity<AdminDtos.DashboardStatsResponse> getStatus(){
        return ResponseEntity.ok(adminService.getDashBoardStatus());
    }

    @Operation(summary = "Get all users", description = "Returns a paginated list of all registered users")
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(@PageableDefault(size = 20)Pageable pageable){
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Void> verifyUser(@PathVariable Long id){
        adminService.verifyUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Filter users by status", description = "Returns a paginated list of users filtered by their status (PENDING, ACTIVE, BLOCKED)")
    @GetMapping("/users/filter")
    public ResponseEntity<Page<User>> getUsersByStatus(@RequestParam UserStatus status,@PageableDefault(size = 20)Pageable pageable){
        return ResponseEntity.ok(adminService.getUsersByStatus(status,pageable));
    }

    @Operation(summary = "Update user status", description = "Updates the status of a specific user by their ID")
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long id, @Valid @RequestBody AdminDtos.UpdateUserStatusRequest request){
        adminService.updateUserStatus(id,request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Block a user", description = "Blocks a specific user by their ID, preventing them from logging in")
    @PostMapping("/users/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long id){
        adminService.blockUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a user", description = "Permanently deletes a user account by their ID")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all categories", description = "Returns a list of all job categories")
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories(){
        return ResponseEntity.ok(adminService.getAllCategories());
    }

    @Operation(summary = "Create a category", description = "Creates a new job category")
    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestParam @Valid AdminDtos.CategoryRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createCategory(request));
    }

    @Operation(summary = "Update a category", description = "Updates an existing job category by its ID")
    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestParam @Valid AdminDtos.CategoryRequest request){
        return ResponseEntity.ok(adminService.updateCategory(id,request));
    }

    @Operation(summary = "Toggle category visibility", description = "Enables or disables a category by its ID")
    @PatchMapping("/categories/{id}/toggle")
    public ResponseEntity<Void> toggleCategories(@PathVariable Long id){
        adminService.toggleCategory(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a category", description = "Permanently deletes a job category by its ID")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id){
        adminService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}