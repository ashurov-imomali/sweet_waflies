import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserAuth {
    public static Map<Boolean, Integer> authenticate(String username, String password) {
        String query = "SELECT username, password, employee_id FROM users_auth WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("employee_id");
                System.out.println(id + " " + username);
                return Map.of(Boolean.TRUE, id);
            }
            return Map.of(Boolean.FALSE, Integer.valueOf(0));
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of(false, 0);
        }
    }
}
