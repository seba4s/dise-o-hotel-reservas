package com.hotel.reservation.service;

import com.hotel.reservation.model.Room;
import com.hotel.reservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for room management operations (admin use)
 * Note: Room availability queries are handled by AvailabilityService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {
    
    private final RoomRepository roomRepository;
    
    /**
     * Get all rooms (admin operation)
     */
    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        log.info("Getting all rooms");
        return roomRepository.findAll();
    }
    
    /**
     * Get room by ID
     */
    @Transactional(readOnly = true)
    public Optional<Room> getRoomById(String roomId) {
        log.info("Getting room by ID: {}", roomId);
        return roomRepository.findById(roomId);
    }
    
    /**
     * Get room by room number
     */
    @Transactional(readOnly = true)
    public Optional<Room> getRoomByRoomNumber(String roomNumber) {
        log.info("Getting room by room number: {}", roomNumber);
        return roomRepository.findByRoomNumber(roomNumber);
    }
    
    /**
     * Save or update room (admin operation)
     */
    @Transactional
    public Room saveRoom(Room room) {
        log.info("Saving room: {}", room.getRoomNumber());
        return roomRepository.save(room);
    }
    
    /**
     * Delete room (admin operation)
     */
    @Transactional
    public void deleteRoom(String roomId) {
        log.info("Deleting room with ID: {}", roomId);
        roomRepository.deleteById(roomId);
    }
    
    /**
     * Update room status (admin/staff operation)
     */
    @Transactional
    public void updateRoomStatus(String roomId, Room.RoomStatus status) {
        log.info("Updating room {} status to: {}", roomId, status);
        roomRepository.findById(roomId).ifPresent(room -> {
            room.setStatus(status);
            roomRepository.save(room);
        });
    }
}
