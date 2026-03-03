package user;
import java.util.*;

/*
UserManagement class, used to manage users of the three distinct types
*/
public class UserManagement {
    
    /*
    Initialized ArrayList of all users is made to easily configure user data using the Arraylist methods
    */
    private ArrayList<User> users;

    /*
    A Constructor is made to initialize and set the ArrayList of users
    */
    public UserManagement() {
        this.users = new ArrayList<>();
    }

    /*
    The createUser method allows for user creation and addition to the arraylist by doing various validity checks beforehand
    given the input information
    */
    public boolean createUser(String userId, String name, String email, String userType) { 

        /*
        The method starts with 2 empty/null checks and an email format check
        , ensuring that there was no blank data entered for the userId, name or email 
        regardless of the userType. The email check specifically will also ensure that the all emails are entered with the specific format
        '____@___.__' ('.ca'/'.com' and all the other ones for different countries are disregarded for now)
        */
        if(userId == null || userId.trim().isEmpty()) {
            System.out.println("User ID cannot be empty.\n");
            return false;
        }

        if(name == null || name.trim().isEmpty()) {
            System.out.println("Name cannot be empty.\n");
            return false;
        }
     
         /*
        trim the parameters to ensure that no blank space is inputted for each data
        */
        userId = userId.trim();
        name = name.trim();
        email = email.trim();

        if(!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            System.out.println("Invalid email format.\n");
            return false;
        }

        /*
        Duplicate ID checker using userExistChecker helper method
        */
        if(userExistChecker(userId)) {
            System.out.println("User ID already exists.\n");
            return false;
        }

        /*
        After those passes are checked, a new user is created by calling the createUserByType method to specify for the userType chosen
        */
        User newUser = createUserByType(userId, name, email, userType);
        
        /*
        If this new User has blank parameters, the userType entered would be invalid since its the first check in the createUserByType method
        */
        if(newUser == null) {
            System.out.println("Invalid user type.\n");
            return false;
        }

        /*
        A User will then successfully be added to the arraylist of users if all the checks are passed
        */
        users.add(newUser);
        System.out.println("User was successfully created.\n");
        return true;
    }

    /*
    createUserByType method, which will be a helper method for the createUser method when dealing with userType decision
    */
    private User createUserByType(String userId, String name, String email, String userType) {  
        /*
        The first check is if the userType entered is blank or null, which will then return the entire User as null
        */
        if (userType == null) {
            return null;
        }

        /*
        The userType is then set to lowercase and trimmed to prevent Case or blank space issues
        */
        userType = userType.toLowerCase().trim();

        /*
        The method will now check for each of the three possible user types and set them accordingly by creating a new Student/Staff/Guest class
        */
        if(userType.equals("student")) {
            return new Student(userId, name, email, "student");
        }
        if(userType.equals("staff")) {
            return new Staff(userId, name, email, "staff");
        }
        if(userType.equals("guest")) {
            return new Guest(userId, name, email,"guest");
        }

        /*
        If the userType was not blank, but also not one of the three required types, the User will also be null (must be student or staff or guest)
        */
        return null;
    }

    /*
    findUserById method allows for simple searching from the users ID by looping through the arraylist of users
    and searching for equivalent user ID's to the one that was inputted
    */
    private User findUserById(String userId) {
        for(User user : users) {
            if(user.getUserId().equalsIgnoreCase(userId)) {
                return user;
            }
        }
        /*
        If no matches are found in the users arraylist, return nothing
        */
        return null;
    }

    /*
    Using the findUserById method as a helper method to easily get users
    */
    public User getUser(String userId) {
        return findUserById(userId);
    }

    /*
    The findUserById method is used once again to view user details by running a check for valid users
    */
    public void viewUserDetails(String userId) {
        User user = findUserById(userId);

        if(user == null) {
            System.out.printf("User ID '%s' was not found.\n", userId);
        }
        else{
            user.viewUserDetails();
        }
    }

    /*
    listAllUsers method allows for a non GUI list temporarily of all the registered users in the users arraylist
    */
    public void listAllUsers() {

        if(users.isEmpty()) {
            System.out.println("No users are registered in the system.\n");
            return;
        }

        System.out.println("All Registered Users:\n");

        /*
        Format specifier editing in order to format the list when executed
        */
        System.out.printf("%-10s %-25s %-30s %-10s%n","User ID", "Name", "Email", "Type");

        /*
        Loop through and output all the users in the list
        */
        for(User user : users) {
            System.out.printf("%-10s %-25s %-30s %-10s%n",
            user.getUserId(), user.getName(), user.getEmail(), user.getUserType());
        }
    }

    /*
    userExistChecker checks if the user exists using the findUserById method
    */
    private boolean userExistChecker(String userId) {
        return findUserById(userId) != null;
    }

    /*
    getUserCount gets the size of all the users in the users arraylist
    */
    public int getUserCount() {
        return users.size();
    }

    /*
    removeUser allows for removing non blank users from the arraylist by using the .remove method
    */
    public boolean removeUser(String userId) {
        User user = findUserById(userId);
        
        if (user != null) {
            users.remove(user);
            System.out.printf("User '%s' was successfully removed.\n", userId);
            return true;
        }

        /*
        if the user was null (blank), this error message shows
        */
        System.out.printf("User '%s' was not found.\n", userId);
        return false;
    }

    /*
    getter for all the users in the arraylist
    */
    public ArrayList<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /*
    clearing all the users using the .clear method
    */
    public void clearAllUsers() {
        users.clear();
        System.out.println("All users were cleared from the system.\n");
    }

}
