package com.StartUp.service;

import com.StartUp.dtos.student.StudentDtos;
import com.StartUp.entity.StudentProfile;
import com.StartUp.entity.User;
import com.StartUp.enums.Role;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.StudentProfileRepository;
import com.StartUp.repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Service
public class StudentProfileService {

    private final Cloudinary cloudinary;

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public StudentDtos.StudentProfileResponse getMyProfile(String email){
        User user = getUser(email);

        StudentProfile profile = getProfile(user.getId());

        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public StudentDtos.StudentProfileResponse getProfileByUserId(Long userId) {
        return mapToResponse(getProfile(userId));
    }

    @Transactional

    public StudentDtos.StudentProfileResponse updateProfile(String email, StudentDtos.UpdateStudentProfileRequest request){
        User user = getUser(email);
        StudentProfile profile = getProfile(user.getId());

        profile.setBio(request.bio());
        profile.setUniversity(request.university());
        profile.setMajor(request.major());
        profile.setGraduationYear(request.graduationYear());
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setPhone(request.phone());
        profile.setCity(request.city());
        profile.setCountry(request.country());
        profile.setSkills(request.skills());
        profile.setLinkedinUrl(request.linkedinUrl());
        profile.setGithubUrl(request.githubUrl());

        return mapToResponse(studentProfileRepository.save(profile));

    }

    @Transactional
    public String uploadCv(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new AppExceptions.BadRequestException("Файлът е празен.");

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf")
                && !contentType.contains("word"))) {
            throw new AppExceptions.BadRequestException("Само PDF или Word файлове са позволени.");
        }

        User user = getUser(email);
        StudentProfile profile = getProfile(user.getId());

        String filename = "cv_" + user.getId() + "_" + UUID.randomUUID() + getExtension(file);
        String url = saveFile(file, "cv", filename);

        profile.setCvUrl(url);
        studentProfileRepository.save(profile);
        return url;
    }

    private User getUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Потребителят не е намерен"));
        if (user.getRole() != Role.STUDENT) {
            throw new AppExceptions.BadRequestException("Само студенти имат студентски профил.");
        }
        return user;
    }

    private StudentProfile getProfile(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Студентски профил не е намерен"));
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
            throw new AppExceptions.BadRequestException("Грешка при качване на файл.");
        }
    }


    private String getExtension(MultipartFile file){
        String original = file.getOriginalFilename();

        if(original == null || !original.contains(".")){
            return ".pdf";
        }

        return original.substring(original.lastIndexOf("."));
    }

    private StudentDtos.StudentProfileResponse mapToResponse(StudentProfile p) {
        User user = p.getUser();
        return new StudentDtos.StudentProfileResponse(
                p.getId(), user.getId(),
                user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getAvatarUrl(),
                p.getBio(), p.getUniversity(), p.getMajor(), p.getGraduationYear(),
                p.getDateOfBirth(), p.getPhone(), p.getCity(), p.getCountry(),
                p.getCvUrl(), p.getSkills(), p.getLinkedinUrl(), p.getGithubUrl(),
                p.getCreatedAt()
        );
    }
}
