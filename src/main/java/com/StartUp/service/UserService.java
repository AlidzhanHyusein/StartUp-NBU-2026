package com.StartUp.service;

import com.StartUp.dtos.user.UserDtos;
import com.StartUp.entity.User;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final Cloudinary cloudinary;


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

        String filename = "avatar_" +  user.getId() + "_"  + UUID.randomUUID();

        String url = saveFile(file,"avatars",filename);

        user.setAvatarUrl(url);
        userRepository.save(user);
        return url;

    }


    private String saveFile(MultipartFile file, String folder, String filename) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "public_id", filename,
                            "overwrite", true,
                            "resource_type", "auto"
                    )
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload to Cloudinary: " + e.getMessage());
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

    private String getExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return ".jpg";
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
