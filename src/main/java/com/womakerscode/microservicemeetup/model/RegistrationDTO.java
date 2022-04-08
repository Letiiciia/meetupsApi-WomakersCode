package com.womakerscode.microservicemeetup.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationDTO {
    @NotEmpty
    private Integer id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String dateOfRegistration;
    @NotEmpty
    private String registration;
}