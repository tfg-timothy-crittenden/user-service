package com.timcritt.tfg.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.port.inbound.UserUseCase;
import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.aggregate.user.PasswordHash;
import com.timcritt.tfg.domain.model.aggregate.user.User;
import com.timcritt.tfg.infrastructure.security.CustomUserDetailsService;
import com.timcritt.tfg.infrastructure.security.SecurityConfig;
import com.timcritt.tfg.infrastructure.web.controller.UserController;
import com.timcritt.tfg.infrastructure.web.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class UserControllerSecurityIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserUseCase userUseCase;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final PasswordHash passwordHash =
            PasswordHash.of("hashed-password");

    @Test
    void getTeachers_unauthenticated_returnsUnauthorized() throws Exception {
        mvc.perform(
                        get("/api/users/teachers")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTeachers_authenticatedWithoutAdmin_returnsForbidden() throws Exception {
        mvc.perform(
                        get("/api/users/teachers")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeachers_asAdmin_returnsOk() throws Exception {
        User teacher = User.rehydrate(
                1L,
                "t1",
                "T",
                "One",
                "t1@example.com",
                Set.of(Role.TEACHER),
                passwordHash,
                true
        );

        given(userUseCase.getAllUsersByRole(Role.TEACHER))
                .willReturn(List.of(teacher));

        mvc.perform(
                        get("/api/users/teachers")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    void patchUser_unauthenticated_returnsUnauthorized() throws Exception {
        mvc.perform(
                        patch("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void patchUser_authenticated_returnsOk() throws Exception {
        User updated = User.rehydrate(
                2L,
                "alice",
                "Alice",
                "A",
                "alice@example.com",
                Set.of(Role.STUDENT),
                passwordHash,
                true
        );

        given(userUseCase.updateUser(
                eq(2L),
                any(),
                any(),
                any(),
                any()
        )).willReturn(updated);

        UserDto dto = new UserDto();
        dto.setUsername("alice");
        dto.setName("Alice");
        dto.setSurname("A");
        dto.setEmail("alice@example.com");

        mvc.perform(
                        patch("/api/users/2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk());
    }
}