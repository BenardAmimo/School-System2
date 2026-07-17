package com.school.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TermRequest {
    private String name;
    private String year;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
