import com.haha.util.PasswordUtils;

import java.util.Scanner;

public class CreatePassword {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String password = scanner.nextLine();
        System.out.println("Password: " + PasswordUtils.hashPassword(password));
        scanner.close();
    }
}
