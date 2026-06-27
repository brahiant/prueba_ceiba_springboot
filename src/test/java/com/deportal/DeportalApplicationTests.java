package com.deportal;

import static org.assertj.core.api.Assertions.assertThat;

import com.deportal.courts.repository.CourtRepository;
import com.deportal.products.repository.ProductRepository;
import com.deportal.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DeportalApplicationTests {

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldSeedReferenceData() {
        assertThat(courtRepository.count()).isEqualTo(4);
        assertThat(userRepository.count()).isEqualTo(4);
        assertThat(productRepository.count()).isEqualTo(3);
    }
}
