package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.*;
import user.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

public class MainController {

    private UserManagement userManager = new UserManagement();
    private BookingManager bookingManager = new BookingManager();

    private ObservableList<Event> eventData = FXCollections.observableArrayList();
    private ObservableList<User> userData = FXCollections.observableArrayList();
    private ObservableList<Booking> userBookingsData = FXCollections.observableArrayList();
    private ObservableList<Booking> rosterData = FXCollections.observableArrayList();

    // ==========================================
    // GETTERS FOR DATA PERSISTENCE
    // ==========================================
    public UserManagement getUserManager() {
        return userManager;
    }

    public ObservableList<Event> getEventList() {
        return eventData;
    }

    public BookingManager getBookingManager() {
        return bookingManager;
    }

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
    @FXML private Button addEventBtn, cancelEventBtn;

    // ==========================================
    // TAB 3: BOOKING MANAGEMENT
    // ==========================================
    @FXML private TextField bookingUserIdInput, bookingEventIdInput;
    @FXML private Label bookingMessageLabel;
    @FXML private TableView<Booking> userBookingsTable;
    @FXML private TableColumn<Booking, String> bookingEventIdColumn, bookingEventTitleColumn, bookingStatusColumn;
    @FXML private Button bookEventBtn;

    // ==========================================
    // TAB 4: WAITLIST / ROSTER MANAGEMENT
    // ==========================================
    @FXML private ComboBox<String> rosterEventSelect;
    @FXML private TableView<Booking> rosterTable;
    @FXML private TableColumn<Booking, String> rosterUserIdColumn, rosterUserNameColumn, rosterStatusColumn;
    @FXML private TableColumn<Booking, LocalDateTime> rosterTimeColumn;
    @FXML private Button viewRosterBtn, removeRosterBtn, promoteBtn, demoteBtn;

    @FXML
    public void initialize() {
        // Dropdowns
        userTypeDropdown.setItems(FXCollections.observableArrayList("Student", "Staff", "Guest"));
        eventTypeDropdown.setItems(FXCollections.observableArrayList("Workshop", "Seminar", "Concert"));

        // User Table
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userTypeColumn.setCellValueFactory(new PropertyValueFactory<>("userType"));

        // Event Table
        idColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("eventtype"));
        extraInfoColumn.setCellValueFactory(new PropertyValueFactory<>("extrainfo"));

