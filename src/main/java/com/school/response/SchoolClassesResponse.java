package com.school.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchoolClassesResponse {
    private Long classesId;
    private String location;
    private String name;
    private String year;
    private int studentsCount;
    //private String studentsFirstName;
    //private String studentsLastName;

}
