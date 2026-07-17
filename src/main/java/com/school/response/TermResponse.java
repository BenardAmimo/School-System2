package com.school.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TermResponse {
    private Long termId;
    private String name;
    private String year;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
