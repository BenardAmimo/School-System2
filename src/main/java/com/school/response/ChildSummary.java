package com.school.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChildSummary {
    private Long studentId;
    private String firstName;
    private String lastName;
    private String className;
}
