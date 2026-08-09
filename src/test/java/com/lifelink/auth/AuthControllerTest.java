package com.lifelink.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.auth.dto.AuthRequest;
import com.lifelink.auth.dto.RegisterRequest;
import com.lifelink.bloodchain.BloodChainService;
import com.lifelink.donor.DonorRepository;
import com.lifelink.user.Role;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DonorRepository donorRepository;
    
    @MockBean
    private com.lifelink.bloodbank.BloodBankRepository bloodBankRepository;
    
    @MockBean
    private com.lifelink.ngo.NgoRepository ngoRepository;

    @MockBean
    private BloodChainService bloodChainService;

    @MockBean
    private OtpService otpService;

    @MockBean
    private FirebaseAuthService firebaseAuthService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testRegisterNewUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setPhone("1234567890");
        request.setPassword("password123");
        request.setRole(Role.REQUESTER);

        when(userRepository.findByPhone("1234567890")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testRegisterUserAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setPhone("1234567890");
        request.setPassword("password123");
        request.setRole(Role.REQUESTER);

        User existingUser = new User();
        existingUser.setPhone("1234567890");

        when(userRepository.findByPhone("1234567890")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
