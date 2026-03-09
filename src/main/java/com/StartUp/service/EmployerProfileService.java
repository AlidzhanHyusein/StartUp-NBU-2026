package com.StartUp.service;

import com.StartUp.dtos.employer.EmployerDtos;
import com.StartUp.entity.Application;
import com.StartUp.entity.EmployerProfile;
import com.StartUp.entity.Job;
import com.StartUp.entity.User;
import com.StartUp.enums.ApplicationStatus;
import com.StartUp.enums.Role;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.ApplicationRepository;
import com.StartUp.repository.EmployerProfileRepository;
import com.StartUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployerProfileService {

    private final EmployerProfileRepository employerProfileRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public EmployerDtos.EmployerProfileResponse getMyProfile(String email){
        User user = getUser(email);

        return mapToResponse(getProfile(user.getId()));
    }

    @Transactional(readOnly = true)
    public EmployerDtos.EmployerProfileResponse getProfileByUserId(Long userId){
        return mapToResponse(getProfile(userId));
    }

    @Transactional
    public EmployerDtos.EmployerProfileResponse updateProfile(String email,EmployerDtos.UpdateEmployerProfileRequest request){
        User user = getUser(email);

        EmployerProfile profile = getProfile(user.getId());

        if (request.companyName() != null) {
            profile.setCompanyName(request.companyName());
            profile.setDescription(request.description());
            profile.setWebsite(request.website());
            profile.setPhone(request.phone());
            profile.setCity(request.city());
            profile.setCountry(request.country());
            profile.setCompanySize(request.companySize());
            profile.setIndustry(request.industry());
        }

        return mapToResponse(employerProfileRepository.save(profile));
    }

    @Transactional
    public String uploadLogo(String email,MultipartFile file){
        if(file == null || file.isEmpty()){
            throw new AppExceptions.BadRequestException("Файлът е празен");
        }
        String contentType = file.getContentType();

        if(contentType == null || !contentType.startsWith("image/")){
            throw new AppExceptions.BadRequestException("Само изображения се ползват за лого");
        }
        User user = getUser(email);
        EmployerProfile profile = getProfile(user.getId());

        String filename = "logo_" +  user.getId() + "_" + UUID.randomUUID() + getExtension(file);

        String url = saveFile(file,"logos",filename);

        profile.setLogoUrl(url);

        employerProfileRepository.save(profile);
        return url;
    }

    private User getUser(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Потребителят не е намерен"));

        if(user.getRole() != Role.EMPLOYER){
            throw new AppExceptions.BadRequestException("Само работодатели имат employer профил.");
        }

        return user;
    }



    private EmployerProfile getProfile(Long userId){
        return employerProfileRepository.findByUserId(userId).orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer профил не е намерен"));
    }

    private String saveFile(MultipartFile file,String subDir,String filename){
        try {
            Path dir = Paths.get(uploadDir, subDir);
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename));
            return "/uploads/" + subDir + "/" + filename;
        }catch (IOException e) {
            throw new AppExceptions.BadRequestException("Грешка при запис на файл.");
        }
    }

    private String getExtension(MultipartFile file){
        String original = file.getOriginalFilename();
        if(original == null || !original.contains(".")){
            return ".jpg";
        }
        return original.substring(original.lastIndexOf("."));
    }

    private EmployerDtos.EmployerProfileResponse mapToResponse(EmployerProfile p){
        User user = p.getUser();
        return new EmployerDtos.EmployerProfileResponse(
                p.getId(),
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                p.getCompanyName(),
                p.getDescription(),
                p.getWebsite(),
                p.getPhone(),
                p.getCity(),
                p.getCountry(),
                p.getCompanySize(),
                p.getIndustry(),
                p.getLogoUrl(),
                p.getIsVerified(),
                p.getCreatedAt()
        );
    }
}
