package com.StartUp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    private Long jobApplicationId;
    private Long reviewerId;
    private Long reviewedUserId;
    private Integer rating;
    private String comment;
}
