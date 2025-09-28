package com.jobportal.dto;

import com.jobportal.model.JobApplication;

import java.time.LocalDate;

public class JobApplicationDTO {

    private int id;
    private String candidateName;
    private String candidateEmail;
    private String jobTitle;
    private String status;
    private LocalDate appliedDate;
    private String resume;  // filename

    public JobApplicationDTO() {}

    // Convert entity to DTO
    public static JobApplicationDTO fromEntity(JobApplication app) {
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setId(app.getId());
        dto.setCandidateName(app.getCandidate().getName());
        dto.setCandidateEmail(app.getCandidate().getEmail());
        dto.setJobTitle(app.getJob().getTitle());
        dto.setStatus(app.getStatus());
        dto.setAppliedDate(app.getAppliedDate());
        dto.setResume(app.getResume());  // just filename
        return dto;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }

    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }

}
