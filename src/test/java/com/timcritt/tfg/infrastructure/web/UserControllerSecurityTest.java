package com.timcritt.tfg.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.port.inbound.UserUseCase;
import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.domain.model.User;
import com.timcritt.tfg.infrastructure.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerSecurityTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserUseCase userUseCase;

    // Satisfy security config bean creation
    @MockitoBean
    CustomUserDetailsService customUserDetailsService;


    @Test
    void getTeachers_unauthenticated_returnsUnauthorized() throws Exception {
        mvc.perform(get("/api/users/teachers")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTeachers_authenticatedWithoutAdmin_returnsForbidden() throws Exception {
        mvc.perform(get("/api/users/teachers")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeachers_asAdmin_returnsOk() throws Exception {
        Role teacherRole = new Role(null, RoleType.TEACHER);
        User u = new User(1L, "t1", "T", "One", "t1@example.com", Set.of(teacherRole), null);
        given(userUseCase.getAllUsersByRoleType(eq(RoleType.TEACHER))).willReturn(List.of(u));

        mvc.perform(get("/api/users/teachers")).andExpect(status().isOk());
    }

    @Test
    void patchUser_unauthenticated_returnsUnauthorized() throws Exception {
        mvc.perform(patch("/api/users/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void patchUser_authenticated_returnsOk() throws Exception {
        Role studentRole = new Role(null, RoleType.STUDENT);
        User updated = new User(2L, "alice", "Alice", "A", "alice@example.com", Set.of(studentRole), null);
        given(userUseCase.updateUser(eq(2L), any(), any(), any(), any())).willReturn(updated);

        // minimal payload
        var dto = new com.timcritt.tfg.infrastructure.web.dto.UserDto();
        dto.setUsername("alice");
        dto.setName("Alice");
        dto.setSurname("A");
        dto.setEmail("alice@example.com");

        mvc.perform(patch("/api/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
}

