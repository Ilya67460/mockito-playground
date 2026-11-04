package pages;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getGreeting(int userId) {


        try {
            int normalizedId = Math.abs(userId);  // ← для @Captor
            String username = userRepository.findUsernameById(normalizedId);
            if (username == null) {
                return "Hello, guest!";
            }
            return "Hello, " + username + "!";
        } catch (DatabaseException e) {          // ← для thenThrow
            return "Hello, guest!";
        }
    }
}
