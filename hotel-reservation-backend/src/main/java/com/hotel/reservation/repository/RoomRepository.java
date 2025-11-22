package com.hotel.reservation.repository;

import com.hotel.reservation.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Room entity
 * Handles availability queries for HU001 and room management
 */
@Repository
public interface RoomRepository extends MongoRepository<Room, String> {

    /**
     * Basic room queries
     */
    Optional<Room> findByRoomNumber(String roomNumber);
    
    Boolean existsByRoomNumber(String roomNumber);
    
    List<Room> findByActiveTrueOrderByRoomNumber();
    
    /**
     * Availability queries for HU001 - Búsqueda de disponibilidad
     */
    
    /**
     * Find available rooms by basic criteria
     */
    @Query("{ 'active': true, 'status': 'AVAILABLE' }")
    List<Room> findAvailableRooms();
    
    /**
     * Find rooms by type and availability
     */
    @Query("{ 'roomType': ?0, 'active': true, 'status': 'AVAILABLE' }")
    List<Room> findAvailableRoomsByType(String roomType);
    
    /**
     * Find rooms with capacity >= required guests
     */
    @Query("{ 'capacity': { $gte: ?0 }, 'active': true, 'status': 'AVAILABLE' }")
    List<Room> findAvailableRoomsByMinCapacity(Integer minCapacity);
    
    /**
     * Find rooms by type and minimum capacity
     */
    @Query("{ " +
           "'roomType': ?0, " +
           "'capacity': { $gte: ?1 }, " +
           "'active': true, " +
           "'status': 'AVAILABLE' " +
           "}")
    List<Room> findAvailableRoomsByTypeAndCapacity(String roomType, Integer minCapacity);
    
    /**
     * Find rooms within price range
     */
    @Query("{ " +
           "'basePrice': { $gte: ?0, $lte: ?1 }, " +
           "'active': true, " +
           "'status': 'AVAILABLE' " +
           "}")
    List<Room> findAvailableRoomsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Complex availability query with all filters
     */
    @Query("{ " +
           "'active': true, " +
           "'status': 'AVAILABLE', " +
           "'capacity': { $gte: ?0 }, " +
           "'roomType': { $regex: ?1, $options: 'i' }, " +
           "'basePrice': { $gte: ?2, $lte: ?3 } " +
           "}")
    List<Room> findAvailableRoomsWithFilters(
        Integer minCapacity, 
        String roomType, 
        BigDecimal minPrice, 
        BigDecimal maxPrice
    );
    
    /**
     * Find rooms with specific amenities
     */
    @Query("{ 'amenities': { $in: ?0 }, 'active': true, 'status': 'AVAILABLE' }")
    List<Room> findAvailableRoomsWithAmenities(List<String> requiredAmenities);
    
    /**
     * Find rooms with accessibility features
     */
    @Query("{ 'accessible': true, 'active': true, 'status': 'AVAILABLE' }")
    List<Room> findAccessibleAvailableRooms();
    
    /**
     * Room status management queries
     */
    List<Room> findByStatus(Room.RoomStatus status);
    
    @Query("{ 'status': { $in: ?0 } }")
    List<Room> findByStatusIn(List<Room.RoomStatus> statuses);
    
    /**
     * Maintenance and housekeeping queries
     */
    @Query("{ 'status': 'MAINTENANCE' }")
    List<Room> findRoomsUnderMaintenance();
    
    @Query("{ 'status': 'CLEANING' }")
    List<Room> findRoomsBeingCleaned();
    
    @Query("{ 'status': 'OUT_OF_ORDER' }")
    List<Room> findOutOfOrderRooms();
    
    @Query("{ 'nextScheduledMaintenance': { $lte: ?0 } }")
    List<Room> findRoomsDueForMaintenance(LocalDateTime date);
    
    /**
     * Room type and category queries
     */
    @Query("{ 'roomType': { $regex: ?0, $options: 'i' }, 'active': true }")
    List<Room> findByRoomTypeLike(String roomType);
    
    List<Room> findByRoomTypeAndActiveTrue(String roomType);
    
    /**
     * Floor and location queries
     */
    @Query("{ 'floor': ?0, 'active': true }")
    List<Room> findByFloor(Integer floor);
    
    @Query("{ 'view': { $regex: ?0, $options: 'i' }, 'active': true }")
    List<Room> findByView(String view);
    
    /**
     * Bed type queries
     */
    @Query("{ 'bedType': ?0, 'active': true, 'status': 'AVAILABLE' }")
    List<Room> findAvailableRoomsByBedType(String bedType);
    
    /**
     * Pet and smoking policy queries
     */
    @Query("{ 'petFriendly': true, 'active': true, 'status': 'AVAILABLE' }")
    List<Room> findPetFriendlyAvailableRooms();
    
    @Query("{ 'smokingAllowed': ?0, 'active': true, 'status': 'AVAILABLE' }")
    List<Room> findAvailableRoomsBySmokingPolicy(Boolean smokingAllowed);
    
    /**
     * Size and features queries
     */
    @Query("{ 'size': { $gte: ?0 }, 'active': true, 'status': 'AVAILABLE' }")
    List<Room> findAvailableRoomsByMinSize(Integer minSize);
    
    @Query("{ 'hasBalcony': true, 'active': true, 'status': 'AVAILABLE' }")
    List<Room> findAvailableRoomsWithBalcony();
    
    @Query("{ 'hasKitchenette': true, 'active': true, 'status': 'AVAILABLE' }")
    List<Room> findAvailableRoomsWithKitchenette();
    
