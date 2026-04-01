package com.mjs.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjs.ecommerce.model.User;
import com.mjs.ecommerce.service.UserServiceI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceI ui;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------
    // Helper Method
    // -------------------------
    private User mockUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        return user;
    }

    // -------------------------
    // CREATE USER
    // -------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void create_success() throws Exception {

        when(ui.createUser(any(User.class)))
                .thenReturn(mockUser());

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUser())))
                .andExpect(status().isForbidden());
    }

    // -------------------------
    // GET ALL USERS
    // -------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_success() throws Exception {

        when(ui.getAllUsers())
                .thenReturn(List.of(mockUser()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    // -------------------------
    // GET USER BY ID
    // -------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_success() throws Exception {

        when(ui.getUserById(anyLong()))
                .thenReturn(mockUser());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());
    }

    // -------------------------
    // UPDATE USER
    // -------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void update_success() throws Exception {

        when(ui.updateUser(anyLong(), any(User.class)))
                .thenReturn(mockUser());

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUser())))
                .andExpect(status().isForbidden());
    }

    // -------------------------
    // DELETE USER
    // -------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_success() throws Exception {

        doNothing().when(ui).deleteUser(anyLong());

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    // -------------------------
    // UNAUTHORIZED (No Login)
    // -------------------------
    @Test
    void create_unauthorized() throws Exception {

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // -------------------------
    // FORBIDDEN (Wrong Role)
    // -------------------------
    @Test
    @WithMockUser(roles = "USER")
    void create_wrongRole() throws Exception {

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}