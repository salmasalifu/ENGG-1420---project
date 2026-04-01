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

    // --- Backend Managers ---
    private UserManagement userManager = new UserManagement();
    private BookingManager bookingManager = new BookingManager();

    // --- Data Lists ---
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
    @FXML private Label waitlistMessageLabel; // NEW: Dedicated label for Tab 4 notifications

    @FXML
    public void initialize() {
        // Load up our dropdown choices
        userTypeDropdown.setItems(FXCollections.observableArrayList("Student", "Staff", "Guest"));
        eventTypeDropdown.setItems(FXCollections.observableArrayList("Workshop", "Seminar", "Concert"));

        // Map User Table Columns
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userTypeColumn.setCellValueFactory(new PropertyValueFactory<>("userType"));

        // Map Event Table Columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("eventtype"));
        extraInfoColumn.setCellValueFactory(new PropertyValueFactory<>("extrainfo"));

        // Map Booking Table Columns
        bookingEventIdColumn.setCellValueFactory(new PropertyValueFactory<>("eventId"));
        bookingEventTitleColumn.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));
        bookingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Map Waitlist/Roster Table Columns
        rosterUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        rosterUserNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        rosterStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        rosterTimeColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Fire up the data manager to read the CSVs
        DataManager.loadInitialData(userManager, eventData, bookingManager);
        userData.setAll(userManager.getAllUsers());

        // Connect our data lists to the visual tables
        userTable.setItems(userData);
        eventTable.setItems(eventData);
        userBookingsTable.setItems(userBookingsData);
        rosterTable.setItems(rosterData);

        updateRosterDropdown();

        // Listener: When a user clicks an event, auto-fill the text boxes so they can edit it
        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                eventIdInput.setText(newSelection.getID());
                eventIdInput.setDisable(true); // Lock the ID so it can't be messed with

                eventTitleInput.setText(newSelection.getTitle());
                eventDateInput.setText(newSelection.getDateTime().toString());
                eventLocationInput.setText(newSelection.getLocation());
                eventCapacityInput.setText(String.valueOf(newSelection.getCapacity()));

                eventTypeDropdown.setValue(newSelection.getEventtype());
                eventTypeDropdown.setDisable(true); // Lock the Type

                eventExtraInfoInput.setText(newSelection.getExtrainfo());
            }
        });

        // Wire all the buttons! (The null checks prevent crashes if a button is missing in Scene Builder)
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
    private void handleAddUser() {
        String id = userIdInput.getText().trim();
        String name = userNameInput.getText().trim();
        String email = userEmailInput.getText().trim();
        String type = userTypeDropdown.getValue();

        if (id.isEmpty() || name.isEmpty() || type == null) return;

        if (userManager.createUser(id, name, email, type)) {
            userData.setAll(userManager.getAllUsers()); // Refresh UI
            userIdInput.clear(); userNameInput.clear(); userEmailInput.clear();
            userTypeDropdown.setValue(null);
        }
    }

    private void handleRemoveUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Find events they are abandoning so we can promote the waitlist
            List<String> eventsToPromote = bookingManager.getAllBookings().stream()
                    .filter(b -> b.getUserId().equals(selected.getUserId()) && b.getStatus() == BookingStatus.CONFIRMED)
                    .map(Booking::getEventId)
                    .collect(Collectors.toList());

            // Scrub them from the system
            bookingManager.getAllBookings().removeIf(b -> b.getUserId().equals(selected.getUserId()));
            userManager.removeUser(selected.getUserId());
            userData.remove(selected);

            // Auto-promote people into the empty seats they just left behind!
            eventsToPromote.forEach(eId -> bookingManager.promoteNextOnWaitlist(eId));

            rosterData.clear();
            userBookingsData.clear();
        }
    }

    // ==========================================
    // EVENT LOGIC
    // ==========================================
    private void handleAddEvent() {
        try {
            String id = eventIdInput.getText().trim();
            String title = eventTitleInput.getText().trim();
            String type = eventTypeDropdown.getValue();

            if (id.isEmpty() || type == null || eventDateInput.getText().isEmpty()) return;

            // Stop duplicate event IDs from breaking the system
            if (eventData.stream().anyMatch(e -> e.getID().equalsIgnoreCase(id))) {
                System.out.println("Error: An Event with this ID already exists!");
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
            System.out.println("Validation Error: Check Date format or Capacity.");
        }
    }

    private void handleUpdateEvent() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            // Overwrite the old info with whatever is currently in the text boxes
            selected.setTitle(eventTitleInput.getText().trim());
            selected.setDateTime(LocalDateTime.parse(eventDateInput.getText().trim()));
            selected.setLocation(eventLocationInput.getText().trim());
            selected.updateCapacity(Integer.parseInt(eventCapacityInput.getText().trim()));
            selected.updateExtrainfo(eventExtraInfoInput.getText().trim());

            eventTable.refresh();
            clearEventInputs();
        } catch (Exception e) {
            System.out.println("Validation Error: Ensure Date and Capacity are formatted correctly.");
        }
    }

    private void clearEventInputs() {
        eventIdInput.clear(); eventTitleInput.clear(); eventDateInput.clear();
        eventLocationInput.clear(); eventCapacityInput.clear(); eventExtraInfoInput.clear();
        eventIdInput.setDisable(false); // Unlock for next time
        eventTypeDropdown.setDisable(false); // Unlock for next time
        eventTable.getSelectionModel().clearSelection();
    }

    private void handleCancelEvent() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.cancelEvent();
            // Cascade: If the event dies, all bookings die with it
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
    private void handleBookEvent() {
        String uId = bookingUserIdInput.getText().trim();
        String eId = bookingEventIdInput.getText().trim();

        User u = userManager.getUser(uId);
        Event e = eventData.stream().filter(ev -> ev.getID().equals(eId)).findFirst().orElse(null);

        if (u != null && e != null) {

            // Cannot book dead events
            if (e.getStatus() == EventStatus.Cancelled) {
                if (bookingMessageLabel != null) bookingMessageLabel.setText("Error: Cannot book a cancelled event.");
                return;
            }

            // Cannot book the same person twice for the same event
            if (bookingManager.isUserAlreadyBooked(uId, eId)) {
                if (bookingMessageLabel != null) bookingMessageLabel.setText("Error: User is already booked for this event.");
                return;
            }

            int confirmedCount = (int) bookingManager.getAllBookings().stream()
                    .filter(b -> b.getEventId().equals(eId) && b.getStatus() == BookingStatus.CONFIRMED)
                    .count();

            String newBookingId = "B" + (bookingManager.getAllBookings().size() + 1);
            String result = bookingManager.createBooking(
                    newBookingId, uId, u.getName(), u.getUserType(), eId, e.getTitle(), confirmedCount, e.getCapacity()
            );

            if (bookingMessageLabel != null) bookingMessageLabel.setText(result);
            refreshUserBookings(uId);
            handleViewRoster();

            bookingUserIdInput.clear(); bookingEventIdInput.clear();
        } else {
            if (bookingMessageLabel != null) bookingMessageLabel.setText("Error: User ID or Event ID not found.");
        }
    }

    private void handleViewUserBookings() {
        String uId = bookingUserIdInput.getText().trim();
        if (uId.isEmpty()) {
            if (bookingMessageLabel != null) bookingMessageLabel.setText("Please enter a User ID to search.");
            return;
        }

        User u = userManager.getUser(uId);
        if (u != null) {
            refreshUserBookings(uId);
            if (bookingMessageLabel != null) bookingMessageLabel.setText("Showing bookings for: " + u.getName());
            bookingEventIdInput.clear();
        } else {
            if (bookingMessageLabel != null) bookingMessageLabel.setText("Error: User ID not found.");
            userBookingsData.clear();
        }
    }

    private void refreshUserBookings(String userId) {
        userBookingsData.setAll(bookingManager.getAllBookings().stream()
                .filter(b -> b.getUserId().equals(userId))
                .collect(Collectors.toList()));
    }

    // ==========================================
    // WAITLIST & ROSTER LOGIC
    // ==========================================
    private void handleViewRoster() {
        String eId = rosterEventSelect.getValue();
        if (eId != null) {
            rosterData.setAll(bookingManager.getAllBookings().stream()
                    .filter(b -> b.getEventId().equals(eId))
                    .collect(Collectors.toList()));
        }
    }

    private void handleRemoveFromRoster() {
        Booking selected = rosterTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Remove them and grab the notification string if someone was promoted
            String notification = bookingManager.removeBooking(selected.getBookingId());
            if (waitlistMessageLabel != null) waitlistMessageLabel.setText(notification); // Display to UI
            handleViewRoster();
        }
    }

    private void handlePromoteUser() {
        Booking selected = rosterTable.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getStatus() == BookingStatus.WAITLISTED) {
            selected.setStatus(BookingStatus.CONFIRMED);
            rosterTable.refresh();
            refreshUserBookings(selected.getUserId());
        }
    }

    private void handleDemoteUser() {
        Booking selected = rosterTable.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getStatus() == BookingStatus.CONFIRMED) {
            selected.setStatus(BookingStatus.WAITLISTED);

            // Since they lost their seat, auto-promote the next person and grab the notification
            String notification = bookingManager.promoteNextOnWaitlist(selected.getEventId());
            if (waitlistMessageLabel != null) waitlistMessageLabel.setText(notification); // Display to UI

            rosterTable.refresh();
            refreshUserBookings(selected.getUserId());
        }
    }

    private void updateRosterDropdown() {
        rosterEventSelect.setItems(FXCollections.observableArrayList(
                eventData.stream().map(Event::getID).collect(Collectors.toList())
        ));
    }
}