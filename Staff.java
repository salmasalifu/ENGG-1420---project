package user;

public class Staff extends User {
    /*
    Given the booking rules, the maximum booking for staff is 5, so the use of static 
    and final keywords repersent this value that cannot be changed
    */
    private static final int MAX_BOOKINGS = 5;

    /*
    Using inheritance rules to setup a constructor for the Staff class (subclass) from the User class (superclass)
    */
    public Staff(String userId, String name, String email, String userType) {
        super(userId, name, email, userType);
    }

     /*
    Using Overriding to create the same viewUserDetails function as the User class, but specifiying staff details
    like their max bookings
    */
    @Override
    public void viewUserDetails(){
        super.viewUserDetails();
        System.out.println("Max Bookings: " + MAX_BOOKINGS);
    }

    /*
    Using Overriding to create the same getUserType function as the User class, but specifiying staff userType
    */
    @Override
    public String getUserType() {
        return "staff";
    }

    /*
    Getter for the maximum bookings of staff
    */
    @Override
    public int getMaxBookings() {
        return MAX_BOOKINGS;
    }
}