package ong.myapp.studentmanagement;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ong.myapp.studentmanagement.adapter.StudentAdapter;
import ong.myapp.studentmanagement.model.Student;

public class StudentActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private static final Logger log = LogManager.getLogger(StudentActivity.class);
    private static final String TAG = "StudentActivity";
    private static final int CREATE_FILE_REQUEST_CODE = 1;
    private static final int PICK_FILE_REQUEST_CODE = 100;
    private Uri imageUri;
    private List<Student> studentList;
    private StudentAdapter studentAdapter;
    private EditText edSearch;

    private Toolbar toolbar;
    private GridView gridView;
    private ImageView dialogAvatarImageView;

    private FirebaseFirestore db;

    private List<Student> filteredStudent;
    private TextView tvNoData;
    ImageButton btnBack;
    private String userRole;
    private String avatarUrl = "";
    private DialogViewHolderStudent dialogViewHolderStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "StudentActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        _init();

        setSupportActionBar(toolbar);

        studentAdapter = new StudentAdapter(this, studentList);
        gridView.setAdapter(studentAdapter);

        FloatingActionButton fabAddStudent = findViewById(R.id.fab_addStudent);
        fabAddStudent.setOnClickListener(view -> showAddStudentDialog());
        btnBack.setOnClickListener(v -> onBackPressed());
        readStudentFromDB();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Student selectedStudent = studentList.get(position);
            showInformationStudentDialog(selectedStudent);
        });

        ImageView imvBack = findViewById(R.id.btnBack);
        imvBack.setOnClickListener(view -> finish());
        userRole = getIntent().getStringExtra("role");
        if (userRole.equalsIgnoreCase("Employee")) {
            toolbar.setFocusable(false);
            toolbar.setFocusableInTouchMode(false);
            toolbar.setClickable(false);

            fabAddStudent.setFocusable(false);
            fabAddStudent.setClickable(false);
        }

        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String searchText = s.toString().trim();
                if (!searchText.isEmpty()) {
                    filterStudents(searchText);
                } else {
                    studentAdapter.updateList(studentList);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (userRole.equalsIgnoreCase("Employee")) {
                return true;
            }
            Student selectedStudent = studentList.get(position);

            new AlertDialog.Builder(this)
                    .setTitle("Xóa sinh viên")
                    .setMessage("Bạn có chắc chắn muốn xóa sinh viên này không?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Xóa sinh viên từ Firestore
                        db.collection("Student").document(selectedStudent.getStudentId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    studentList.remove(selectedStudent);
                                    studentAdapter.updateList(studentList);

                                    // Thông báo người dùng
                                    Toast.makeText(this, "Xóa thành công!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Thông báo lỗi khi xóa
                                    Toast.makeText(this, "Lỗi khi xóa!", Toast.LENGTH_SHORT).show();
                                    Log.e("Firestore", "Lỗi khi xóa sinh viên", e);
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();

            return true;
        });

    }

    private void _init() {
        toolbar = findViewById(R.id.toolbar);
        gridView = findViewById(R.id.gv_student);
        btnBack = findViewById(R.id.btnBack);


        studentList = new ArrayList<>();
        filteredStudent = new ArrayList<>(studentList);
        edSearch = findViewById(R.id.editSearch);
        db = FirebaseFirestore.getInstance();
        tvNoData = findViewById(R.id.tvNoData);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_student, menu);

        return true;
    }

    private boolean validateStudentInputs(EditText edtFullName, EditText edtBirthday, EditText edtClass,
                                          EditText edtFaculty, EditText edtTeacher,
                                          TextInputLayout layoutFullName, TextInputLayout layoutBirthday,
                                          TextInputLayout layoutClass, TextInputLayout layoutFaculty, TextInputLayout layoutTeacher) {

        String fullName = edtFullName.getText().toString().trim();
        String birthday = edtBirthday.getText().toString().trim();
        String className = edtClass.getText().toString().trim();
        String faculty = edtFaculty.getText().toString().trim();
        String teacher = edtTeacher.getText().toString().trim();

        if (fullName.isEmpty()) {
            layoutFullName.setError("Họ và tên không được để trống");
            edtFullName.requestFocus();
            return false;
        }

        if (birthday.isEmpty()) {
            layoutBirthday.setError("Ngày sinh không được để trống");
            edtBirthday.requestFocus();
            return false;
        }

        if (className.isEmpty()) {
            layoutClass.setError("Lớp không được để trống");
            edtClass.requestFocus();
            return false;
        }

        if (faculty.isEmpty()) {
            layoutFaculty.setError("Khoa không được để trống");
            edtFaculty.requestFocus();
            return false;
        }

        if (teacher.isEmpty()) {
            layoutTeacher.setError("Giáo viên không được để trống");
            edtTeacher.requestFocus();
            return false;
        }

        return true;
    }

    private void showAddStudentDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Thêm sinh viên mới");

        DialogViewHolderStudent viewHolder = DialogViewHolderStudent.setupDialogView(this, null, false);
        builder.setView(viewHolder.tv_studentId.getRootView());

        String generatedUserId = "STU" + String.format("%03d", (int) (Math.random() * 999 + 1));
        viewHolder.tv_studentId.setText(generatedUserId);

        viewHolder.tv_studentId.setFocusable(false);
        viewHolder.tv_studentId.setClickable(false);

        viewHolder.tv_birthday.setFocusable(false);
        viewHolder.tv_birthday.setFocusableInTouchMode(false);

        viewHolder.tv_birthday.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                viewHolder.tv_birthday.setText(String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear));
            }, year, month, day);

            datePickerDialog.show();
        });


        builder.setPositiveButton("Thêm", null);
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        android.app.AlertDialog dialog = builder.create();
        dialog.show();

        Button addButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        addButton.setOnClickListener(v -> {
            viewHolder.layoutFullName.setError(null);
            viewHolder.layoutBirthday.setError(null);
            viewHolder.layoutClass.setError(null);
            viewHolder.layoutFaculty.setError(null);
            viewHolder.layoutTeacher.setError(null);
            if (validateStudentInputs(viewHolder.tv_fullName, viewHolder.tv_birthday, viewHolder.tv_class,
                    viewHolder.tv_faculty, viewHolder.tv_teacher, viewHolder.layoutFullName, viewHolder.layoutBirthday,
                    viewHolder.layoutClass, viewHolder.layoutFaculty, viewHolder.layoutTeacher)) {
                if (avatarUrl == null || avatarUrl.isEmpty()) {
                    Toast.makeText(this, "Bạn chưa tải ảnh hoặc Đang tải ảnh lên, vui lòng chờ...", Toast.LENGTH_SHORT).show();
                } else {
                    createStudent(viewHolder, dialog);
                }
            }
        });

        viewHolder.iv_student.setOnClickListener(v -> openFileChooser(viewHolder.iv_student));
        this.dialogViewHolderStudent = viewHolder;
        tvNoData.setVisibility(View.GONE);
        ;
    }


    private void createStudent(DialogViewHolderStudent viewHolder, android.app.AlertDialog dialog) {
        String studentId = viewHolder.tv_studentId.getText().toString().trim();
        String name = viewHolder.tv_fullName.getText().toString().trim();
        String birthday = viewHolder.tv_birthday.getText().toString().trim();
        String faculty = viewHolder.tv_faculty.getText().toString().trim();
        String teacher = viewHolder.tv_teacher.getText().toString().trim();
        String classname = viewHolder.tv_class.getText().toString().trim();

        Student s = new Student(studentId, name, birthday, classname, faculty, avatarUrl, teacher);
        s.saveToFirebase();
        studentList.add(s);
        studentAdapter.updateList(studentList);

        Toast.makeText(this, "Thêm tài khoản thành công", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        avatarUrl = "";
        imageUri = null;

    }


    private void showUpdatesStudentDialog(Student student) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Cập nhật thông tin sinh viên");
        avatarUrl = student.getImageURL();

        DialogViewHolderStudent viewHolder = DialogViewHolderStudent.setupDialogView(this, student, true);
        builder.setView(viewHolder.tv_studentId.getRootView());
        viewHolder.tv_birthday.setFocusable(false);
        viewHolder.tv_birthday.setFocusableInTouchMode(false);

        viewHolder.tv_birthday.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                viewHolder.tv_birthday.setText(String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear));
            }, year, month, day);

            datePickerDialog.show();
        });


        builder.setPositiveButton("Cập nhật", null);
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        android.app.AlertDialog dialog = builder.create();
        dialog.show();

        Button updateButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        updateButton.setOnClickListener(v -> {
            viewHolder.layoutFullName.setError(null);
            viewHolder.layoutBirthday.setError(null);
            viewHolder.layoutClass.setError(null);
            viewHolder.layoutFaculty.setError(null);
            viewHolder.layoutTeacher.setError(null);
            if (validateStudentInputs(viewHolder.tv_fullName, viewHolder.tv_birthday, viewHolder.tv_class,
                    viewHolder.tv_faculty, viewHolder.tv_teacher, viewHolder.layoutFullName, viewHolder.layoutBirthday,
                    viewHolder.layoutClass, viewHolder.layoutFaculty, viewHolder.layoutTeacher)) {

                if (imageUri != null) {
                    if (!avatarUrl.equals(student.getImageURL())) {
                        updateStudent(viewHolder, student, dialog);

                    } else {
                        Toast.makeText(this, "Đang tải ảnh lên, vui lòng chờ...", Toast.LENGTH_SHORT).show();
                        updateButton.setEnabled(false);
                        uploadImageToFirebase(dialogViewHolderStudent.progressBar1, () -> updateStudent(viewHolder, student, dialog));
                    }
                    imageUri = null;

                } else {
                    updateStudent(viewHolder, student, dialog);
                }
            }

        });

        viewHolder.iv_student.setOnClickListener(v -> openFileChooser(viewHolder.iv_student));
        this.dialogViewHolderStudent = viewHolder;
        ;
    }

    private void updateStudent(DialogViewHolderStudent viewHolder, Student student, android.app.AlertDialog dialog) {
        String studentId = viewHolder.tv_studentId.getText().toString().trim();
        String name = viewHolder.tv_fullName.getText().toString().trim();
        String birthday = viewHolder.tv_birthday.getText().toString().trim();
        String faculty = viewHolder.tv_faculty.getText().toString().trim();
        String teacher = viewHolder.tv_teacher.getText().toString().trim();
        String classId = viewHolder.tv_class.getText().toString().trim();
        student.setStudentId(studentId);
        student.setName(name);
        student.setBirthday(birthday);
        student.setFacultyId(faculty);
        student.setTeacher(teacher);
        student.setClassname(classId);
        student.setImageURL(avatarUrl);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Student").document(studentId).set(student)
                .addOnSuccessListener(aVoid -> {
                    for (int i = 0; i < studentList.size(); i++) {
                        if (studentList.get(i).getStudentId().equals(studentId)) {
                            studentList.set(i, student);
                            break;
                        }
                    }
                    dialog.dismiss();
                    studentAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        avatarUrl = "";
    }

    private void openFileChooser(ImageView iv_avatar) {
        dialogAvatarImageView = iv_avatar;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (dialogAvatarImageView != null) {
                dialogAvatarImageView.setImageURI(imageUri);
                {
                    uploadImageToFirebase(dialogViewHolderStudent.progressBar1, () -> {
                    });

                }
            }
        }

        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            if (requestCode == CREATE_FILE_REQUEST_CODE && uri != null) {
                String mimeType = getContentResolver().getType(uri);
                if ("text/csv".equals(mimeType)) {
                    exportToCSV(uri);
                } else if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(mimeType)) {
                    exportToExcel(uri);
                }
            } else if (requestCode == PICK_FILE_REQUEST_CODE && uri != null) {
                String fileName = getFileName(uri);


                if (fileName.endsWith(".csv")) {
                    importFromCSV(uri);
                } else if (fileName.endsWith(".xlsx")) {
                    importFromExcel(uri);
                } else {
                    Toast.makeText(this, "Tệp không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }

        return result;
    }

    private void showInformationStudentDialog(Student student) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_detail_student, null);
        builder.setView(dialogView);


        ImageView imgDetail = dialogView.findViewById(R.id.img_detail);
        TextView tvId=dialogView.findViewById(R.id.tv_idDetail);
        TextView tvNameDetail = dialogView.findViewById(R.id.tv_nameDetail);
        TextView tvAgeDetail = dialogView.findViewById(R.id.tv_birthdayDetail);
        TextView tvClassDetail = dialogView.findViewById(R.id.tv_classDetail);
        TextView tvFacultyDetail = dialogView.findViewById(R.id.tv_facultyDetail);
        TextView tvTeacher = dialogView.findViewById(R.id.tv_teacher);


        Glide.with(this)
                .load(student.getImageURL())
                .into(imgDetail);

        tvNameDetail.setText(student.getName());
        tvAgeDetail.setText(student.getBirthday());
        tvClassDetail.setText(student.getClassname());
        tvFacultyDetail.setText(student.getFacultyId());
        tvTeacher.setText(student.getTeacher());
        tvId.setText(student.getStudentId());


        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
        if (!userRole.equalsIgnoreCase("Employee")) {
            builder.setNegativeButton("Chỉnh sửa", (dialog, which) -> {
                dialog.dismiss();
                showUpdatesStudentDialog(student);
            });
        }


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void readStudentFromDB() {
        db.collection("Student").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        List<Student> students = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Student student = documentSnapshot.toObject(Student.class);
                            students.add(student);
                        }
                        studentList.clear();
                        studentList.addAll(students);
                        filteredStudent.clear();
                        filteredStudent.addAll(students);
                        studentAdapter.updateList(filteredStudent);
                    } else {
                        tvNoData.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi đọc dữ liệu: ", e);
                    Toast.makeText(StudentActivity.this, "Không thể tải dữ liệu.", Toast.LENGTH_SHORT).show();
                });
    }


    private void uploadImageToFirebase(ProgressBar progressBar, Runnable onSuccessCallback) {
        if (imageUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            StorageReference fileReference = FirebaseStorage.getInstance()
                    .getReference("uploads/" + System.currentTimeMillis() + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        avatarUrl = uri.toString();
                        progressBar.setVisibility(View.GONE);

                        Log.d("Upload", "Tải ảnh thành công, URL: " + avatarUrl);
                        onSuccessCallback.run();
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Upload thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
        } else {
            onSuccessCallback.run();
        }
    }

    private void filterStudents(String query) {
        if (query == null) {
            query = "";
        }
        query = query.toLowerCase();


        List<Student> filteredStudent = new ArrayList<>();


        for (Student student : studentList) {
            boolean matches = false;
            if (student.getName() != null && student.getName().toLowerCase().contains(query)) {
                matches = true;
            } else if (student.getFacultyId() != null && student.getFacultyId().toLowerCase().contains(query)) {
                matches = true;
            } else if (student.getClassname() != null && student.getClassname().toLowerCase().contains(query)) {
                matches = true;
            } else if (student.getTeacher() != null && student.getTeacher().toLowerCase().contains(query)) {
                matches = true;
            }

            if (matches) {
                filteredStudent.add(student);
            }
        }

        // Update the adapter's data and notify it
        studentAdapter.updateList(filteredStudent);  // Ensure your adapter has a method to update the list
        studentAdapter.notifyDataSetChanged();

        // Handle search result message
        TextInputLayout layoutSearch = findViewById(R.id.layoutSearch);
        if (filteredStudent.isEmpty()) {
            layoutSearch.setError("Không tìm thấy sinh viên");
        } else {
            layoutSearch.setError(null);
        }
    }

    private void deleteAllStudents() {
        db.collection("Student")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection("Student").document(document.getId()).delete()
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Xóa thành công: " + document.getId()))
                                .addOnFailureListener(e -> Log.e("Firestore", "Lỗi khi xóa", e));
                    }

                    studentList.clear();
                    studentAdapter.updateList(studentList);
                    Toast.makeText(this, "Xóa tất cả dữ liệu thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi lấy dữ liệu", e);
                    Toast.makeText(this, "Lỗi khi xóa dữ liệu!", Toast.LENGTH_SHORT).show();
                });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (userRole.equalsIgnoreCase("employee")) {
            return false;
        }
        if (itemId == R.id.menu_delete_student) {
            showDeleteConfirmationDialog();
            return true;
        } else if (itemId == R.id.menu_sort) {
            showPopupSort();
            return true;
        } else if (itemId == R.id.menu_import) {
           openFileChooser();
            return true;
        } else if (itemId == R.id.menu_export) {
            showExportOptions();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tất cả dữ liệu")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ dữ liệu không?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAllStudents())
                .setNegativeButton("No", null)
                .show();
    }

    private void sortStudentsByName() {
        if (studentList == null || studentAdapter == null) {
            Log.e("StudentActivity", "studentList hoặc studentAdapter bị null khi sắp xếp theo tên");
            return;
        }
        Collections.sort(studentList, new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                if (s1.getName() != null && s2.getName() != null) {
                    return s1.getName().compareToIgnoreCase(s2.getName());
                } else if (s1.getName() == null) {
                    return -1; // null sẽ đứng trước
                } else {
                    return 1; // null sẽ đứng sau
                }
            }
        });
        studentAdapter.updateList(studentList);
    }



    public void showPopupSort() {
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.menu_sort));

        popupMenu.getMenu().add("Sắp xếp theo tên");
        popupMenu.getMenu().add("Sắp xếp theo ngày sinh");
        popupMenu.getMenu().add("Sắp xếp theo ID");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item != null) {
                    Log.d("StudentActivity", "Selected menu item: " + item.getTitle().toString());
                } else {
                    Log.e("StudentActivity", "MenuItem is null");
                    return false;
                }

                switch (item.getTitle().toString()) {
                    case "Sắp xếp theo tên":
                        sortStudentsByName();
                        return true;
                    case "Sắp xếp theo ngày sinh":
                        sortStudentsByBirthdayAsync();
                        return true;
                    case "Sắp xếp theo ID":
                        sortStudentsById();
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }
    private void sortStudentsById() {
        if (studentList == null || studentAdapter == null) {
            Log.e("StudentActivity", "studentList hoặc studentAdapter bị null khi sắp xếp theo ID");
            return;
        }

        studentList.removeIf(student -> student == null);


        Collections.sort(studentList, new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                if (s1 == null && s2 == null) {
                    return 0;
                } else if (s1 == null) {
                    return 1;
                } else if (s2 == null) {
                    return -1;
                }

                if (s1.getStudentId() != null && s2.getStudentId() != null) {
                    try {
                        int id1 = extractNumericPart(s1.getStudentId());
                        int id2 = extractNumericPart(s2.getStudentId());
                        return Integer.compare(id1, id2);
                    } catch (NumberFormatException e) {
                        Log.e("StudentActivity", "Lỗi định dạng số: " + e.getMessage());
                        return s1.getStudentId().compareTo(s2.getStudentId());
                    }
                } else if (s1.getStudentId() == null) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        studentAdapter.updateList(studentList);
    }

    private int extractNumericPart(String studentId) {
        return Integer.parseInt(studentId.replaceAll("[^0-9]", ""));
    }




    @SuppressLint("StaticFieldLeak")
    private void sortStudentsByBirthdayAsync() {
        if (studentList == null || studentAdapter == null) {
            Log.e("StudentActivity", "studentList hoặc studentAdapter bị null khi sắp xếp theo ngày sinh");
            return;
        }
        new AsyncTask<Void, Void, List<Student>>() {
            @Override
            protected List<Student> doInBackground(Void... voids) {

                Collections.sort(studentList, new Comparator<Student>() {
                    @Override
                    public int compare(Student s1, Student s2) {
                        if (s1 == null || s2 == null) {
                            return 0;
                        }
                        if (s1.getBirthday() == null && s2.getBirthday() == null) {
                            return 0;
                        } else if (s1.getBirthday() == null) {
                            return 1;
                        } else if (s2.getBirthday() == null) {
                            return -1;
                        }
                        return s1.getBirthday().compareTo(s2.getBirthday());
                    }
                });
                return studentList;
            }

            @Override
            protected void onPostExecute(List<Student> result) {
                super.onPostExecute(result);
                studentAdapter.updateList(studentList);
            }
        }.execute();
    }




    private void showExportOptions() {
        String[] exportOptions = {"Xuất sang CSV", "Xuất sang Excel"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Chọn định dạng xuất dữ liệu");
        builder.setItems(exportOptions, (dialog, which) -> {
            if (which == 0) {
                createFile("text/csv", "students.csv");  // Export to CSV
            } else if (which == 1) {
                createFile("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "students.xlsx");  // Export to Excel
            }
        });
        builder.create().show();
    }

    private void exportToCSV(Uri uri) {
        try {

            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);

            writer.append("ID,Name,Birthday,ClassName,Faculty,ImageURL,Teacher\n");

            for (Student s : studentList) {
                writer.append(s.getStudentId()).append(",");
                writer.append(s.getBirthday()).append(",");
                writer.append(s.getClassname()).append(",");

                writer.append(s.getFacultyId()).append(",");
                writer.append(s.getImageURL()).append(",");
                writer.append(s.getTeacher()).append("\n");
            }

            writer.flush();
            writer.close();

            Toast.makeText(this, "Xuất dữ liệu thành công", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi xuất dữ liệu CSV", Toast.LENGTH_SHORT).show();
        }
    }


    private void exportToExcel(Uri uri) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Students");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Birthday");
            header.createCell(3).setCellValue("Class");
            header.createCell(4).setCellValue("Faculty");
            header.createCell(5).setCellValue("ImageURL");
            header.createCell(6).setCellValue("Teacher");
            if (studentList == null || studentList.isEmpty()) {
                Log.d("ExportDebug", "studentList is empty or null");
                return;
            } else {
                Log.d("ExportDebug", "studentList size: " + studentList.size());
            }


            int rowIndex = 1;
            for (Student s : studentList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(s.getStudentId());
                row.createCell(1).setCellValue(s.getName());
                row.createCell(2).setCellValue(s.getBirthday());
                row.createCell(3).setCellValue(s.getClassname());
                row.createCell(4).setCellValue(s.getFacultyId());
                row.createCell(5).setCellValue(s.getImageURL());
                row.createCell(6).setCellValue(s.getTeacher());
            }

            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();
            Toast.makeText(this, "Xuất dữ liệu thành công", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi xuất dữ liệu Excel", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");  // Accept both CSV and Excel
        String[] mimeTypes = {"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "text/csv"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Chọn tệp"), 100);
    }

    private void createFile(String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    private void importFromCSV(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            List<Student> newStudent = new ArrayList<>();
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                if (lineNumber == 0) {
                    lineNumber++;
                    continue;
                }


                String[] tokens = line.split(",");


                if (tokens.length == 7) {
                    String studentId = tokens[0].trim();
                    String name = tokens[1].trim();
                    String birthday = tokens[2].trim();
                    String classname = tokens[3].trim();
                    String faculty = tokens[4].trim();
                    String avatarUrl = tokens[5].trim();
                    String teacher = tokens[6].trim();

                    Student s = new Student(studentId, name, birthday, classname, faculty, avatarUrl,teacher);
                    newStudent.add(s);

                    db.collection("Student").document(studentId).set(s)
                            .addOnSuccessListener(aVoid -> {
                                loadStudentData();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Thêm Sinh viên thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
                lineNumber++;
            }

            studentAdapter.updateList(newStudent);
            reader.close();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi đọc tệp CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private void importFromExcel(Uri uri) {
        try {

            InputStream inputStream = getContentResolver().openInputStream(uri);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);

            List<Student> newStudent = new ArrayList<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }

                String studentId = getCellValueAsString(row.getCell(0));
                String name = getCellValueAsString(row.getCell(1));
                String birthday = getCellValueAsString(row.getCell(2));
                String classname = getCellValueAsString(row.getCell(3));
                String faculty = getCellValueAsString(row.getCell(4));
                String avatarUrl = getCellValueAsString(row.getCell(5));
                String teacher = getCellValueAsString(row.getCell(6));

                Student s = new Student(studentId, name, birthday, classname, faculty, avatarUrl,teacher);
                newStudent.add(s);
                db.collection("Student").document(studentId).set(s)
                        .addOnSuccessListener(aVoid -> {
                            loadStudentData();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Thêm Sinh viên thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            studentAdapter.updateList(newStudent);
            workbook.close();
            inputStream.close();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi đọc tệp Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:

                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:

                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "Invalid Value";
        }
    }

    private void loadStudentData() {

        db.collection("Student").get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                List<Student> newList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Student s = document.toObject(Student.class);
                    newList.add(s);
                }

                if (newList.isEmpty()) {
                    Log.d("LoadDebug", "No data available.");
                }
                studentList.clear();
                studentList.addAll(newList);
                studentAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(StudentActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}