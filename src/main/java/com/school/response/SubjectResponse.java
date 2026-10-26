package com.school.response;

import com.school.entity.Subject;

public record SubjectResponse (
        Long subjectId,
        String name,
        String description,
        //private String classesName;
        int assignmentCount
){
   public static SubjectResponse from(Subject subject){
       return new SubjectResponse(
               subject.getSubjectId(),
               subject.getName(),
               subject.getDescription(),
               subject.getAssignment() != null ? subject.getAssignment().size() : 0
       );
   }

}
