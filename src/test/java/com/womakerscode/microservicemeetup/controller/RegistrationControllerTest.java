package com.womakerscode.microservicemeetup.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.womakerscode.microservicemeetup.controller.dto.RequisitionRegistrationDTO;
import com.womakerscode.microservicemeetup.controller.dto.ResponseRegistrationDTO;
import com.womakerscode.microservicemeetup.exception.BusinessException;
import com.womakerscode.microservicemeetup.model.entity.Meetup;
import com.womakerscode.microservicemeetup.model.entity.Registration;
import com.womakerscode.microservicemeetup.service.RegistrationService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class RegistrationControllerTest {
    static String REGISTRATION_API = "/api/registration";
    @Autowired
    MockMvc mockMvc;
    @MockBean
    RegistrationService registrationService;
    @MockBean
    RegistrationController registrationController;

    @Test
    @DisplayName("Should create a registration with success")
    public void testCreateRegistration() throws Exception {

        // cenario
        RequisitionRegistrationDTO requisitionRegistrationDTO = this.createNewRegistration();
        Meetup meetup = this.createNewMeetup();
        String message = "Created Registration of " + requisitionRegistrationDTO.getName() + " to meetup " + meetup.getMeetupName();


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        String json = objectMapper.writeValueAsString(requisitionRegistrationDTO);

        // execucao
        BDDMockito.given(registrationController.create(requisitionRegistrationDTO)).willReturn(ResponseEntity.status(HttpStatus.CREATED).body(message));
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(REGISTRATION_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        // verificacao, assert....
        mockMvc
                .perform(request);


    }

    @Test
    @DisplayName("Should throw an exception when not have date enough for the test.")
    public void createInvalidRegistrationTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(new ResponseRegistrationDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(REGISTRATION_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);


        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Should throw an exception when try to create a new registration with an registration already created.")
    public void createRegistrationWithDuplicatedRegistration() throws Exception {

        RequisitionRegistrationDTO dto = createNewRegistration();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        String json = objectMapper.writeValueAsString(dto);

        BDDMockito.given(registrationController.create(dto))
                .willThrow(new BusinessException("Registration already created!"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(REGISTRATION_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Registration already created!"));
    }

    @Test
    @DisplayName("Should get registration informations")
    public void getRegistrationTest() throws Exception {

        Integer id = 11;

        ResponseRegistrationDTO registration = ResponseRegistrationDTO.builder()
                .id(id)
                .name(createNewRegistration().getName())
                .dateOfRegistration(createNewRegistration().getDateOfRegistration())
                .nickName(createNewRegistration().getNickName()).build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();


        BDDMockito.given(registrationController.get(id)).willReturn(registration);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(REGISTRATION_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("name").value(createNewRegistration().getName()))
                .andExpect(jsonPath("dateOfRegistration").value(createNewRegistration().getDateOfRegistration().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                .andExpect(jsonPath("nickName").value(createNewRegistration().getNickName()));

    }


    @Test
    @DisplayName("Should delete the registration")
    public void deleteRegistration() throws Exception {

        BDDMockito.given(registrationService
                .getRegistrationById(anyInt()))
                .willReturn(Optional.of(Registration.builder().id(11).build()));

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .delete(REGISTRATION_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());
    }



    private Meetup createNewMeetup() {
        return Meetup.builder().meetupName("Spring boot Bootcamp").meetupDate(LocalDate.now()).registrated(true).build();
    }

    private RequisitionRegistrationDTO createNewRegistration() {
        return RequisitionRegistrationDTO.builder().name("Ana Neri").dateOfRegistration(LocalDate.now()).nickName("001").meetupId(createNewMeetup().getId()).build();
    }
}
