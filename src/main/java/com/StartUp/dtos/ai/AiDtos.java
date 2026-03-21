package com.StartUp.dtos.ai;

import java.util.List;

public class AiDtos {


    public record JobMatchResponse(
            int matchScore,
            String verdict,
            List<String> strengths,
            List<String> gaps,
            String summary
    ) {}

    public record CoverLetterResponse(
            String coverLetter
    ) {}

    public record ProfileTipsResponse(
            int profileStrength,
            List<ProfileTip> tips
    ) {}

    public record ProfileTip(
            String area,
            String priority,
            String advice
    ) {}

    public record ApplicationScreeningResponse(
            List<RankedApplicant> rankedApplicants,
            String summary
    ) {}

    public record RankedApplicant(
            Long applicationId,
            String fullName,
            int fitScore,
            String recommendation,
            String reasoning
    ) {}
}
