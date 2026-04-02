package controller;

import model.*;
import user.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * DataManager is responsible for loading and saving the application state.
 *
 * File Persistance Implementation:
 * - First run: Loads starter data from bundled CSV resources
 * - Subsequent runs: Loads from saved files (users_saved.csv, events_saved.csv, bookings_saved.csv)
 * - On shutdown: Saves current state to preserve changes across program restarts
 */
public class DataManager {

    // Formatter to match the ISO date-time string in the CSV (e.g., 2026-09-01T10:00:00)
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Saved File formats as constants to be used throughout the code for file persistance:
    private static final String USERS_SAVED = "users_saved.csv";
    private static final String EVENTS_SAVED = "events_saved.csv";
    private static final String BOOKINGS_SAVED = "bookings_saved.csv";


    /**
     * Master method to load all data. The order matters here!
     * Users and Events MUST be loaded BEFORE Bookings so that when a booking
     * is read from the file, we can look up the actual User Name and Event Title.
     *
     * File Persistance Implementation: Checks for saved files first, falls back to bundled starter data if none exist.
     */
    public static void loadInitialData(UserManagement userManager, List<Event> eventList, BookingManager bookingManager) {
        // Check if saved files exist from a previous session
        File savedUsers = new File(USERS_SAVED);
        File savedEvents = new File(EVENTS_SAVED);
        File savedBookings = new File(BOOKINGS_SAVED);

        // If all saved files exist, load from them (user's previous data)
        if (savedUsers.exists() && savedEvents.exists() && savedBookings.exists()) {
            System.out.println("Loading saved data...");
            loadUsersFromPath(userManager, savedUsers);
            loadEventsFromPath(eventList, savedEvents);
            loadBookingsFromPath(bookingManager, userManager, eventList, savedBookings);
        } else {
            // First run: no saved data exists, load from bundled starter CSVs
            System.out.println("First run - loading starter data...");
            loadUsers(userManager);
            loadEvents(eventList);
            loadBookings(bookingManager, userManager, eventList);
        }
    }

    /**
     * File Persistance helper method: Saves all current data to CSV files.
     * Called when the application closes to preserve user changes.
     * Creates/overwrites: users_saved.csv, events_saved.csv, bookings_saved.csv
     */
    public static void saveAllData(UserManagement userManager, List<Event> eventList, BookingManager bookingManager) {
    // Save Users
    FileWriter userWriter = null;
    try {
        //Create a new file to write into:
        userWriter = new FileWriter(USERS_SAVED);
        userWriter.write("ID,Name,Email,Type\n");
        //loop through all users and write their data to save
        for (User user : userManager.getAllUsers()) {
            userWriter.write(user.getUserId() + "," + user.getName() + "," + 
                           user.getEmail() + "," + user.getUserType() + "\n");
        }
    } catch (IOException e) {
        System.err.println("Error saving users: " + e.getMessage());
    } finally {
        if (userWriter != null) {
            try {
                //close the file after writing
                userWriter.close();
            } catch (IOException e) {
                System.err.println("Error closing users file: " + e.getMessage());
            }
        }
    }

    // Save Events (similar logic to save users but with events)
    FileWriter eventWriter = null;
    try {
        eventWriter = new FileWriter("events_saved.csv");
        eventWriter.write("ID,Title,DateTime,Location,Capacity,Status,Type,Topic,Speaker,AgeRestriction\n");
        for (Event event : eventList) {
            String type = "", topic = "", speaker = "", ageRestriction = "";
            if (event instanceof Workshop) {
                type = "Workshop";
                topic = ((Workshop) event).getEventtype();
            } else if (event instanceof Seminar) {
                type = "Seminar";
                speaker = ((Seminar) event).getEventtype();
            } else if (event instanceof Concert) {
                type = "Concert";
                ageRestriction = ((Concert) event).getEventtype();
            }

            eventWriter.write(event.getID() + "," +
                    event.getTitle() + "," +
                    event.getDateTime().format(FORMATTER) + "," +
                    event.getLocation() + "," +
                    event.getCapacity() + "," +
                    event.getStatus().toString() + "," +
                    type + "," +
                    topic + "," +
                    speaker + "," +
                    ageRestriction + "\n");
        }
    } catch (IOException e) {
        System.err.println("Error saving events: " + e.getMessage());
    } finally {
        if (eventWriter != null) {
            try {
                eventWriter.close();
            } catch (IOException e) {
                System.err.println("Error closing events file: " + e.getMessage());
            }
        }
    }

    // Save Bookings (similar logic to save users but with bookings)
    FileWriter bookingWriter = null;
    try {
        bookingWriter = new FileWriter("bookings_saved.csv");
        bookingWriter.write("BookingID,UserID,EventID,CreatedAt,Status\n");
        for (Booking booking : bookingManager.getAllBookings()) {
            bookingWriter.write(booking.getBookingId() + "," +
                    booking.getUserId() + "," +
                    booking.getEventId() + "," +
                    booking.getCreatedAt().format(FORMATTER) + "," +
                    booking.getStatus().toString() + "\n");
        }
    } catch (IOException e) {
        System.err.println("Error saving bookings: " + e.getMessage());
    } finally {
        if (bookingWriter != null) {
            try {
                bookingWriter.close();
            } catch (IOException e) {
                System.err.println("Error closing bookings file: " + e.getMessage());
            }
        }
    }

    System.out.println("Data saved.");
}

