package com.saksham.vitalmonitoring.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, unique = true)
    private String bedId;

    protected Patient() {}

    public Patient(String name, int age, String bedId) {
        this.name = name;
        this.age = age;
        this.bedId = bedId;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getBedId() { return bedId; }
}
