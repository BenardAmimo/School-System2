package com.school.response;


import com.school.entity.Assignment;
import com.school.entity.SchoolClasses;
import com.school.entity.Subject;
import com.school.entity.Teacher;

public record AssignmentResponse(Long assignmentId,
                                 String subjectName,
                                 String teacherName,
                                 String classesName
) {
   public static AssignmentResponse from(Assignment assignment){

       Subject subject = assignment.getSubject();
       Teacher teach = assignment.getTeacher();
       SchoolClasses classes = assignment.getClasses();

       return new AssignmentResponse(
               assignment.getAssignmentId(),
               subject.getName(),
               teach.getUserReg().getFirstName()+" "+teach.getUserReg().getLastName(),
               classes.getName()

       );
   }
}
