package user;

public abstract class User {

    /*
    Initial variables repersenting the fundamental data from a User:
    The User's ID, their Name, their Email, as well as their User Type

    User types range from:
    Student users, Staff users and Guest users
    */
    protected String userId;
    protected String name;
    protected String email;
    protected String userType;

    /*
    Abstract class is used since the subclasses only will ultilize this method for their respective maximum bookings
    */
    public abstract int getMaxBookings();

    /*
    The User constructor, allowing for easy access to User information
    */
    public User(String userId, String name, String email, String userType) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.userType = userType;
    }

    /*
    The User class getters
    */
    public String getUserId() {
        return userId;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getUserType() {
        return userType;
    }

    /*
    The User class setters
    */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /*
    The viewUserDetails method, which allows for printing user data that was entered for tracking
    */
    public void viewUserDetails(){
        System.out.printf("User ID: %s\nName: %s\nEmail: %s\nUser Type: %s\n", userId, name, email, userType);
    }
}