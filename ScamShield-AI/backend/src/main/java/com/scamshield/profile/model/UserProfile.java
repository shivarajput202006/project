package com.scamshield.profile.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_profiles")
public class UserProfile {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String phoneNumber;
    private String city;
    private String state;
    private String country;
    private String professionalBio;
    private List<Education> education = new ArrayList<>();
    private List<ProfileSkill> skills = new ArrayList<>();
    private String photoStorageId;
    private String photoContentType;
    private Instant updatedAt;

    protected UserProfile() {
    }

    public UserProfile(String userId) {
        this.userId = userId;
        this.updatedAt = Instant.now();
    }

    public void updateContact(String phoneNumber, String city, String state, String country, String bio) {
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.state = state;
        this.country = country;
        this.professionalBio = bio;
        this.updatedAt = Instant.now();
    }

    public void replaceEducation(List<Education> education) {
        this.education = new ArrayList<>(education);
        this.updatedAt = Instant.now();
    }

    public void replaceSkills(List<ProfileSkill> skills) {
        this.skills = new ArrayList<>(skills);
        this.updatedAt = Instant.now();
    }

    public void updatePhoto(String storageId, String contentType) {
        this.photoStorageId = storageId;
        this.photoContentType = contentType;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getCountry() { return country; }
    public String getProfessionalBio() { return professionalBio; }
    public List<Education> getEducation() { return education == null ? List.of() : List.copyOf(education); }
    public List<ProfileSkill> getSkills() { return skills == null ? List.of() : List.copyOf(skills); }
    public String getPhotoStorageId() { return photoStorageId; }
    public String getPhotoContentType() { return photoContentType; }
    public Instant getUpdatedAt() { return updatedAt; }
}
