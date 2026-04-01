package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.*;
import user.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {

    // These managers handle the core data processing for users and bookings.
    private UserManagement userManager = new UserManagement();
    private BookingManager bookingManager = new BookingManager();

    // These lists keep the UI tables updated in real-time as data changes.
    private ObservableList<Event> eventData = FXCollections.observableArrayList();
    private ObservableList<User> userData = FXCollections.observableArrayList();
    private ObservableList<Booking> userBookingsData = FXCollections.observableArrayList();
    private ObservableList<Booking> rosterData = FXCollections.observableArrayList();

    // ==========================================
    // TAB 1: USER MANAGEMENT
    // ==========================================
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> userIdColumn, userNameColumn, userEmailColumn, userTypeColumn;
    @FXML private TextField userIdInput, userNameInput, userEmailInput;
    @FXML private ComboBox<String> userTypeDropdown;
    @FXML private Button addUserBtn, removeUserBtn;

    // ==========================================
    // TAB 2: EVENT MANAGEMENT
    // ==========================================
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> idColumn, titleColumn, locationColumn, statusColumn, typeColumn, extraInfoColumn;
    @FXML private TableColumn<Event, LocalDateTime> dateColumn;
    @FXML private TableColumn<Event, Integer> capacityColumn;
    @FXML private TextField eventIdInput, eventTitleInput, eventDateInput, eventLocationInput, eventCapacityInput, eventExtraInfoInput;
    @FXML private ComboBox<String> eventTypeDropdown;
    @FXML private Button addEventBtn, cancelEventBtn, updateEventBtn;

    // ==========================================
    // TAB 3: BOOKING MANAGEMENT
    // ==========================================
    @FXML private TextField bookingUserIdInput, bookingEventIdInput;
    @FXML private Label bookingMessageLabel;
    @FXML private TableView<Booking> userBookingsTable;
    @FXML private TableColumn<Booking, String> bookingEventIdColumn, bookingEventTitleColumn, bookingStatusColumn;
    @FXML private Button bookEventBtn, viewUserBookingsBtn;

    // ==========================================
    // TAB 4: WAITLIST / ROSTER MANAGEMENT
    // ==========================================
    @FXML private ComboBox<String> rosterEventSelect;
    @FXML private TableView<Booking> rosterTable;
    @FXML private TableColumn<Booking, String> rosterUserIdColumn, rosterUserNameColumn, rosterStatusColumn;
    @FXML private TableColumn<Booking, LocalDateTime> rosterTimeColumn;
    @FXML private Button viewRosterBtn, removeRosterBtn, promoteBtn, demoteBtn;
    @FXML private Label waitlistMessageLabel;

    @FXML
    public void initialize() {
        // Populates the dropdown menus with valid categories.
        userTypeDropdown.setItems(FXCollections.observableArrayList("Student", "Staff", "Guest"));
        eventTypeDropdown.setItems(FXCollections.observableArrayList("Workshop", "Seminar", "Concert"));

        // Links the table columns to the specific variables in our model classes.
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userTypeColumn.setCellValueFactory(new PropertyValueFactory<>("userType"));

        idColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("eventtype"));
        extraInfoColumn.setCellValueFactory(new PropertyValueFactory<>("extrainfo"));

        bookingEventIdColumn.setCellValueFactory(new PropertyValueFactory<>("eventId"));
        bookingEventTitleColumn.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));
        bookingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        rosterUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        rosterUserNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        rosterStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        rosterTimeColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Pulls in the starting data from the CSV files.
        DataManager.loadInitialData(userManager, eventData, bookingManager);
        userData.setAll(userManager.getAllUsers());

        // Attaches our data lists to the actual UI components.
        userTable.setItems(userData);
        eventTable.setItems(eventData);
        userBookingsTable.setItems(userBookingsData);
        rosterTable.setItems(rosterData);

        updateRosterDropdown();

        // This listener detects when an event is clicked and fills the text fields for easy editing.
        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                eventIdInput.setText(newSelection.getID());
                eventIdInput.setDisable(true); // Lock ID: You can't change the ID of an existing event

                eventTitleInput.setText(newSelection.getTitle());
                eventDateInput.setText(newSelection.getDateTime().toString());
                eventLocationInput.setText(newSelection.getLocation());
                eventCapacityInput.setText(String.valueOf(newSelection.getCapacity()));

                eventTypeDropdown.setValue(newSelection.getEventtype());
                eventTypeDropdown.setDisable(true); // Lock Type: Changing type requires a new object

                eventExtraInfoInput.setText(newSelection.getExtrainfo());
            }
        });

        // Configures what happens when each button is clicked.
        addUserBtn.setOnAction(e -> handleAddUser());
        if (removeUserBtn != null) removeUserBtn.setOnAction(e -> handleRemoveUser());

        addEventBtn.setOnAction(e -> handleAddEvent());
        if (updateEventBtn != null) updateEventBtn.setOnAction(e -> handleUpdateEvent());
        cancelEventBtn.setOnAction(e -> handleCancelEvent());

        bookEventBtn.setOnAction(e -> handleBookEvent());
        if (viewUserBookingsBtn != null) viewUserBookingsBtn.setOnAction(e -> handleViewUserBookings());

        viewRosterBtn.setOnAction(e -> handleViewRoster());
        if (removeRosterBtn != null) removeRosterBtn.setOnAction(e -> handleRemoveFromRoster());
        if (promoteBtn != null) promoteBtn.setOnAction(e -> handlePromoteUser());
        if (demoteBtn != null) demoteBtn.setOnAction(e -> handleDemoteUser());
    }

    // ==========================================
    // USER LOGIC
    // ==========================================

    // Creates a new user and refreshes the table to show them immediately.
    private void handleAddUser() {
        String id = userIdInput.getText().trim();
        String name = userNameInput.getText().trim();
        String email = userEmailInput.getText().trim();
        String type = userTypeDropdown.getValue();

        if (id.isEmpty() || name.isEmpty() || type == null) return;

        if (userManager.createUser(id, name, email, type)) {
            userData.setAll(userManager.getAllUsers());
            userIdInput.clear(); userNameInput.clear(); userEmailInput.clear();
            userTypeDropdown.setValue(null);
        }
    }

    // Completely removes a user and automatically fills any event seats they were holding.
    private void handleRemoveUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Find seats they are giving up so we can promote the waitlist
            List<String> affectedEvents = bookingManager.getAllBookings().stream()
                    .filter(b -> b.getUserId().equals(selected.getUserId()) && b.getStatus() == BookingStatus.CONFIRMED)
                    .map(Booking::getEventId)
                    .collect(Collectors.toList());

            bookingManager.getAllBookings().removeIf(b -> b.getUserId().equals(selected.getUserId()));
            userManager.removeUser(selected.getUserId());
            userData.remove(selected);

            // Auto-promote the next people in line
            affectedEvents.forEach(eId -> bookingManager.promoteNextOnWaitlist(eId));

            rosterData.clear();
            userBookingsData.clear();
        }
    }

    // ==========================================
    // EVENT LOGIC
    // ==========================================

    // Handles creating a new event and ensures the ID isn't already in use.
    private void handleAddEvent() {
        try {
            String id = eventIdInput.getText().trim();
            String title = eventTitleInput.getText().trim();
            String type = eventTypeDropdown.getValue();

            if (id.isEmpty() || type == null || eventDateInput.getText().isEmpty()) return;

            // Check for duplicate IDs before creating
            if (eventData.stream().anyMatch(e -> e.getID().equalsIgnoreCase(id))) {
                System.out.println("Error: That Event ID is already taken.");
                return;
            }

            LocalDateTime date = LocalDateTime.parse(eventDateInput.getText().trim());
            String loc = eventLocationInput.getText().trim();
            int cap = Integer.parseInt(eventCapacityInput.getText().trim());
            String extra = eventExtraInfoInput.getText().trim();

            Event newEvent = null;
            if (type.equals("Workshop")) newEvent = new Workshop(id, title, date, loc, cap, extra);
            else if (type.equals("Seminar")) newEvent = new Seminar(id, title, date, loc, cap, extra);
            else if (type.equals("Concert")) newEvent = new Concert(id, title, date, loc, cap, extra);

            if (newEvent != null) {
                eventData.add(newEvent);
                updateRosterDropdown();
                clearEventInputs();
            }
        } catch (Exception e) {
            System.out.println("Formatting error: Ensure the date is YYYY-MM-DDTHH:MM and capacity is a number.");
        }
    }

    // Updates the details of an existing event and refreshes the display.
    private void handleUpdateEvent() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            // Modify the existing object using the Setters we added to Event.java
            selected.setTitle(eventTitleInput.getText().trim());
            selected.setDateTime(LocalDateTime.parse(eventDateInput.getText().trim()));
            selected.setLocation(eventLocationInput.getText().trim());
            selected.updateCapacity(Integer.parseInt(eventCapacityInput.getText().trim()));
            selected.updateExtrainfo(eventExtraInfoInput.getText().trim());

            eventTable.refresh(); // Tell JavaFX to redraw the table with new info
            clearEventInputs();
        } catch (Exception e) {
            System.out.println("Update failed: Check your date and capacity formats.");
        }
    }

    // Resets the input fields so the user can start a fresh entry.
    private void clearEventInputs() {
        eventIdInput.clear(); eventTitleInput.clear(); eventDateInput.clear();
        eventLocationInput.clear(); eventCapacityInput.clear(); eventExtraInfoInput.clear();
        eventIdInput.setDisable(false); // Unlock for new additions
        eventTypeDropdown.setDisable(false);
        eventTable.getSelectionModel().clearSelection();
    }

    // Marks an event as cancelled and updates all associated bookings.
    private void handleCancelEvent() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.cancelEvent();
            // All bookings for this event also become cancelled
            bookingManager.getAllBookings().stream()
                    .filter(b -> b.getEventId().equals(selected.getID()))
                    .forEach(b -> b.setStatus(BookingStatus.CANCELLED));
            eventTable.refresh();
            rosterData.clear();
        }
    }

    // ==========================================
    // BOOKING LOGIC
    // ==========================================

    // Processes a new booking request while enforcing limits and checking event status.
    private void handleBookEvent() {
        String uId = bookingUserIdInput.getText().trim();
        String eId = bookingEventIdInput.getText().trim();

        User u = userManager.getUser(uId);
        Event e = eventData.stream().filter(ev -> ev.getID().equals(eId)).findFirst().orElse(null);

        if (u != null && e != null) {

            // Blocks booking if the event has been officially cancelled.
            if (e.getStatus() == EventStatus.Cancelled) {
                bookingMessageLabel.setText("Error: You cannot book a ticket for a cancelled event.");
                return;
            }

            // Blocks duplicate entries for the same person and event.
            if (bookingManager.isUserAlreadyBooked(uId, eId)) {
                bookingMessageLabel.setText("Error: This user is already registered for this event.");
                return;
            }

            // Checks the user's rank (Guest, Student, Staff) to ensure they haven't hit their ticket limit.
            if (bookingManager.hasReachedBookingLimit(uId, u.getUserType())) {
                bookingMessageLabel.setText("Error: The maximum booking limit for a " + u.getUserType() + " has been reached.");
                return;
            }

            int confirmedCount = (int) bookingManager.getAllBookings().stream()
                    .filter(b -> b.getEventId().equals(eId) && b.getStatus() == BookingStatus.CONFIRMED)
                    .count();

            String bookingId = "B" + (bookingManager.getAllBookings().size() + 1);
            String result = bookingManager.createBooking(
                    bookingId, uId, u.getName(), u.getUserType(), eId, e.getTitle(), confirmedCount, e.getCapacity()
            );

            bookingMessageLabel.setText(result);
            refreshUserBookings(uId);
            handleViewRoster();

            bookingUserIdInput.clear(); bookingEventIdInput.clear();
        } else {
            bookingMessageLabel.setText("Error: Please verify that both the User ID and Event ID are correct.");
        }
    }

    // Shows the booking history for a specific user on Tab 3.
    private void handleViewUserBookings() {
        String uId = bookingUserIdInput.getText().trim();
        if (uId.isEmpty()) return;

        User u = userManager.getUser(uId);
        if (u != null) {
            refreshUserBookings(uId);
            bookingMessageLabel.setText("Viewing history for " + u.getName());
        } else {
            bookingMessageLabel.setText("Error: User not found.");
            userBookingsData.clear();
        }
    }

    // Refreshes the personal booking history for a specific user.
    private void refreshUserBookings(String userId) {
        userBookingsData.setAll(bookingManager.getAllBookings().stream()
                .filter(b -> b.getUserId().equals(userId))
                .collect(Collectors.toList()));
    }

    // ==========================================
    // WAITLIST & ROSTER LOGIC
    // ==========================================

    // Displays the full list of people signed up for the event chosen in the dropdown.
    private void handleViewRoster() {
        String eId = rosterEventSelect.getValue();
        if (eId != null) {
            rosterData.setAll(bookingManager.getAllBookings().stream()
                    .filter(b -> b.getEventId().equals(eId))
                    .collect(Collectors.toList()));
        }
    }

    // Removes a user from an event and provides a message that includes their name.
    private void handleRemoveFromRoster() {
        Booking selected = rosterTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Remove the booking and show notification if a waitlist promotion occurred
            String result = bookingManager.removeBooking(selected.getBookingId());
            if (waitlistMessageLabel != null) waitlistMessageLabel.setText(result);
            handleViewRoster();
        }
    }

    // Promotes a waitlisted user to confirmed status manually.
    private void handlePromoteUser() {
        Booking selected = rosterTable.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getStatus() == BookingStatus.WAITLISTED) {
            selected.setStatus(BookingStatus.CONFIRMED);
            rosterTable.refresh();
            refreshUserBookings(selected.getUserId());
        }
    }

    // Moves a confirmed user to the waitlist and triggers a promotion for the next person in line.
    private void handleDemoteUser() {
        Booking selected = rosterTable.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getStatus() == BookingStatus.CONFIRMED) {
            selected.setStatus(BookingStatus.WAITLISTED);
            // Promote next in line and show notification
            String result = bookingManager.promoteNextOnWaitlist(selected.getEventId());
            if (waitlistMessageLabel != null) waitlistMessageLabel.setText(result);

            rosterTable.refresh();
            refreshUserBookings(selected.getUserId());
        }
    }

    // Updates the roster dropdown menu whenever events are added or removed.
    private void updateRosterDropdown() {
        rosterEventSelect.setItems(FXCollections.observableArrayList(
                eventData.stream().map(Event::getID).collect(Collectors.toList())
        ));
    }
}