
package com.school.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.tool.schema.internal.StandardUserDefinedTypeExporter;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "courses_tbl"
)
public class Course {
 @Id
 @SequenceGenerator(
         name = "course_gen",
         sequenceName = "course_gen",
         allocationSize = 1
 )
 @GeneratedValue(
         strategy = GenerationType.SEQUENCE,
 generator = "course_gen"
 )
 private Long courseId;
 private String name;
 private String description;
 @OneToMany(
         mappedBy = "course",
         cascade = CascadeType.ALL,
         orphanRemoval = true
 )
 private List<Assignment> assignment;
 @ManyToOne(
         cascade = CascadeType.ALL
 )
 @JoinColumn(
         name = "school_id",
         referencedColumnName = "schoolId"
 )
 private School school;

}
