import java.util.*;

abstract class Room {

    protected int numberOfBeds;
    protected int squareFeet;
    protected double pricePerNight;

    public Room(int numberOfBeds, int squareFeet, double pricePerNight) {
        this.numberOfBeds = numberOfBeds;
        this.squareFeet = squareFeet;
        this.pricePerNight = pricePerNight;
    }

    public void displayRoomDetails() {
        System.out.println("Beds: " + numberOfBeds);
        System.out.println("Size: " + squareFeet + " sqft");
        System.out.println("Price per night: " + pricePerNight);
    }
}

// Room Types
class SingleRoom extends Room {
    public SingleRoom() {
        super(1, 250, 1500.0);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super(2, 400, 2500.0);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super(3, 750, 5000.0);
    }
}

// Inventory Class
class RoomInventory {

    private Map<String, Integer> roomAvailability;

    // Constructor
    public RoomInventory() {
        roomAvailability = new HashMap<>();
        initializeInventory();
    }

    // Initialize default availability
    private void initializeInventory() {
        roomAvailability.put("Single", 5);
        roomAvailability.put("Double", 3);
        roomAvailability.put("Suite", 2);
    }

    // Get availability
    public Map<String, Integer> getRoomAvailability() {
        return roomAvailability;
    }

    // Update availability
    public void updateAvailability(String roomType, int count) {
        roomAvailability.put(roomType, count);
    }
}
public class BookMyStayApp {
    public static void main(String[] args) {

        // Create room objects
        Room single = new SingleRoom();
        Room doubleroom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Initialize inventory
        RoomInventory inventory = new RoomInventory();

        Map<String, Integer> availability = inventory.getRoomAvailability();

        System.out.println("Hotel Room Inventory Status\n");

        System.out.println("Single Room:");
        single.displayRoomDetails();
        System.out.println("Available Rooms: " + availability.get("Single") + "\n");

        System.out.println("Double Room:");
        doubleroom.displayRoomDetails();
        System.out.println("Available Rooms: " + availability.get("Double") + "\n");

        System.out.println("Suite Room:");
        suite.displayRoomDetails();
        System.out.println("Available Rooms: " + availability.get("Suite"));
    }
}
