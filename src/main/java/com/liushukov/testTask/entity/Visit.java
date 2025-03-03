package com.liushukov.testTask.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "visits")
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private Instant createdAt;
    @Column(name = "start_date_time", columnDefinition = "DATETIME")
    private LocalDateTime startDateTime;
    @Column(name = "end_date_time", columnDefinition = "DATETIME")
    private LocalDateTime endDateTime;
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    public Visit() {}

    public Integer getId() {
        return id;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public Visit setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
        return this;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public Visit setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
        return this;
    }

    public Patient getPatient() {
        return patient;
    }

    public Visit setPatient(Patient patient) {
        this.patient = patient;
        return this;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Visit setDoctor(Doctor doctor) {
        this.doctor = doctor;
        return this;
    }

    @Override
    public String toString() {
        return "Visit{" +
                "startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", patient=" + patient +
                ", doctor=" + doctor +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Visit visit = (Visit) o;
        return Objects.equals(id, visit.id) && Objects.equals(createdAt, visit.createdAt) && Objects.equals(startDateTime, visit.startDateTime) && Objects.equals(endDateTime, visit.endDateTime) && Objects.equals(patient, visit.patient) && Objects.equals(doctor, visit.doctor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt, startDateTime, endDateTime, patient, doctor);
    }
}
