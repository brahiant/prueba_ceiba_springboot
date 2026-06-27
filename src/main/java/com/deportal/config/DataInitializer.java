package com.deportal.config;

import com.deportal.courts.entity.CourtEntity;
import com.deportal.courts.enums.SportType;
import com.deportal.courts.repository.CourtRepository;
import com.deportal.products.entity.ProductEntity;
import com.deportal.products.enums.ProductType;
import com.deportal.products.repository.ProductRepository;
import com.deportal.users.entity.UserEntity;
import com.deportal.users.enums.CustomerType;
import com.deportal.users.enums.UserRole;
import com.deportal.users.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedReferenceData(
            CourtRepository courtRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {
        return args -> {
            seedCourts(courtRepository);
            seedUsers(userRepository);
            seedProducts(productRepository);
        };
    }

    private void seedCourts(CourtRepository courtRepository) {
        if (courtRepository.count() > 0) {
            return;
        }

        courtRepository.save(new CourtEntity(
                "Cancha Central",
                SportType.FUTBOL,
                22,
                LocalTime.of(6, 0),
                LocalTime.of(22, 0),
                new BigDecimal("20.00"),
                true));
        courtRepository.save(new CourtEntity(
                "Cancha Norte",
                SportType.BASQUET,
                10,
                LocalTime.of(7, 0),
                LocalTime.of(21, 0),
                new BigDecimal("15.00"),
                true));
        courtRepository.save(new CourtEntity(
                "Cancha Sur",
                SportType.TENIS,
                4,
                LocalTime.of(8, 0),
                LocalTime.of(20, 0),
                new BigDecimal("25.00"),
                true));
        courtRepository.save(new CourtEntity(
                "Multiusos Este",
                SportType.MULTIUSOS,
                16,
                LocalTime.of(6, 0),
                LocalTime.of(22, 0),
                new BigDecimal("18.00"),
                true));
    }

    private void seedUsers(UserRepository userRepository) {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.save(new UserEntity(
                "Administrador Deportal",
                "admin@deportal.local",
                "pending-security-stage",
                CustomerType.MIEMBRO,
                UserRole.ADMIN,
                true));
        userRepository.save(new UserEntity(
                "Juan Perez",
                "juan.perez@deportal.local",
                "pending-security-stage",
                CustomerType.MIEMBRO,
                UserRole.USER,
                true));
        userRepository.save(new UserEntity(
                "Maria Garcia",
                "maria.garcia@deportal.local",
                "pending-security-stage",
                CustomerType.NO_MIEMBRO,
                UserRole.USER,
                true));
        userRepository.save(new UserEntity(
                "Carlos Lopez",
                "carlos.lopez@deportal.local",
                "pending-security-stage",
                CustomerType.MIEMBRO,
                UserRole.USER,
                true));
    }

    private void seedProducts(ProductRepository productRepository) {
        if (productRepository.count() > 0) {
            return;
        }

        productRepository.save(new ProductEntity(
                "Reserva de cancha",
                "Servicio base de alquiler por hora de cancha deportiva.",
                ProductType.COURT_RENTAL,
                BigDecimal.ZERO,
                true));
        productRepository.save(new ProductEntity(
                "Alquiler de balon",
                "Alquiler opcional de balon para la reserva.",
                ProductType.EQUIPMENT_RENTAL,
                new BigDecimal("3.00"),
                true));
        productRepository.save(new ProductEntity(
                "Bebida hidratante",
                "Producto opcional para usuarios durante la reserva.",
                ProductType.DRINK,
                new BigDecimal("2.50"),
                true));
    }
}