    /**
     * Price-based queries for different room categories
     */
    @Query("{ 'basePrice': { $lt: ?0 }, 'active': true, 'status': 'AVAILABLE' }")
    List<Room> findEconomyRooms(BigDecimal maxPrice);
    
    @Query("{ " +
           "'basePrice': { $gte: ?0, $lt: ?1 }, " +
           "'active': true, " +
           "'status': 'AVAILABLE' " +
           "}")
    List<Room> findStandardRooms(BigDecimal minPrice, BigDecimal maxPrice);
    
    @Query("{ " +
           "'basePrice': { $gte: ?0, $lt: ?1 }, " +
           "'active': true, " +
           "'status': 'AVAILABLE' " +
           "}")
    List<Room> findDeluxeRooms(BigDecimal minPrice, BigDecimal maxPrice);
    
    @Query("{ 'basePrice': { $gte: ?0 }, 'active': true, 'status': 'AVAILABLE' }")
    List<Room> findLuxuryRooms(BigDecimal minPrice);
    
    /**
     * Aggregation queries for statistics and reporting
     */
    @Aggregation(pipeline = {
        "{ $match: { 'active': true } }",
        "{ $group: { _id: '$status', count: { $sum: 1 } } }"
    })
    List<RoomStatusStatsProjection> getRoomStatusStatistics();
    
    @Aggregation(pipeline = {
        "{ $match: { 'active': true } }",
        "{ $group: { _id: '$roomType', count: { $sum: 1 }, avgPrice: { $avg: '$basePrice' } } }",
        "{ $sort: { 'avgPrice': 1 } }"
    })
    List<RoomTypeStatsProjection> getRoomTypeStatistics();
    
    @Aggregation(pipeline = {
        "{ $match: { 'active': true, 'status': 'AVAILABLE' } }",
        "{ $group: { " +
        "    _id: { " +
        "      $switch: { " +
        "        branches: [ " +
        "          { case: { $lt: ['$basePrice', 300000] }, then: 'ECONOMY' }, " +
        "          { case: { $lt: ['$basePrice', 500000] }, then: 'STANDARD' }, " +
        "          { case: { $lt: ['$basePrice', 800000] }, then: 'DELUXE' } " +
        "        ], " +
        "        default: 'LUXURY' " +
        "      } " +
        "    }, " +
        "    count: { $sum: 1 }, " +
        "    avgPrice: { $avg: '$basePrice' }, " +
        "    minPrice: { $min: '$basePrice' }, " +
        "    maxPrice: { $max: '$basePrice' } " +
        "  } }"
    })
    List<RoomCategoryStatsProjection> getAvailableRoomsByPriceCategory();
    
    /**
     * Complex search query for availability with multiple criteria
     */
              @Query("""
                            {
                                   'active': true,
                                   'status': 'AVAILABLE',
                                   '$and': [
                                          { 'capacity': { $gte: ?0 } },
                                          {
                                                 '$or': [
                                                        { 'roomType': { $regex: ?1, $options: 'i' } },
                                                        { ?1: null }
                                                 ]
                                          },
                                          {
                                                 '$or': [
                                                        { 'basePrice': { $gte: ?2, $lte: ?3 } },
                                                        { '$and': [ { ?2: null }, { ?3: null } ] }
                                                 ]
                                          },
                                          {
                                                 '$or': [
                                                        { 'amenities': { $in: ?4 } },
                                                        { ?4: null }
                                                 ]
                                          }
                                   ]
                            }
                            """)
    List<Room> searchAvailableRooms(
        Integer minCapacity,
        String roomType,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<String> amenities
    );
    
    /**
     * Projection interfaces for aggregation results
     */
    interface RoomStatusStatsProjection {
        String getId(); // Status
        Long getCount();
    }
    
    interface RoomTypeStatsProjection {
        String getId(); // Room type
        Long getCount();
        Double getAvgPrice();
    }
    
    interface RoomCategoryStatsProjection {
        String getId(); // Price category
        Long getCount();
        Double getAvgPrice();
        Double getMinPrice();
        Double getMaxPrice();
    }
    
    /**
     * Maintenance and operational queries
     */
    @Query("{ 'lastMaintenance': { $lt: ?0 } }")
    List<Room> findRoomsNotMaintainedSince(LocalDateTime date);
    
    @Query("{ " +
           "'maintenanceHistory': { " +
           "  $elemMatch: { " +
           "    'type': 'EMERGENCY', " +
           "    'completedDate': { $gte: ?0 } " +
           "  } " +
           "} " +
           "}")
    List<Room> findRoomsWithRecentEmergencyMaintenance(LocalDateTime since);
    
    /**
     * Room search for specific guest preferences
     */
    @Query("{ " +
           "'active': true, " +
           "'status': 'AVAILABLE', " +
           "'capacity': { $gte: ?0 }, " +
           "'floor': { $gte: ?1 }, " +
           "'view': { $regex: ?2, $options: 'i' } " +
           "}")
    List<Room> findRoomsWithPreferences(Integer minCapacity, Integer minFloor, String preferredView);
    
    /**
     * Find similar rooms (for alternative suggestions)
     */
    @Query("{ " +
           "'active': true, " +
           "'status': 'AVAILABLE', " +
           "'roomType': ?0, " +
           "'id': { $ne: ?1 } " +
           "}")
    List<Room> findSimilarAvailableRooms(String roomType, String excludeRoomId);
    
    /**
     * Paginated availability search
     */
    @Query("{ " +
           "'active': true, " +
           "'status': 'AVAILABLE', " +
           "'capacity': { $gte: ?0 } " +
           "}")
    Page<Room> findAvailableRoomsPageable(Integer minCapacity, Pageable pageable);
}