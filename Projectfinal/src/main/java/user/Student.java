package user;

public class Student extends User {
    /*
    Given the booking rules, the maximum booking for students is 3, so the use of static 
    and final keywords repersent this value that cannot be changed
    */
    private static final int MAX_BOOKINGS = 3;

    /*
    Using inheritance rules to setup a constructor for the Student class (subclass) from the User class (superclass)
    */
    public Student(String userId, String name, String email, String userType) {
        super(userId, name, email, userType);
    }

    /*
    Using Overriding to create the same viewUserDetails function as the User class, but specifiying student details
    like their max bookings
    */
    @Override
    public void viewUserDetails(){
        super.viewUserDetails();
        System.out.println("Max Bookings: " + MAX_BOOKINGS);
    }

    /*
    Using Overriding to create the same getUserType function as the User class, but specifiying student userType
    */
    @Override
    public String getUserType() {
        return "student";
    }

    /*
    Getter for the maximum bookings of students
    */
    @Override
    public int getMaxBookings() {
        return MAX_BOOKINGS;
    }
}