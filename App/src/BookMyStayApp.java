import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.*;
import java.util.List;

class CancellationService {

    private Stack<String> releasedRoomIds;

    private Map<String, String> reservationRoomTypeMap;

    public CancellationService() {
        this.releasedRoomIds = new Stack<>();
        this.reservationRoomTypeMap = new HashMap<>();
    }

    public void registerBooking(String reservationId, String roomType) {
        reservationRoomTypeMap.put(reservationId, roomType);
    }

    public void cancelBooking(String reservationId, RoomInventory inventory) {
        if (reservationRoomTypeMap.containsKey(reservationId)) {
            String roomType = reservationRoomTypeMap.get(reservationId);

            releasedRoomIds.push(reservationId);

            inventory.incrementInventory(roomType);

            reservationRoomTypeMap.remove(reservationId);

            System.out.println("Booking cancelled successfully. Inventory restored for room type: " + roomType);
        } else {
            System.out.println("Cancellation failed: Reservation ID " + reservationId + " not found.");
        }
    }

    public void showRollbackHistory() {
        System.out.println("\nRollback History (Most Recent First):");
        if (releasedRoomIds.isEmpty()) {
            System.out.println("No cancellations recorded.");
            return;
        }
        for (int i = releasedRoomIds.size() - 1; i >= 0; i--) {
            System.out.println("Released Reservation ID: " + releasedRoomIds.get(i));
        }
    }
}

class BookingHistory {

    private List<Reservation> confirmedReservations;

    public BookingHistory() {
        confirmedReservations = new ArrayList<>();
    }

    public void addReservation(Reservation reservation) {
        confirmedReservations.add(reservation);
    }

    public List<Reservation> getConfirmedReservations() {
        return confirmedReservations;
    }
}
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
    public boolean isValidRoomType(String type) {
        return counts.containsKey(type);
    }
    public void incrementInventory(String type) {
        counts.put(type, counts.get(type) + 1);
    }
}
class BookingReportService {

    public void generateReport(BookingHistory history) {
        System.out.println("Booking History Report");

        for (Reservation res : history.getConfirmedReservations()) {
            System.out.println("Guest: " + res.getGuestName() +
                    ", Room Type: " + res.getRoomType());
        }
    }
}
class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}
class ReservationValidator {

    public void validate(String guestName, String roomType, RoomInventory inventory)
            throws InvalidBookingException {

        if (guestName == null || guestName.trim().isEmpty()) {
            throw new InvalidBookingException("Guest name cannot be empty.");
        }

        if (!inventory.isValidRoomType(roomType)) {
            throw new InvalidBookingException("Invalid room type selected.");
        }

        if (inventory.getAvailableCount(roomType) <= 0) {
            throw new InvalidBookingException("No availability for the selected room type.");
        }
    }
}
class ConcurrentBookingProcessor implements Runnable {

    private RoomAllocationService allocationService;
    private RoomInventory inventory;
    private Reservation reservation;

    public ConcurrentBookingProcessor(RoomAllocationService allocationService,
                                      RoomInventory inventory,
                                      Reservation reservation) {
        this.allocationService = allocationService;
        this.inventory = inventory;
        this.reservation = reservation;
    }

    @Override
    public void run() {
        // Critical Section: Synchronize on the inventory object
        // to prevent multiple threads from over-booking.
        synchronized (inventory) {
            System.out.println("Thread " + Thread.currentThread().getName() +
                    " attempting booking for: " + reservation.getGuestName());

            allocationService.allocateRoom(reservation, inventory);
        }
    }
}

class PersistenceService {

    private static final String FILE_NAME = "booking_data.ser";

    public void saveState(List<Reservation> history) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(history);
            System.out.println("System state persisted successfully to " + FILE_NAME);
        } catch (IOException e) {
            System.out.println("Error saving state: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Reservation> loadState() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("No persistence file found. Starting with fresh state.");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (List<Reservation>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading state: " + e.getMessage());
            return null;
        }
    }
}

public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("Data Persistence & System Recovery");

        PersistenceService persistenceService = new PersistenceService();
        BookingHistory history = new BookingHistory();

        System.out.println("\nStep 1: Creating initial bookings...");
        history.addReservation(new Reservation("Abhi", "Single"));
        history.addReservation(new Reservation("Subha", "Double"));

        persistenceService.saveState(history.getConfirmedReservations());

        history = new BookingHistory();
        System.out.println("\nStep 2: Restarting system and recovering data...");

        List<Reservation> recoveredData = persistenceService.loadState();
        if (recoveredData != null) {
            for (Reservation res : recoveredData) {
                history.addReservation(res);
            }
        }

        System.out.println("\nRecovery complete. Restored Bookings:");
        for (Reservation res : history.getConfirmedReservations()) {
            System.out.println("- Guest: " + res.getGuestName() + ", Room: " + res.getRoomType());
        }
    }
    }

