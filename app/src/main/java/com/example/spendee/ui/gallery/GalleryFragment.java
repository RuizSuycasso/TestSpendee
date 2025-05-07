package com.example.spendee.ui.gallery; // Giữ nguyên package

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // Vẫn có thể giữ nếu dùng ViewModel

import com.example.spendee.AppDatabase; // Import AppDatabase
import com.example.spendee.MainActivity; // Import MainActivity để gọi update header (optional)
import com.example.spendee.PasswordHasher; // Import PasswordHasher
import com.example.spendee.User;         // Import User entity
import com.example.spendee.UserDAO;      // Import UserDAO
import com.example.spendee.databinding.FragmentGalleryBinding; // Giữ ViewBinding

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private EditText edtName, edtPhone, edtAddress;
    private TextView tvUsername; // Chỉ hiển thị, không sửa
    private Button btnSave, btnChangePassword;
    private AppDatabase db;
    private SharedPreferences loginPrefs;
    private User currentUser; // Lưu user đang hiển thị/sửa

    // Constants cho SharedPreferences (phải khớp với MainActivity)
    private static final String LOGIN_PREFS_NAME = "LoginStatus";
    private static final String LOGGED_IN_USER_ID_KEY = "logged_in_user_id";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // ViewModel setup (nếu có)
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = AppDatabase.getInstance(requireContext());
        loginPrefs = requireActivity().getSharedPreferences(LOGIN_PREFS_NAME, android.content.Context.MODE_PRIVATE);

        // Ánh xạ views từ binding
        edtName = binding.edtName;
        edtPhone = binding.edtPhone;
        edtAddress = binding.edtAddress;
        tvUsername = binding.tvUsername; // Username hiển thị trên TextView
        btnSave = binding.btnSave;
        btnChangePassword = binding.btnChangePassword;

        // Tải thông tin người dùng từ DB dựa trên ID đã lưu
        loadUserInfo();

        // Xử lý sự kiện click
        btnSave.setOnClickListener(v -> attemptSaveUserInfo());
        btnChangePassword.setOnClickListener(v -> {
            if (currentUser != null) { // Chỉ hiển thị dialog nếu đã tải xong user
                showChangePasswordDialog();
            } else {
                Toast.makeText(requireContext(), "Đang tải thông tin...", Toast.LENGTH_SHORT).show();
            }
        });

        // ViewModel text observer (nếu có sử dụng)
        final TextView textView = binding.textGallery;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    private void loadUserInfo() {
        int loggedInUserId = loginPrefs.getInt(LOGGED_IN_USER_ID_KEY, -1);
        if (loggedInUserId != -1) {
            // Lấy thông tin user từ DB trên background thread
            new LoadUserForGalleryTask().execute(loggedInUserId);
        } else {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy ID người dùng.", Toast.LENGTH_SHORT).show();
            // Cần xử lý tốt hơn, ví dụ điều hướng về login hoặc hiển thị lỗi rõ ràng
        }
    }

    // AsyncTask để tải thông tin User cho màn hình Gallery
    private class LoadUserForGalleryTask extends AsyncTask<Integer, Void, User> {
        @Override
        protected User doInBackground(Integer... userIds) {
            if (userIds.length > 0 && userIds[0] != -1) {
                try {
                    return db.userDAO().findById(userIds[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            currentUser = user; // Lưu lại user hiện tại để sửa/đổi mk
            if (currentUser != null) {
                // Điền thông tin lên các trường EditText và TextView
                edtName.setText(currentUser.getName());
                tvUsername.setText(currentUser.getUsername()); // Hiển thị username (không sửa)
                edtPhone.setText(currentUser.getPhone());
                edtAddress.setText(currentUser.getAddress());
                // Không điền mật khẩu vào bất kỳ đâu
            } else {
                Toast.makeText(requireContext(), "Không tải được thông tin người dùng", Toast.LENGTH_SHORT).show();
                // Xử lý lỗi, có thể disable các nút hoặc hiển thị thông báo
                btnSave.setEnabled(false);
                btnChangePassword.setEnabled(false);
            }
        }
    }

    private void attemptSaveUserInfo() {
        // Kiểm tra xem currentUser đã được tải chưa
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Thông tin người dùng chưa sẵn sàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        // Cập nhật các trường của đối tượng currentUser trong bộ nhớ
        boolean changed = false;
        if (!name.equals(currentUser.getName())) {
            currentUser.setName(name);
            changed = true;
        }
        if (!phone.equals(currentUser.getPhone())) {
            currentUser.setPhone(phone);
            changed = true;
        }
        if (!address.equals(currentUser.getAddress())) {
            currentUser.setAddress(address);
            changed = true;
        }


        if (changed) {
            // Nếu có thay đổi, thực hiện cập nhật vào DB trên background thread
            new UpdateUserTask().execute(currentUser);
        } else {
            Toast.makeText(requireContext(), "Không có thay đổi để lưu.", Toast.LENGTH_SHORT).show();
        }

    }

    // AsyncTask để cập nhật thông tin User vào DB
    private class UpdateUserTask extends AsyncTask<User, Void, Boolean> {
        private boolean isPasswordChange = false; // Biến để kiểm tra xem có phải đổi MK không

        // Constructor để nhận biết là đổi MK hay chỉ cập nhật thông tin
        public UpdateUserTask(boolean isPasswordChange) {
            this.isPasswordChange = isPasswordChange;
        }
        public UpdateUserTask() { this(false); } // Constructor mặc định


        @Override
        protected Boolean doInBackground(User... users) {
            if (users.length > 0 && users[0] != null) {
                try {
                    db.userDAO().update(users[0]);
                    return true; // Cập nhật thành công
                } catch (Exception e) {
                    e.printStackTrace(); // Log lỗi DB
                    return false; // Cập nhật thất bại
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                if (isPasswordChange) {
                    Toast.makeText(requireContext(), "Mật khẩu đã được thay đổi!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Thông tin đã được cập nhật", Toast.LENGTH_SHORT).show();
                    // (Optional) Cập nhật header trong MainActivity nếu thông tin tên thay đổi
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateNavHeader();
                    }
                }
            } else {
                if (isPasswordChange) {
                    Toast.makeText(requireContext(), "Lỗi khi đổi mật khẩu!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Lỗi khi cập nhật thông tin!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Hiển thị Dialog để đổi mật khẩu
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Đổi mật khẩu");

        // Tạo layout cho dialog bằng code (hoặc dùng file layout riêng)
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density); // 16dp padding
        layout.setPadding(padding, padding, padding, padding);

        // Input mật khẩu cũ
        final EditText oldPasswordInput = new EditText(requireContext());
        oldPasswordInput.setHint("Mật khẩu cũ");
        oldPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int marginBottom = (int) (8 * getResources().getDisplayMetrics().density); // 8dp margin
        params.setMargins(0, 0, 0, marginBottom);
        oldPasswordInput.setLayoutParams(params);
        layout.addView(oldPasswordInput);

        // Input mật khẩu mới
        final EditText newPasswordInput = new EditText(requireContext());
        newPasswordInput.setHint("Mật khẩu mới");
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPasswordInput.setLayoutParams(new LinearLayout.LayoutParams(params)); // Copy params
        layout.addView(newPasswordInput);

        // Input xác nhận mật khẩu mới
        final EditText confirmNewPasswordInput = new EditText(requireContext());
        confirmNewPasswordInput.setHint("Xác nhận mật khẩu mới");
        confirmNewPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmNewPasswordInput.setLayoutParams(new LinearLayout.LayoutParams(params)); // Copy params
        layout.addView(confirmNewPasswordInput);

        builder.setView(layout);

        // Nút "Đổi"
        builder.setPositiveButton("Đổi", (dialog, which) -> {
            String oldPassword = oldPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmNewPassword = confirmNewPasswordInput.getText().toString().trim();

            // Gọi hàm xử lý đổi mật khẩu
            attemptChangePassword(oldPassword, newPassword, confirmNewPassword);
            // Không đóng dialog ở đây, để đóng sau khi AsyncTask thành công/thất bại nếu muốn
        });

        // Nút "Hủy"
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Xử lý logic đổi mật khẩu
    private void attemptChangePassword(String oldPassword, String newPassword, String confirmPassword) {
        // Kiểm tra lại currentUser (dù đã kiểm tra trước khi gọi dialog)
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Thông tin người dùng không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Xác thực mật khẩu cũ với hash đã lưu
        if (!PasswordHasher.verify(oldPassword, currentUser.getPasswordHash())) {
            Toast.makeText(requireContext(), "Mật khẩu cũ không đúng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Kiểm tra mật khẩu mới và xác nhận
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Mật khẩu mới không được bỏ trống!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "Xác nhận mật khẩu mới không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }
        // (Optional) Kiểm tra độ phức tạp mật khẩu mới ở đây
        // (Optional) Kiểm tra mật khẩu mới có trùng mật khẩu cũ không

        // 3. Băm mật khẩu mới
        String newHashedPassword = PasswordHasher.hash(newPassword);
        if (newHashedPassword == null) {
            Toast.makeText(requireContext(), "Lỗi khi tạo mật khẩu mới!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Cập nhật hash mật khẩu mới cho đối tượng currentUser
        currentUser.setPasswordHash(newHashedPassword);

        // 5. Lưu thay đổi vào DB bằng AsyncTask (đánh dấu là đổi mật khẩu)
        new UpdateUserTask(true).execute(currentUser);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Quan trọng để tránh memory leak
        currentUser = null; // Giải phóng tham chiếu user
    }
}