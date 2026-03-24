package controller;

import model.*;
import user.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * DataManager is responsible for loading the initial state of the application
 * from CSV files. It populates the UserManagement system, the Event list, and the BookingManager.
 */
public class DataManager {

    // Formatter to match the ISO date-time string in the CSV (e.g., 2026-09-01T10:00:00)
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Master method to load all data. The order matters here!
     * Users and Events MUST be loaded BEFORE Bookings so that when a booking
     * is read from the file, we can look up the actual User Name and Event Title.
     */
    public static void loadInitialData(UserManagement userManager, List<Event> eventList, BookingManager bookingManager) {
        loadUsers(userManager);
        loadEvents(eventList);
        loadBookings(bookingManager, userManager, eventList);
    }

    /**
     * Reads users.csv and creates User objects in the UserManagement system.
     */
    private static void loadUsers(UserManagement userManager) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                DataManager.class.getResourceAsStream("/com/example/projectfinal/users.csv")))) {
            String line;
            br.readLine(); // Skip the header row

            while ((line = br.readLine()) != null) {
                // Split the line by commas. -1 ensures empty trailing columns aren't discarded.
                String[] data = line.split(",", -1);

                // Ensure we have enough columns to create a user
                if (data.length >= 4) {
                    // Data mapping: ID, Name, Email, Type
                    userManager.createUser(data[0].trim(), data[1].trim(), data[2].trim(), data[3].trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    /**
     * Reads events.csv and creates specific Event subclasses (Workshop, Seminar, Concert).
     */
    private static void loadEvents(List<Event> eventList) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                DataManager.class.getResourceAsStream("/com/example/projectfinal/events.csv")))) {
            String line;
            br.readLine(); // Skip the header row

            while ((line = br.readLine()) != null) {
                String[] d = line.split(",", -1);

                // Skip malformed lines missing columns
                if (d.length < 10) continue;

                // Extract core event data
                String id = d[0].trim();
                String title = d[1].trim();
                LocalDateTime dt = LocalDateTime.parse(d[2].trim(), FORMATTER);
                String loc = d[3].trim();
                int cap = Integer.parseInt(d[4].trim());
                String statusCSV = d[5].trim();
                String type = d[6].trim();

                Event newEvent = null;

                // Determine which subclass to instantiate based on the "type" column
                if (type.equalsIgnoreCase("Workshop")) {
                    newEvent = new Workshop(id, title, dt, loc, cap, d[7].trim()); // Column H: Topic
                } else if (type.equalsIgnoreCase("Seminar")) {
                    newEvent = new Seminar(id, title, dt, loc, cap, d[8].trim());  // Column I: Speaker
                } else if (type.equalsIgnoreCase("Concert")) {
                    newEvent = new Concert(id, title, dt, loc, cap, d[9].trim());  // Column J: Age Restriction
                }

                // If an event was successfully created, process its status and save it
                if (newEvent != null) {
                    // Override default Active status if CSV explicitly says Cancelled
                    if (statusCSV.equalsIgnoreCase("Cancelled")) {
                        newEvent.setStatus(EventStatus.Cancelled);
                    }
                    eventList.add(newEvent);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading events: " + e.getMessage());
        }
    }

    /**
     * Reads bookings.csv. This method uses the populated userManager and eventList
     * to look up names and titles so the GUI tables display human-readable data instead of just IDs.
     */
    private static void loadBookings(BookingManager bookingManager, UserManagement userManager, List<Event> eventList) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                DataManager.class.getResourceAsStream("/com/example/projectfinal/bookings.csv")))) {
            String line;
            br.readLine(); // Skip the header row

            while ((line = br.readLine()) != null) {
                String[] d = line.split(",", -1);

                // Skip malformed lines
                if (d.length < 5) continue;

                String bookingId = d[0].trim();
                String userId = d[1].trim();
                String eventId = d[2].trim();
                LocalDateTime createdAt = LocalDateTime.parse(d[3].trim(), FORMATTER);
                BookingStatus status = BookingStatus.valueOf(d[4].trim().toUpperCase());

                // --- RELATIONAL LOOKUPS ---

                // 1. User Lookup: The CSV only stores IDs, so we look up the full User object to get their actual Name
                User associatedUser = userManager.getUser(userId);
                String actualName = (associatedUser != null) ? associatedUser.getName() : "Unknown User";

                // 2. Event Lookup: Look up the full Event object to get its actual Title
                Event associatedEvent = eventList.stream()
                        .filter(e -> e.getID().equals(eventId))
                        .findFirst()
                        .orElse(null);
                String actualTitle = (associatedEvent != null) ? associatedEvent.getTitle() : "Unknown Event";

                // Create the booking with the dynamically fetched name and title
                Booking b = new Booking(bookingId, userId, actualName, eventId, actualTitle, status);

                // Overwrite the default 'now()' timestamp with the original booking time from the CSV
                b.setCreatedAt(createdAt);

                // Add to the centralized booking manager
                bookingManager.getAllBookings().add(b);
            }
        } catch (Exception e) {
            System.err.println("Error loading bookings: " + e.getMessage());
        }
    }
}