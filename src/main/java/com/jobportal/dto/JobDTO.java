package com.jobportal.dto;

import com.jobportal.model.Job;

public class JobDTO {
    private int id;
    private String title;
    private String description;
    private String location;
    private String employerName; // flat name for frontend
    private String postedDate; // yyyy-mm-dd or null

    public JobDTO() {}

    public static JobDTO fromEntity(Job job) {
        JobDTO d = new JobDTO();
        d.setId(job.getId());
        d.setTitle(job.getTitle());
        d.setDescription(job.getDescription());
        d.setLocation(job.getLocation());
        d.setEmployerName(job.getEmployer() != null ? job.getEmployer().getName() : null);
        d.setPostedDate(job.getPostedDate() != null ? job.getPostedDate().toString() : null);
        return d;
    }

    public Job toEntity() {
        Job j = new Job();
        j.setId(this.id);
        j.setTitle(this.title);
        j.setDescription(this.description);
        j.setLocation(this.location);
        // employer and postedDate set in controller/service where needed
        return j;
    }

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEmployerName() { return employerName; }
    public void setEmployerName(String employerName) { this.employerName = employerName; }
    public String getPostedDate() { return postedDate; }
    public void setPostedDate(String postedDate) { this.postedDate = postedDate; }
}
