package com.school.money.entity.moneyEntities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "money_supplier")
@Getter @Setter
public class Supplier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String kraPin;
    private boolean active = true;
}