    /**
     * File Persistance helper method: Loads users from external saved file.
     * Same parsing logic as loadUsers(), but reads from disk instead of bundled resources.
     */
    private static void loadUsersFromPath(UserManagement userManager, File file) {
    BufferedReader br = null;
    try {
        //Open the saved file and read from it 
        br = new BufferedReader(new FileReader(file));
        String line;
        br.readLine(); // Skip header
        while ((line = br.readLine()) != null) {
            //while reading exisitng data, split it into indexes seperated by commas but also including blanks into an array repersenting that users data
            String[] data = line.split(",", -1);
            if (data.length >= 4) {
                //create a copy of that data as the logic for loading it
                userManager.createUser(data[0].trim(), data[1].trim(), data[2].trim(), data[3].trim());
            }
        }
    } catch (Exception e) {
        System.err.println("Error loading users: " + e.getMessage());
    } finally {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                System.err.println("Error closing users file: " + e.getMessage());
            }
        }
    }
}

/**
     * File Persistance helper method: Loads events from external saved file.
     * Same parsing logic as loadEvents(), but reads from disk instead of bundled resources.
     */
private static void loadEventsFromPath(List<Event> eventList, File file) {
    BufferedReader br = null;
    try {
        br = new BufferedReader(new FileReader(file));
        String line;
        br.readLine(); // Skip header
        while ((line = br.readLine()) != null) {
            String[] d = line.split(",", -1);
            if (d.length < 10) continue;

            LocalDateTime dt = LocalDateTime.parse(d[2].trim(), FORMATTER);
            int cap = Integer.parseInt(d[4].trim());
            Event newEvent = null;

            if (d[6].trim().equalsIgnoreCase("Workshop")) 
                newEvent = new Workshop(d[0].trim(), d[1].trim(), dt, d[3].trim(), cap, d[7].trim());
            else if (d[6].trim().equalsIgnoreCase("Seminar")) 
                newEvent = new Seminar(d[0].trim(), d[1].trim(), dt, d[3].trim(), cap, d[8].trim());
            else if (d[6].trim().equalsIgnoreCase("Concert")) 
                newEvent = new Concert(d[0].trim(), d[1].trim(), dt, d[3].trim(), cap, d[9].trim());

            if (newEvent != null) {
                if (d[5].trim().equalsIgnoreCase("Cancelled")) 
                    newEvent.setStatus(EventStatus.Cancelled);
                eventList.add(newEvent);
            }
        }
    } catch (Exception e) {
        System.err.println("Error loading events: " + e.getMessage());
    } finally {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                System.err.println("Error closing events file: " + e.getMessage());
            }
        }
    }
}

/**
     * File Persistance helper method: Loads bookings from external saved file.
     * Same parsing logic as loadBookings(), but reads from disk instead of bundled resources.
     * Performs lookups to match UserIDs/EventIDs with actual names/titles.
     */
private static void loadBookingsFromPath(BookingManager bookingManager, UserManagement userManager, List<Event> eventList, File file) {
    BufferedReader br = null;
    try {
        br = new BufferedReader(new FileReader(file));
        String line;
        br.readLine(); // Skip header
        while ((line = br.readLine()) != null) {
            String[] d = line.split(",", -1);
            if (d.length < 5) continue;

            User u = userManager.getUser(d[1].trim());
            Event ev = eventList.stream().filter(e -> e.getID().equals(d[2].trim())).findFirst().orElse(null);

            Booking b = new Booking(d[0].trim(), d[1].trim(),
                    u != null ? u.getName() : "Unknown",
                    d[2].trim(),
                    ev != null ? ev.getTitle() : "Unknown",
                    BookingStatus.valueOf(d[4].trim().toUpperCase()));
            b.setCreatedAt(LocalDateTime.parse(d[3].trim(), FORMATTER));
            bookingManager.getAllBookings().add(b);
        }
    } catch (Exception e) {
        System.err.println("Error loading bookings: " + e.getMessage());
    } finally {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                System.err.println("Error closing bookings file: " + e.getMessage());
            }
        }
    }
}

    /**
     * loadUsers method reads users from bundled starter CSV.
     * Used as fallback when no saved data exists (first run).
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
     * loadEvents method reads events from bundled starter CSV.
     * Used as fallback when no saved data exists (first run).
     */
    private static void loadEvents(List<Event> eventList) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                DataManager.class.getResourceAsStream("/com/example/projectfinal/events.csv")))) {
            String line;
            br.readLine(); // Skip the header row

            while ((line = br.readLine()) != null) {
                String[] d = line.split(",", -1);

                // Skip broken lines missing columns
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
     * loadBookings method reads bookings from bundled starter CSV.
     * Used as fallback when no saved data exists (first run).
     * Uses populated userManager and eventList to look up names and titles.
     */
    private static void loadBookings(BookingManager bookingManager, UserManagement userManager, List<Event> eventList) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                DataManager.class.getResourceAsStream("/com/example/projectfinal/bookings.csv")))) {
            String line;
            br.readLine(); // Skip the header row

            while ((line = br.readLine()) != null) {
                String[] d = line.split(",", -1);

                // Skip broken lines
                if (d.length < 5) continue;

                String bookingId = d[0].trim();
                String userId = d[1].trim();
                String eventId = d[2].trim();
                LocalDateTime createdAt = LocalDateTime.parse(d[3].trim(), FORMATTER);
                BookingStatus status = BookingStatus.valueOf(d[4].trim().toUpperCase());

                //Relational Lookups:

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
