package controller;

public class Main {
    public static void main(String[] args) {
        BookingManager manager = new BookingManager();

        System.out.println("--- TEST 1: Normal Bookings (Capacity 2) ---");
        // Event has 2 max capacity. Current capacity is 0, then 1. 
        // These should both be CONFIRMED
        System.out.println(manager.createBooking("B001", "U001", "Student", "E101", 0, 2));
        System.out.println(manager.createBooking("B002", "U002", "Staff", "E101", 1, 2));

        System.out.println("\n--- TEST 2: Event Full -> Waitlist ---");
        // Event capacity is 2, currently at 2. This should be WAITLISTED
        System.out.println(manager.createBooking("B003", "U003", "Guest", "E101", 2, 2));

        System.out.println("\n--- TEST 3: Duplicate Booking Prevention ---");
        // User U001 trying to book E101 again. Should throw an error[cite: 57].
        System.out.println(manager.createBooking("B004", "U001", "Student", "E101", 2, 2));

        System.out.println("\n--- TEST 4: User Limits (Student max 3) ---");
        // U001 already has 1 confirmed booking. Let's add 2 more so they hit their limit of 3
        System.out.println(manager.createBooking("B005", "U001", "Student", "E102", 0, 10));
        System.out.println(manager.createBooking("B006", "U001", "Student", "E103", 0, 10));

        // This 4th confirmed booking attempt should fail
        System.out.println(manager.createBooking("B007", "U001", "Student", "E104", 0, 10));

        System.out.println("\n--- TEST 5: Cancellation & Waitlist Promotion ---");
        // Cancelling U001's confirmed spot in E101. 
        // This should trigger the system to automatically promote U003 (Guest) from the waitlist
        System.out.println("Cancelling booking B001 for event E101...");
        manager.cancelBooking("B001");
    }
}