        // Booking Table
        bookingEventIdColumn.setCellValueFactory(new PropertyValueFactory<>("eventId"));
        bookingEventTitleColumn.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));
        bookingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Roster Table
        rosterUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        rosterUserNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        rosterStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        rosterTimeColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Load Data
        DataManager.loadInitialData(userManager, eventData, bookingManager);
        userData.setAll(userManager.getAllUsers());

        // Bind Tables
        userTable.setItems(userData);
        eventTable.setItems(eventData);
        userBookingsTable.setItems(userBookingsData);
        rosterTable.setItems(rosterData);

        updateRosterDropdown();

        // Button Actions
        addUserBtn.setOnAction(e -> handleAddUser());
        if (removeUserBtn != null) removeUserBtn.setOnAction(e -> handleRemoveUser());

        addEventBtn.setOnAction(e -> handleAddEvent());
        cancelEventBtn.setOnAction(e -> handleCancelEvent());

        bookEventBtn.setOnAction(e -> handleBookEvent());

        viewRosterBtn.setOnAction(e -> handleViewRoster());
        if (removeRosterBtn != null) removeRosterBtn.setOnAction(e -> handleRemoveFromRoster());
        if (promoteBtn != null) promoteBtn.setOnAction(e -> handlePromoteUser());
        if (demoteBtn != null) demoteBtn.setOnAction(e -> handleDemoteUser());
    }

    private void handleAddUser() {
        String id = userIdInput.getText();
        String name = userNameInput.getText();
        String email = userEmailInput.getText();
        String type = userTypeDropdown.getValue();

        if (id.isEmpty() || name.isEmpty() || type == null) {
            System.out.println("Error: Missing required fields.");
            return;
        }

        if (userManager.createUser(id, name, email, type)) {
            userData.setAll(userManager.getAllUsers());
            userIdInput.clear(); userNameInput.clear(); userEmailInput.clear();
            userTypeDropdown.setValue(null);
            System.out.println("User Created.");
        }
    }

    private void handleRemoveUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            bookingManager.getAllBookings().removeIf(b -> b.getUserId().equals(selected.getUserId()));
            userManager.removeUser(selected.getUserId());
            userData.remove(selected);
            rosterData.clear();
            System.out.println("User and associated bookings removed.");
        }
    }

    private void handleAddEvent() {
        try {
            String id = eventIdInput.getText();
            String title = eventTitleInput.getText();
            String type = eventTypeDropdown.getValue();

            if (id.isEmpty() || type == null || eventDateInput.getText().isEmpty()) {
                System.out.println("Error: Missing event details.");
                return;
            }

            LocalDateTime date = LocalDateTime.parse(eventDateInput.getText());
            String loc = eventLocationInput.getText();
            int cap = Integer.parseInt(eventCapacityInput.getText());
            String extra = eventExtraInfoInput.getText();

            Event newEvent = null;
            if (type.equals("Workshop")) newEvent = new Workshop(id, title, date, loc, cap, extra);
            else if (type.equals("Seminar")) newEvent = new Seminar(id, title, date, loc, cap, extra);
            else if (type.equals("Concert")) newEvent = new Concert(id, title, date, loc, cap, extra);

            if (newEvent != null) {
                eventData.add(newEvent);
                updateRosterDropdown();
                eventIdInput.clear(); eventTitleInput.clear(); eventDateInput.clear(); eventLocationInput.clear(); eventCapacityInput.clear(); eventExtraInfoInput.clear();
                System.out.println("Event Created.");
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            System.out.println("Error: Check Date format (YYYY-MM-DDTHH:MM) and Capacity (Number).");
        }
    }

    private void handleCancelEvent() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.cancelEvent();
            bookingManager.getAllBookings().stream()
                    .filter(b -> b.getEventId().equals(selected.getID()))
                    .forEach(b -> b.setStatus(BookingStatus.CANCELLED));
            eventTable.refresh();
            rosterData.clear();
        }
    }

    private void handleBookEvent() {
        String uId = bookingUserIdInput.getText().trim();
        String eId = bookingEventIdInput.getText().trim();

        User u = userManager.getUser(uId);
        Event e = eventData.stream().filter(ev -> ev.getID().equals(eId)).findFirst().orElse(null);

        if (u != null && e != null) {
            int confirmedCount = (int) bookingManager.getAllBookings().stream()
                    .filter(b -> b.getEventId().equals(eId) && b.getStatus() == BookingStatus.CONFIRMED)
                    .count();

            String newBookingId = "B" + (bookingManager.getAllBookings().size() + 1);
            String result = bookingManager.createBooking(
                    newBookingId, uId, u.getName(), u.getUserType(), eId, e.getTitle(), confirmedCount, e.getCapacity()
            );

            bookingMessageLabel.setText(result);
            refreshUserBookings(uId);
            handleViewRoster();

            bookingUserIdInput.clear(); bookingEventIdInput.clear();
        } else {
            bookingMessageLabel.setText("Error: User ID or Event ID not found.");
        }
    }

    private void refreshUserBookings(String userId) {
        userBookingsData.setAll(bookingManager.getAllBookings().stream()
                .filter(b -> b.getUserId().equals(userId))
                .collect(Collectors.toList()));
    }

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
            //call cancel booking to promote earliest waitlisted booking
            bookingManager.cancelBooking(selected.getBookingId());
            handleViewRoster(); //refresh roster table
            refreshUserBookings(selected.getUserId()); //refresh the user booking table
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