package com.example.spendee; // Thay đổi package nếu cần

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom; // Dùng cho salt
import android.util.Base64;

public class PasswordHasher {

    // !! CẢNH BÁO AN NINH: Hashing này RẤT CƠ BẢN và KHÔNG ĐỦ AN TOÀN !!
    // - Thiếu Salt: Cần tạo salt ngẫu nhiên cho mỗi mật khẩu.
    // - Thuật toán yếu: SHA-256 nhanh, dễ bị brute-force.
    // - Số lượt lặp (Iterations): Không có.
    // --> HÃY THAY BẰNG THƯ VIỆN BCRYPT hoặc ARGON2 cho ứng dụng thực tế.

    private static final String ALGORITHM = "SHA-256";

    // Chỉ để demo cách hoạt động cơ bản
    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        try {
            // --- Phần nên thêm (Salt) ---
            // SecureRandom random = new SecureRandom();
            // byte[] salt = new byte[16];
            // random.nextBytes(salt);
            // String encodedSalt = Base64.encodeToString(salt, Base64.NO_WRAP);
            // Lưu encodedSalt cùng với hash (ví dụ: salt:hash)
            // --- Kết thúc phần Salt ---

            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            // Hash kết hợp password + salt
            byte[] hashBytes = digest.digest(password.getBytes(/* StandardCharsets.UTF_8 */)); // Nên chỉ định charset

            // Chỉ trả về hash (thiếu salt)
            return Base64.encodeToString(hashBytes, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(); // Log lỗi cẩn thận
            return null;
        }
    }

    // Chỉ để demo cách hoạt động cơ bản
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty() || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }

        // --- Phần nên thêm (Salt) ---
        // Tách salt và hash đã lưu từ hashedPassword
        // Hash plainPassword với salt đã lưu
        // --- Kết thúc phần Salt ---

        // Hash lại mật khẩu nhập vào (không có salt trong ví dụ này)
        String hashOfInput = hash(plainPassword);

        // So sánh hash mới tạo với hash đã lưu
        return hashOfInput != null && hashOfInput.equals(hashedPassword);
    }
}