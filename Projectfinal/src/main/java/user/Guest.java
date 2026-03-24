package user;

public class Guest extends User {
    /*
    Given the booking rules, the maximum booking for guests is 1, so the use of static 
    and final keywords repersent this value that cannot be changed
    */
    private static final int MAX_BOOKINGS = 1;

    
    /*
    Using inheritance rules to setup a constructor for the Guest class (subclass) from the User class (superclass)
    */
    public Guest(String userId, String name, String email, String userType) {
        super(userId, name, email, userType);
    }
   
     /*
    Using Overriding to create the same viewUserDetails function as the User class, but specifiying guest details
    like their max bookings
    */
    @Override
    public void viewUserDetails(){
        super.viewUserDetails();
        System.out.println("Max Bookings: " + MAX_BOOKINGS);
    }

    /*
    Using Overriding to create the same getUserType function as the User class, but specifiying guest userType
    */
    @Override
    public String getUserType() {
        return "guest";
    }

    /*
    Getter for the maximum bookings of guest
    */
    @Override
    public int getMaxBookings() {
        return MAX_BOOKINGS;
    }
}
