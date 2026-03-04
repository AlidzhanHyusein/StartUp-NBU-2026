package com.StartUp.service;

import com.StartUp.dtos.user.UserDtos;
import com.StartUp.entity.User;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public UserDtos.UserProfileResponse getProfile(String email){

        User user = findByEmail(email);
        return mapToResponse(user);
    }


    @Transactional(readOnly = true)
    public UserDtos.UserProfileResponse getProfileById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Потребителят не е намерен: " + id));

        return mapToResponse(user);
    }

    @Transactional
    public UserDtos.UserProfileResponse updateProfile(String email, UserDtos.UpdateProfileRequest request){

        User user = findByEmail(email);

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        return mapToResponse(userRepository.save(user));

    }

    @Transactional
    public String uploadAvatar(String email,MultipartFile file){
        validateImageFile(file);

        User user = findByEmail(email);

        String filename = "avatar_" +  user.getId() + "_"  + UUID.randomUUID() + getExtension(file);

        String url = saveFile(file,"avatars",filename);

        user.setAvatarUrl(url);
        userRepository.save(user);
        return url;

    }


    private String saveFile(MultipartFile file, String subDir, String filename){
        try {
            Path dir = Paths.get(uploadDir,subDir);
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(),target);
            return "/uploads/" + subDir + "/" + filename;
        } catch (IOException e) {
            throw new AppExceptions.BadRequestException("Грешка при запис на файл: " + e.getMessage());
        }
    }

    private void validateImageFile(MultipartFile file){
        if(file == null || file.isEmpty()){
            throw new AppExceptions.BadRequestException("Файлът е празен.");
        }

        String contentType = file.getContentType();

        if(contentType == null || !contentType.startsWith("image/")){
            throw new AppExceptions.BadRequestException("Само изображения са позволени.");
        }
    }

    private String getExtension(MultipartFile file){

        String original = file.getOriginalFilename();

        if(original == null || !original.contains(".")){
            return ".jpg";
        }
        return original.substring(original.lastIndexOf("."));
    }

    private User findByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Потребителят не е намерен: " + email));
    }

    private UserDtos.UserProfileResponse mapToResponse(User user){
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
}
