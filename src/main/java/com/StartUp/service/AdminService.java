package com.StartUp.service;

import com.StartUp.dtos.admin.AdminDtos;
import com.StartUp.dtos.user.UserDtos;
import com.StartUp.entity.Category;
import com.StartUp.entity.User;
import com.StartUp.enums.Role;
import com.StartUp.enums.UserStatus;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.CategoryRepository;
import com.StartUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor

public class AdminService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;


    @Transactional(readOnly = true)

    public AdminDtos.DashboardStatsResponse getDashBoardStatus(){
        return new AdminDtos.DashboardStatsResponse(
                userRepository.count(),
                userRepository.countByRole(Role.STUDENT),
                userRepository.countByRole(Role.EMPLOYER),
                userRepository.countByStatus(UserStatus.PENDING),
                userRepository.countByStatus(UserStatus.BLOCKED),
                categoryRepository.count()

        );
    }
    @Transactional(readOnly = true)
    public Page<UserDtos.UserProfileResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToResponse); // ← map each User to DTO
    }


    @Transactional(readOnly = true)
    public Page<User> getUsersByStatus(UserStatus status, Pageable pageable){
        return userRepository.findByStatus(status,pageable);
    }

    @Transactional
    public void updateUserStatus(Long userId, AdminDtos.UpdateUserStatusRequest request){
        User user = getUser(userId);
        user.setStatus(request.status());
        userRepository.save(user);
    }

    @Transactional
    public void verifyUser(Long userId){
        User user = getUser(userId);

        if(user.getStatus() != UserStatus.PENDING){
            throw new AppExceptions.BadRequestException("Потребителят не е в PENDING статус.");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void blockUser(Long userId){
        User user = getUser(userId);

        if(user.getRole() == Role.ADMIN){
            throw new AppExceptions.BadRequestException("Не можете да блокирате Admin акаунт.");
        }

        user.setStatus(UserStatus.BLOCKED);
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId){
        User user = getUser(userId);

        if(user.getRole() == Role.ADMIN){
            throw new AppExceptions.BadRequestException("Не можете да изтриете Admin акаунт.");
        }

        userRepository.delete(user);
    }


    @Transactional(readOnly = true)
    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }

    @Transactional
    public Category createCategory(AdminDtos.CategoryRequest request){
        if(categoryRepository.existsByName(request.name())){
            throw new AppExceptions.BadRequestException("Категория с това име вече съществува.");
        }

        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, AdminDtos.CategoryRequest request){
        Category category = categoryRepository.findById(id).orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Категорията не е намерена: " + id));

        category.setName(request.name());
        category.setDescription(request.description());

        return categoryRepository.save(category);
    }

    @Transactional
    public void toggleCategory(Long id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Категорията не е намерена: " + id));

        category.setIsActive(!category.getIsActive());
        categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id){

        if(!categoryRepository.existsById(id)){
            throw new AppExceptions.ResourceNotFoundException("Категорията не е намерена: " + id);
        }

        categoryRepository.deleteById(id);

    }


    private UserDtos.UserProfileResponse mapToResponse(User user) {
        return new UserDtos.UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getStatus(),
                user.getAvatarUrl(),
                user.getCreatedAt()
        );
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Потребителят не е намерен: " + id));
    }
}
