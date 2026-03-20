import java.util.*;
class AddOnService {

    private String serviceName;
    private double cost;

    public AddOnService(String serviceName, double cost) {
        this.serviceName = serviceName;
        this.cost = cost;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getCost() {
        return cost;
    }
}

class AddOnServiceManager {

    private Map<String, List<AddOnService>> servicesByReservation;

    public AddOnServiceManager() {
        servicesByReservation = new HashMap<>();
    }

    public void addService(String reservationId, AddOnService service) {
        // If the ID isn't in the map, create a new list; then add the service
        servicesByReservation
                .computeIfAbsent(reservationId, k -> new ArrayList<>())
                .add(service);
    }

    public double calculateTotalServiceCost(String reservationId) {
        List<AddOnService> services = servicesByReservation.get(reservationId);
        if (services == null) {
            return 0.0;
        }

        double total = 0;
        for (AddOnService s : services) {
            total += s.getCost();
        }
        return total;
    }
}

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
        // Initialize the manager
        AddOnServiceManager manager = new AddOnServiceManager();

        // Define a Reservation ID (from Use Case 6)
        String resId = "Single-1";

        // Define available services
        AddOnService breakfast = new AddOnService("Breakfast", 500.0);
        AddOnService wifi = new AddOnService("High-Speed WiFi", 300.0);
        AddOnService gym = new AddOnService("Gym Access", 700.0);

        // Guest selects services
        manager.addService(resId, breakfast);
        manager.addService(resId, wifi);
        manager.addService(resId, gym);

        // Output results as shown in the screenshot
        System.out.println("Add-On Service Selection");
        System.out.println("Reservation ID: " + resId);

        double totalCost = manager.calculateTotalServiceCost(resId);
        System.out.println("Total Add-On Cost: " + totalCost);
    }
    }

