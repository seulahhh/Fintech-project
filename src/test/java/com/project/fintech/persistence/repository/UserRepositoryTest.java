package com.project.fintech.persistence.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.project.fintech.TestConfig;
import com.project.fintech.builder.UserTestDataBuilder;
import com.project.fintech.persistence.entity.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void testSaveUser() {
        //given
        String email = "repoTest@test.com";
        User user = new UserTestDataBuilder().withEmail(email).build();
        //when
        User savedUser = userRepository.save(user);
        //then
        assertThat(savedUser).isEqualTo(user);
        assertThat(savedUser.getEmail()).isEqualTo(email);
    }
}