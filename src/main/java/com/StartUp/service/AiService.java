package com.StartUp.service;

import com.StartUp.dtos.ai.AiDtos;
import com.StartUp.dtos.chat.ChatDtos;
import com.StartUp.entity.*;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant for Breaddy — a platform that connects students with part-time jobs and gigs.
            About Breaddy:
            - Students can register, create profiles, upload CVs and apply for jobs
            - Employers can post jobs, review applications and hire students
            - Job types: Part-time, Full-time, One-day gigs
            - Categories: Restaurant, Shop, Event, Logistics, Promotion
            Your role: help users navigate the platform, answer questions about jobs and profiles.
            Always respond in the same language the user is writing in.
            """;

    public ChatDtos.ChatResponse chat(String email, ChatDtos.ChatRequest request) {
        User user = findUserByEmail(email);
        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", SYSTEM_PROMPT))));
        contents.add(Map.of("role", "model", "parts", List.of(Map.of("text", "Understood! I am the Breaddy assistant. How can I help you?"))));
        chatMessageRepository.findByUserIdOrderByCreatedAtAsc(user.getId()).forEach(msg ->
                contents.add(Map.of("role", msg.getRole(), "parts", List.of(Map.of("text", msg.getContent()))))
        );
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", request.message()))));
        String reply = callGemini(contents);
        chatMessageRepository.save(ChatMessage.builder().user(user).role("user").content(request.message()).build());
        chatMessageRepository.save(ChatMessage.builder().user(user).role("model").content(reply).build());
        return new ChatDtos.ChatResponse(reply);
    }

    public List<ChatDtos.ChatHistoryResponse> getHistory(String email) {
        User user = findUserByEmail(email);
        return chatMessageRepository.findByUserIdOrderByCreatedAtAsc(user.getId())
                .stream()
                .map(msg -> new ChatDtos.ChatHistoryResponse(msg.getId(), msg.getRole(), msg.getContent(), msg.getCreatedAt()))
                .toList();
    }

    public void clearHistory(String email) {
        chatMessageRepository.deleteByUserId(findUserByEmail(email).getId());
    }



    public AiDtos.JobMatchResponse getJobMatch(String email, Long jobId) {
        StudentProfile student = findStudentByEmail(email);
        Job job = findJobById(jobId);
        String prompt = """
                You are a recruitment expert. Analyze how well this student profile matches this job posting.

                STUDENT PROFILE:
                - University: %s
                - Major: %s
                - Skills: %s
                - Bio: %s
                - City: %s

                JOB POSTING:
                - Title: %s
                - Category: %s
                - Type: %s
                - Location: %s
                - Description: %s
                - Salary: %s

                Respond ONLY with a valid JSON object, no markdown, no explanation:
                {
                  "matchScore": <integer 0-100>,
                  "verdict": "<Strong Match | Good Match | Partial Match | Weak Match>",
                  "strengths": ["<strength>", "<strength>"],
                  "gaps": ["<gap>", "<gap>"],
                  "summary": "<2-3 sentence assessment>"
                }
                """.formatted(
                nvl(student.getUniversity()), nvl(student.getMajor()),
                nvl(student.getSkills()), nvl(student.getBio()), nvl(student.getCity()),
                job.getTitle(), job.getCategory(), job.getType(), job.getLocation(),
                nvl(job.getDescription()),
                job.getSalary() != null ? job.getSalary().toString() : "Not specified"
        );
        return parseJson(callGeminiSingleTurn(prompt), AiDtos.JobMatchResponse.class);
    }


    public AiDtos.CoverLetterResponse generateCoverLetter(String email, Long jobId) {
        StudentProfile student = findStudentByEmail(email);
        Job job = findJobById(jobId);
        String prompt = """
                Write a professional and enthusiastic cover letter for this student applying for this job.

                STUDENT:
                - Name: %s %s
                - University: %s
                - Major: %s
                - Skills: %s
                - Bio: %s

                JOB:
                - Title: %s
                - Company: %s
                - Category: %s
                - Description: %s

                Guidelines:
                - Under 300 words, 3 paragraphs: intro, why they fit, closing
                - Professional but warm tone, highlight the most relevant skills
                - No placeholders like [Your Address] — write a clean ready-to-use letter
                - Return only the cover letter text, no extra commentary.
                """.formatted(
                student.getUser().getFirstName(), student.getUser().getLastName(),
                nvl(student.getUniversity()), nvl(student.getMajor()),
                nvl(student.getSkills()), nvl(student.getBio()),
                job.getTitle(),
                job.getEmployer() != null ? job.getEmployer().getCompanyName() : "the company",
                job.getCategory(), nvl(job.getDescription())
        );
        return new AiDtos.CoverLetterResponse(callGeminiSingleTurn(prompt).trim());
    }


    public AiDtos.ProfileTipsResponse getProfileTips(String email) {
        StudentProfile s = findStudentByEmail(email);
        String prompt = """
                You are a career coach reviewing a student's job platform profile.
                Evaluate completeness and quality, then provide actionable tips.

                PROFILE:
                - Bio: %s
                - University: %s
                - Major: %s
                - Graduation Year: %s
                - Skills: %s
                - City: %s
                - CV uploaded: %s
                - LinkedIn: %s
                - GitHub: %s

                Respond ONLY with a valid JSON object, no markdown, no explanation:
                {
                  "profileStrength": <integer 0-100>,
                  "tips": [
                    {
                      "area": "<Bio | Skills | CV | LinkedIn | GitHub | University | City>",
                      "priority": "<HIGH | MEDIUM | LOW>",
                      "advice": "<specific actionable advice in 1-2 sentences>"
                    }
                  ]
                }
                Provide 3 to 6 tips. Focus on what will most help the student get hired.
                """.formatted(
                nvl(s.getBio()), nvl(s.getUniversity()), nvl(s.getMajor()),
                s.getGraduationYear() != null ? s.getGraduationYear().toString() : "Not set",
                nvl(s.getSkills()), nvl(s.getCity()),
                s.getCvUrl() != null ? "Yes" : "No",
                s.getLinkedinUrl() != null ? "Yes" : "No",
                s.getGithubUrl() != null ? "Yes" : "No"
        );
        return parseJson(callGeminiSingleTurn(prompt), AiDtos.ProfileTipsResponse.class);
    }


    public AiDtos.ApplicationScreeningResponse screenApplications(String email, Long jobId) {
        EmployerProfile employer = findEmployerByEmail(email);
        Job job = findJobById(jobId);

        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new AppExceptions.UnauthorizedException("You do not own this job posting");
        }

        List<Application> applications = applicationRepository
                .findAllByJob_Employer_Id(employer.getId(), Pageable.unpaged())
                .stream()
                .filter(a -> a.getJob().getId().equals(jobId))
                .collect(Collectors.toList());

        if (applications.isEmpty()) {
            return new AiDtos.ApplicationScreeningResponse(
                    List.of(), "No applications received for this job yet.");
        }

        StringBuilder block = new StringBuilder();
        for (Application app : applications) {
            StudentProfile sp = app.getStudent();
            block.append("APPLICATION_ID: ").append(app.getId()).append("\n");
            block.append("  Name: ").append(app.getFirstName()).append(" ").append(app.getLastName()).append("\n");
            block.append("  City: ").append(nvl(app.getCity())).append("\n");
            block.append("  Cover message: ").append(nvl(app.getMessageToCompany())).append("\n");
            block.append("  Has CV: ").append(app.getResumeUrl() != null ? "Yes" : "No").append("\n");
            if (sp != null) {
                block.append("  University: ").append(nvl(sp.getUniversity())).append("\n");
                block.append("  Major: ").append(nvl(sp.getMajor())).append("\n");
                block.append("  Skills: ").append(nvl(sp.getSkills())).append("\n");
                block.append("  Bio: ").append(nvl(sp.getBio())).append("\n");
            }
            block.append("\n");
        }

        String prompt = """
                You are a recruitment AI. Screen and rank these applicants for the job below.

                JOB:
                - Title: %s
                - Category: %s
                - Type: %s
                - Location: %s
                - Description: %s

                APPLICANTS:
                %s

                Respond ONLY with a valid JSON object, no markdown, no explanation:
                {
                  "rankedApplicants": [
                    {
                      "applicationId": <id>,
                      "fullName": "<name>",
                      "fitScore": <integer 0-100>,
                      "recommendation": "<Highly Recommended | Recommended | Consider | Skip>",
                      "reasoning": "<1-2 sentence explanation>"
                    }
                  ],
                  "summary": "<2-3 sentence overview of the applicant pool>"
                }
                Order by fitScore descending.
                """.formatted(
                job.getTitle(), job.getCategory(), job.getType(), job.getLocation(),
                nvl(job.getDescription()), block
        );
        return parseJson(callGeminiSingleTurn(prompt), AiDtos.ApplicationScreeningResponse.class);
    }


    private String callGemini(List<Map<String, Object>> contents) {
        Map<String, Object> body = Map.of("contents", contents);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = webClient.post()
                .uri("/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return extractText(response);
    }

    private String callGeminiSingleTurn(String userPrompt) {
        return callGemini(List.of(Map.of("role", "user", "parts", List.of(Map.of("text", userPrompt)))));
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        return (String) parts.get(0).get("text");
    }

    private <T> T parseJson(String raw, Class<T> clazz) {
        try {
            String cleaned = raw.replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();
            return objectMapper.readValue(cleaned, clazz);
        } catch (Exception e) {
            throw new AppExceptions.BadRequestException("Failed to parse AI response: " + e.getMessage());
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));
    }

    private StudentProfile findStudentByEmail(String email) {
        return studentProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Student profile not found"));
    }

    private EmployerProfile findEmployerByEmail(String email) {
        return employerProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer profile not found"));
    }

    private Job findJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Job not found"));
    }

    private String nvl(String value) {
        return value != null && !value.isBlank() ? value : "Not provided";
    }
}
