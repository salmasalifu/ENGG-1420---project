package controller;

import model.*;
import user.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DataManager {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static void loadInitialData(UserManagement userManager, List<Event> eventList, BookingManager bookingManager) {
        // We load users and events FIRST so the booking loader can look up names and titles
        loadUsers(userManager);
        loadEvents(eventList);
        loadBookings(bookingManager, userManager, eventList);
    }

    private static void loadUsers(UserManagement userManager) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                DataManager.class.getResourceAsStream("/com/example/projectfinal/users.csv")))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", -1);
                if (data.length >= 4) {
                    userManager.createUser(data[0].trim(), data[1].trim(), data[2].trim(), data[3].trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private static void loadEvents(List<Event> eventList) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                DataManager.class.getResourceAsStream("/com/example/projectfinal/events.csv")))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] d = line.split(",", -1);
                if (d.length < 10) continue; // Skip broken lines

                String id = d[0].trim();
                String title = d[1].trim();
                LocalDateTime dt = LocalDateTime.parse(d[2].trim(), FORMATTER);
                String loc = d[3].trim();
                int cap = Integer.parseInt(d[4].trim());
                String statusCSV = d[5].trim();
                String type = d[6].trim();

                Event newEvent = null;
                if (type.equalsIgnoreCase("Workshop")) {
                    newEvent = new Workshop(id, title, dt, loc, cap, d[7].trim());
                } else if (type.equalsIgnoreCase("Seminar")) {
                    newEvent = new Seminar(id, title, dt, loc, cap, d[8].trim());
                } else if (type.equalsIgnoreCase("Concert")) {
                    newEvent = new Concert(id, title, dt, loc, cap, d[9].trim());
                }

                if (newEvent != null) {
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

    private static void loadBookings(BookingManager bookingManager, UserManagement userManager, List<Event> eventList) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                DataManager.class.getResourceAsStream("/com/example/projectfinal/bookings.csv")))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] d = line.split(",", -1);
                if (d.length < 5) continue;

                String bookingId = d[0].trim();
                String userId = d[1].trim();
                String eventId = d[2].trim();
                LocalDateTime createdAt = LocalDateTime.parse(d[3].trim(), FORMATTER);
                BookingStatus status = BookingStatus.valueOf(d[4].trim().toUpperCase());

                // Relational lookups: Grab the actual name and title so the GUI doesn't look blank!
                User associatedUser = userManager.getUser(userId);
                String actualName = (associatedUser != null) ? associatedUser.getName() : "Unknown User";

                Event associatedEvent = eventList.stream().filter(e -> e.getID().equals(eventId)).findFirst().orElse(null);
                String actualTitle = (associatedEvent != null) ? associatedEvent.getTitle() : "Unknown Event";

                Booking b = new Booking(bookingId, userId, actualName, eventId, actualTitle, status);
                b.setCreatedAt(createdAt); // Restore the original timestamp for waitlist accuracy

                bookingManager.getAllBookings().add(b);
            }
        } catch (Exception e) {
            System.err.println("Error loading bookings: " + e.getMessage());
        }
    }
}