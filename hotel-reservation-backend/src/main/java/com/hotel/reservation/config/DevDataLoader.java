package com.hotel.reservation.config;

import com.hotel.reservation.model.Room;
import com.hotel.reservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DevDataLoader implements CommandLineRunner {

    private final RoomRepository roomRepository;

    @Value("${app.seed-on-start:false}")
    private boolean seedOnStart;

    @Override
    public void run(String... args) throws Exception {
        if (!seedOnStart) {
            log.debug("DevDataLoader: seedOnStart=false, skipping seeding");
            return;
        }

        long existing = roomRepository.count();
        if (existing > 0) {
            log.info("DevDataLoader: rooms collection already has {} documents — skipping seed", existing);
            return;
        }

        log.info("DevDataLoader: seeding example rooms into database...");

        Room r1 = Room.builder()
                .roomNumber("101")
                .roomType("STANDARD")
                .capacity(2)
                .size(20)
                .basePrice(BigDecimal.valueOf(400000))
                .description("Habitación Standard de prueba")
                .amenities(List.of("WiFi", "TV"))
                .photos(List.of())
                .status(Room.RoomStatus.AVAILABLE)
                .floor(1)
                .view("City")
                .smokingAllowed(false)
                .petFriendly(false)
                .accessible(true)
                .hasBalcony(false)
                .hasKitchenette(false)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Room r2 = Room.builder()
                .roomNumber("102")
                .roomType("DOUBLE")
                .capacity(4)
                .size(30)
                .basePrice(BigDecimal.valueOf(600000))
                .description("Habitación Double de prueba")
                .amenities(List.of("WiFi", "TV", "MiniBar"))
                .photos(List.of())
                .status(Room.RoomStatus.AVAILABLE)
                .floor(1)
                .view("Garden")
                .smokingAllowed(false)
                .petFriendly(false)
                .accessible(false)
                .hasBalcony(false)
                .hasKitchenette(false)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Room r3 = Room.builder()
                .roomNumber("201")
                .roomType("SUITE")
                .capacity(6)
                .size(60)
                .basePrice(BigDecimal.valueOf(1200000))
                .description("Suite de prueba")
                .amenities(List.of("WiFi", "TV", "Jacuzzi"))
                .photos(List.of())
                .status(Room.RoomStatus.AVAILABLE)
                .floor(2)
                .view("Ocean")
                .smokingAllowed(false)
                .petFriendly(true)
                .accessible(false)
                .hasBalcony(true)
                .hasKitchenette(true)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        roomRepository.saveAll(List.of(r1, r2, r3));

        log.info("DevDataLoader: seeded 3 rooms");
    }
}
