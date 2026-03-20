import java.util.*;

class RoomAllocationService {

    private Set<String> allocatedRoomIds;

    private Map<String, Set<String>> assignedRoomsByType;

    public RoomAllocationService() {
        this.allocatedRoomIds = new HashSet<>();
        this.assignedRoomsByType = new HashMap<>();
    }

    public void allocateRoom(Reservation reservation, RoomInventory inventory) {
        String type = reservation.getRoomType();

        // 1. Check if inventory has rooms of this type
        if (inventory.getAvailableCount(type) > 0) {

            // 2. Generate a unique ID
            String roomId = generateRoomId(type);

            // 3. Update internal tracking (Set ensures uniqueness)
            allocatedRoomIds.add(roomId);

            // 4. Map room type to the specific assigned ID
            assignedRoomsByType.computeIfAbsent(type, k -> new HashSet<>()).add(roomId);

            // 5. Decrement the centralized inventory
            inventory.decrementInventory(type);

            System.out.println("Booking confirmed for Guest: " + reservation.getGuestName() +
                    ", Room ID: " + roomId);
        } else {
            System.out.println("Booking failed for " + reservation.getGuestName() + ": No " + type + " available.");
        }
    }

    private String generateRoomId(String roomType) {
        int currentCount = assignedRoomsByType.getOrDefault(roomType, new HashSet<>()).size();
        return roomType + "-" + (currentCount + 1);
    }
}
class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
}

class RoomInventory {
    private Map<String, Integer> counts = new HashMap<>();

    public void addRooms(String type, int count) { counts.put(type, count); }

    public int getAvailableCount(String type) { return counts.getOrDefault(type, 0); }

    public void decrementInventory(String type) {
        counts.put(type, counts.get(type) - 1);
    }
}

public class BookMyStayApp {
    public static void main(String[] args) {
        RoomInventory inventory = new RoomInventory();
        inventory.addRooms("Single", 10);
        inventory.addRooms("Suite", 5);

        RoomAllocationService allocationService = new RoomAllocationService();

        Queue<Reservation> requestQueue = new LinkedList<>();
        requestQueue.add(new Reservation("Abhi", "Single"));
        requestQueue.add(new Reservation("Subha", "Single"));
        requestQueue.add(new Reservation("Vanmathi", "Suite"));

        System.out.println("Room Allocation Processing");

        while (!requestQueue.isEmpty()) {
            Reservation request = requestQueue.poll();
            allocationService.allocateRoom(request, inventory);
        }
    }
    }

