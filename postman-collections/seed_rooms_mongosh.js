// seed_rooms_mongosh.js
// Usage: mongosh "<CONNECTION_URI>/hotel_reservations" seed_rooms_mongosh.js

const rooms = [
  {
    roomNumber: "101",
    roomType: "STANDARD",
    capacity: 2,
    size: 20,
    basePrice: NumberDecimal("400000"),
    description: "Habitación Standard de prueba",
    amenities: ["WiFi", "TV"],
    photos: [],
    status: "AVAILABLE",
    floor: 1,
    view: "City",
    smokingAllowed: false,
    petFriendly: false,
    accessible: true,
    hasBalcony: false,
    hasKitchenette: false,
    active: true,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    roomNumber: "102",
    roomType: "DOUBLE",
    capacity: 4,
    size: 30,
    basePrice: NumberDecimal("600000"),
    description: "Habitación Double de prueba",
    amenities: ["WiFi", "TV", "MiniBar"],
    photos: [],
    status: "AVAILABLE",
    floor: 1,
    view: "Garden",
    smokingAllowed: false,
    petFriendly: false,
    accessible: false,
    hasBalcony: false,
    hasKitchenette: false,
    active: true,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    roomNumber: "201",
    roomType: "SUITE",
    capacity: 6,
    size: 60,
    basePrice: NumberDecimal("1200000"),
    description: "Suite de prueba",
    amenities: ["WiFi", "TV", "Jacuzzi"],
    photos: [],
    status: "AVAILABLE",
    floor: 2,
    view: "Ocean",
    smokingAllowed: false,
    petFriendly: true,
    accessible: false,
    hasBalcony: true,
    hasKitchenette: true,
    active: true,
    createdAt: new Date(),
    updatedAt: new Date()
  }
];

rooms.forEach(r => {
  // Upsert by roomNumber to avoid duplicates
  const filter = { roomNumber: r.roomNumber };
  const update = { $set: r };
  const opts = { upsert: true };
  db.rooms.updateOne(filter, update, opts);
});

print('Seed finished: inserted/updated ' + rooms.length + ' rooms.');
