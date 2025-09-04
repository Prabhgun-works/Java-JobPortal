package com.jobportal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserRequest {

    @Min(value = 1, message = "id must be >= 1")
    private int id;

    @NotBlank(message = "name is required")
    private String name;

    @Email(message = "email must be valid")
    @NotBlank(message = "email is required")
    private String email;

    // Allow only two roles for now
    @Pattern(regexp = "jobseeker|employer", message = "role must be 'jobseeker' or 'employer'")
    @NotBlank(message = "role is required")
    private String role;

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
