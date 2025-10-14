package ong.myapp.studentmanagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
import java.util.List;

import ong.myapp.studentmanagement.adapter.TinChiAdapter;
import ong.myapp.studentmanagement.model.Certificate;

public class QuanLyTinChiActivity extends AppCompatActivity {
    private static final int CREATE_FILE_REQUEST_CODE = 1;
    private static final int PICK_FILE_REQUEST_CODE = 100;
    ImageView btnBack, btnSearch;
    EditText editSearch;
    RecyclerView recyclerViewTinChi;
    TinChiAdapter tinChiAdapter;
    List<Certificate> tinChiList;
    FirebaseFirestore db;
    ProgressBar progressBar;
    Certificate selectedCertificate;
     String userRole=" ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_tin_chi);
        userRole = getIntent().getStringExtra("role");
        anhXa();
        db = FirebaseFirestore.getInstance();
        tinChiList = new ArrayList<>();
        tinChiAdapter = new TinChiAdapter(this, tinChiList);

        recyclerViewTinChi.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTinChi.setAdapter(tinChiAdapter);

        loadTinChiData();

        tinChiAdapter.setOnItemClickListener(certificate -> {
            selectedCertificate = certificate;
        });

        btnBack.setOnClickListener(view -> finish());
        btnSearch.setOnClickListener(view -> {
            String searchText = editSearch.getText().toString().trim();
            if (!searchText.isEmpty()) {
                searchCertificates(searchText);
            } else {
                loadTinChiData();
            }
        });
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().trim();
                if (!searchText.isEmpty()) {
                    searchCertificates(searchText);
                } else {
                    loadTinChiData();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void searchCertificates(String searchText) {
        String query = searchText.toLowerCase();
        List<Certificate> filteredList = new ArrayList<>();

        for (Certificate certificate : tinChiList) {
            if (certificate.getCerId().toLowerCase().contains(query) ||
                    certificate.getStudentId().toLowerCase().contains(query) ||
                    certificate.getName().toLowerCase().contains(query) ||
                    certificate.getDescription().toLowerCase().contains(query) ||
                    certificate.getFaculty().toLowerCase().contains(query)) {
                filteredList.add(certificate);
            }
        }

        tinChiAdapter.updateData(filteredList);

        TextInputLayout layoutSearch = findViewById(R.id.layoutSearch);
        if (filteredList.isEmpty()) {

            layoutSearch.setError("Không tìm thấy chứng chỉ");
            loadTinChiData();
        } else {
            layoutSearch.setError(null);
        }
    }



    private void anhXa() {
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnSearch = findViewById(R.id.btnSearch);
        editSearch = findViewById(R.id.editSearch);
        recyclerViewTinChi = findViewById(R.id.recyclerViewTinChi);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_tinchi, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (userRole.equalsIgnoreCase("employee")) {
            return true;
        }
        if (itemId == R.id.menu_add_new) {
            showAddTinChiDialog();
            return true;
        } else if (itemId == R.id.menu_update) {
            if (selectedCertificate != null) {
                showUpdateTinChiDialog(selectedCertificate);
            } else {
                Toast.makeText(this, "Vui lòng chọn chứng chỉ cần cập nhật", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (itemId == R.id.menu_delete) {
            showDeleteConfirmationDialog();
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

    private void loadTinChiData() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Certificate").get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                List<Certificate> newList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Certificate tinChi = document.toObject(Certificate.class);
                    newList.add(tinChi);
                }

                if (newList.isEmpty()) {
                    Log.d("LoadDebug", "No data available.");
                }
                tinChiList.clear();
                tinChiList.addAll(newList);
                tinChiAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            } else {
                Toast.makeText(QuanLyTinChiActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showAddTinChiDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm chứng chỉ mới");

        // Set up the view holder for the dialog
        DialogViewHolderTinChi viewHolder = DialogViewHolderTinChi.setupDialogView(this, null, false);
        builder.setView(viewHolder.edtCerId.getRootView());

        // Generate a random certificate ID in the format C### (e.g., C302)
        String generatedCerId = "TC" + (100 + (int)(Math.random() * 900)); // Generates a random number from 100 to 999
        viewHolder.edtCerId.setText(generatedCerId); // Set the generated ID to the EditText

        // Make the certificate ID EditText non-editable and non-focusable
        viewHolder.edtCerId.setFocusable(false);
        viewHolder.edtCerId.setClickable(false);

        builder.setPositiveButton("Thêm", null);
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        addButton.setOnClickListener(v -> {
            viewHolder.layoutCerId.setError(null);
            viewHolder.layoutStudentId.setError(null);
            viewHolder.layoutName.setError(null);
            viewHolder.layoutFaculty.setError(null);
            viewHolder.layoutStatus.setError(null);
            viewHolder.layoutDescription.setError(null);
            if (validateInputs(viewHolder.edtCerId, viewHolder.edtStudentId, viewHolder.edtName, viewHolder.edtFaculty, viewHolder.edtStatus, viewHolder.edtDescription,
                    viewHolder.layoutCerId, viewHolder.layoutStudentId, viewHolder.layoutName, viewHolder.layoutFaculty, viewHolder.layoutStatus, viewHolder.layoutDescription)) {
                String cerId = viewHolder.edtCerId.getText().toString().trim();
                String studentId = viewHolder.edtStudentId.getText().toString().trim();
                String name = viewHolder.edtName.getText().toString().trim();
                String faculty = viewHolder.edtFaculty.getText().toString().trim();
                String status = viewHolder.edtStatus.getText().toString().trim();
                String description = viewHolder.edtDescription.getText().toString().trim();

                checkCertificateExists(cerId, exists -> {
                    if (exists) {
                        viewHolder.layoutCerId.setError("Mã chứng chỉ đã tồn tại");
                        viewHolder.edtCerId.requestFocus();
                    } else {
                        checkStudentExists(studentId, studentExists -> {
                            if (!studentExists) {
                                viewHolder.layoutStudentId.setError("Mã sinh viên không tồn tại");
                                viewHolder.edtStudentId.requestFocus();
                            } else {
                                Certificate certificate = new Certificate(cerId, studentId, name, faculty, status, description);
                                tinChiList.add(certificate);
                                tinChiAdapter.notifyDataSetChanged();
                                certificate.saveToFirebase();
                                Toast.makeText(this, "Thêm chứng chỉ thành công", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                });
            }
        });
    }



    private void showUpdateTinChiDialog(Certificate certificate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật thông tin tín chỉ");
        DialogViewHolderTinChi viewHolder = DialogViewHolderTinChi.setupDialogView(this, certificate, true);
        builder.setView(viewHolder.edtCerId.getRootView());

        builder.setPositiveButton("Cập nhật", null);
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        Button updateButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        updateButton.setOnClickListener(v -> {
            viewHolder.layoutCerId.setError(null);
            viewHolder.layoutStudentId.setError(null);
            viewHolder.layoutName.setError(null);
            viewHolder.layoutFaculty.setError(null);
            viewHolder.layoutStatus.setError(null);
            viewHolder.layoutDescription.setError(null);
            if (validateInputs(viewHolder.edtCerId, viewHolder.edtStudentId, viewHolder.edtName, viewHolder.edtFaculty, viewHolder.edtStatus,viewHolder.edtDescription,
                    viewHolder.layoutCerId, viewHolder.layoutStudentId, viewHolder.layoutName, viewHolder.layoutFaculty, viewHolder.layoutStatus, viewHolder.layoutDescription)) {
                String cerId = viewHolder.edtCerId.getText().toString().trim();
                String studentId = viewHolder.edtStudentId.getText().toString().trim();
                String name = viewHolder.edtName.getText().toString().trim();
                String faculty = viewHolder.edtFaculty.getText().toString().trim();
                String status = viewHolder.edtStatus.getText().toString().trim();
                String description = viewHolder.edtDescription.getText().toString().trim();

                certificate.setCerId(cerId);
                certificate.setStudentId(studentId);
                certificate.setName(name);
                certificate.setFaculty(faculty);
                certificate.setStatus(status);
                certificate.setDescription(description);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Certificate").document(cerId).set(certificate)
                        .addOnSuccessListener(aVoid -> {
                            for (int i = 0; i < tinChiList.size(); i++) {
                                if (tinChiList.get(i).getCerId().equals(cerId)) {
                                    tinChiList.set(i, certificate);
                                    break;
                                }
                            }
                            tinChiAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            }
        });
    }


    private void showDeleteConfirmationDialog(Certificate certificate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa chứng chỉ này?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            deleteCertificate(certificate);
            tinChiList.remove(certificate);
            tinChiAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void deleteCertificate(Certificate certificate) {
        db.collection("Certificate").document(certificate.getCerId())
                .delete()
                .addOnSuccessListener(aVoid -> {

                    loadTinChiData();
                    Toast.makeText(this, "Xóa chứng chỉ thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Xóa chứng chỉ thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void showDeleteConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa tất cả dữ liệu")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ dữ liệu không?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAllStudents())
                .setNegativeButton("No", null)
                .show();
    }
    private void deleteAllStudents() {
        // Lấy toàn bộ tài liệu trong bộ sưu tập "Student"
        db.collection("Certificate")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection("Certificate").document(document.getId()).delete()
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Xóa thành công: " + document.getId()))
                                .addOnFailureListener(e -> Log.e("Firestore", "Lỗi khi xóa", e));
                    }

                    tinChiList.clear();
                    tinChiAdapter.updateData(tinChiList);
                    //  studentAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Xóa tất cả dữ liệu thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi lấy dữ liệu", e);
                    Toast.makeText(this, "Lỗi khi xóa dữ liệu!", Toast.LENGTH_SHORT).show();
                });
    }
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");  // Accept both CSV and Excel
        String[] mimeTypes = {"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "text/csv"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Chọn tệp"), 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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


    private void importFromCSV(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            List<Certificate> newCertificates = new ArrayList<>();
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                if (lineNumber == 0) {
                    lineNumber++;
                    continue;
                }


                String[] tokens = line.split(",");


                if (tokens.length == 7) {
                    String cerId = tokens[0].trim();
                    String studentId = tokens[1].trim();
                    String name = tokens[2].trim();
                    String faculty = tokens[4].trim();
                    String status = tokens[5].trim();
                    String description = tokens[6].trim();


                    Certificate certificate = new Certificate(cerId, studentId, name, faculty, status, description);
                    newCertificates.add(certificate);

                    // Save certificate to Firestore
                    db.collection("Certificate").document(cerId).set(certificate)
                            .addOnSuccessListener(aVoid -> {
                                loadTinChiData();
                                Toast.makeText(this, "Chứng chỉ được thêm thành công", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Thêm chứng chỉ thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
                lineNumber++;
            }

            // Update the RecyclerView with new data
            tinChiAdapter.updateData(newCertificates);
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

            List<Certificate> newCertificates = new ArrayList<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    // Skip header row
                    continue;
                }


                String cerId = getCellValueAsString(row.getCell(0));
                String studentId = getCellValueAsString(row.getCell(1));
                String name = getCellValueAsString(row.getCell(2));
                String numberCredits = getCellValueAsString(row.getCell(3));
                String faculty = getCellValueAsString(row.getCell(4));
                String status = getCellValueAsString(row.getCell(5));
                String description = getCellValueAsString(row.getCell(6));

                Certificate certificate = new Certificate(cerId, studentId, name, faculty, status, description);
                newCertificates.add(certificate);
                db.collection("Certificate").document(cerId).set(certificate)
                        .addOnSuccessListener(aVoid -> {
                            loadTinChiData();
                            Toast.makeText(this, "Chứng chỉ được thêm thành công", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Thêm chứng chỉ thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            tinChiAdapter.updateData(newCertificates);
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
                // Handle formula type cells
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "Invalid Value";
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

    private void exportToCSV(Uri uri) {
        try {
            // Use the content resolver to open the output stream for the selected file
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);

            // Write the CSV header
            writer.append("CerId,StudentId,Name,NumberCredits,Faculty,Status,Description\n");

            // Write data rows
            for (Certificate certificate : tinChiList) {
                writer.append(certificate.getCerId()).append(",");
                writer.append(certificate.getStudentId()).append(",");
                writer.append(certificate.getName()).append(",");

                writer.append(certificate.getFaculty()).append(",");
                writer.append(certificate.getStatus()).append(",");
                writer.append(certificate.getDescription()).append("\n");
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
            XSSFSheet sheet = workbook.createSheet("Certificates");

            // Create header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("CerId");
            header.createCell(1).setCellValue("StudentId");
            header.createCell(2).setCellValue("Name");
            header.createCell(3).setCellValue("NumberCredits");
            header.createCell(4).setCellValue("Faculty");
            header.createCell(5).setCellValue("Status");
            header.createCell(6).setCellValue("Description");
            if (tinChiList == null || tinChiList.isEmpty()) {
                Log.d("ExportDebug", "tinChiList is empty or null");
                return;
            } else {
                Log.d("ExportDebug", "tinChiList size: " + tinChiList.size());
            }

            // Write data rows
            int rowIndex = 1;
            for (Certificate certificate : tinChiList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(certificate.getCerId());
                row.createCell(1).setCellValue(certificate.getStudentId());
                row.createCell(2).setCellValue(certificate.getName());

                row.createCell(4).setCellValue(certificate.getFaculty());
                row.createCell(5).setCellValue(certificate.getStatus());
                row.createCell(6).setCellValue(certificate.getDescription());
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


    private void showExportOptions() {
        String[] exportOptions = {"Xuất sang CSV", "Xuất sang Excel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn định dạng xuất dữ liệu");
        builder.setItems(exportOptions, (dialog, which) -> {
            if (which == 0) {
                createFile("text/csv", "certificates.csv");  // Export to CSV
            } else if (which == 1) {
                createFile("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "certificates.xlsx");  // Export to Excel
            }
        });
        builder.create().show();
    }

    private void createFile(String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    private boolean validateInputs(EditText edtCerId, EditText edtStudentId, EditText edtName, EditText edtFaculty, EditText edtStatus,EditText edtDescription, TextInputLayout layoutCerId, TextInputLayout layoutStudentId, TextInputLayout layoutName, TextInputLayout layoutFaculty, TextInputLayout layoutStatus, TextInputLayout layoutDescription) {
        if (edtCerId.getText().toString().trim().isEmpty()) {
            layoutCerId.setError("Mã chứng chỉ không được để trống");
            edtCerId.requestFocus();
            return false;
        }
        if (edtStudentId.getText().toString().trim().isEmpty()) {
            layoutStudentId.setError("Mã sinh viên không được để trống");
            edtStudentId.requestFocus();
            return false;
        }
        if (edtName.getText().toString().trim().isEmpty()) {
            layoutName.setError("Tên tín ch không được để trống");
            edtName.requestFocus();
            return false;
        }
        if (edtFaculty.getText().toString().trim().isEmpty()) {
            layoutFaculty.setError("Mã khoa không được để trống");
            edtFaculty.requestFocus();
            return false;
        }
        String status = edtStatus.getText().toString().trim().toLowerCase();
        if (status.isEmpty()) {
            layoutStatus.setError("Trạng thái không được để trống");
            edtStatus.requestFocus();
            return false;
        }

        if (edtDescription.getText().toString().trim().isEmpty()) {
            layoutDescription.setError("Mô tả chi tiết không được để trống");
            edtDescription.requestFocus();
            return false;
        }
        return true;
    }

    private void checkCertificateExists(String cerId, OnCheckExistsListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Certificate").document(cerId).get().addOnSuccessListener(documentSnapshot -> {
            listener.onCheckExists(documentSnapshot.exists());
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi kiểm tra mã chứng chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void checkStudentExists(String studentId, OnCheckExistsListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Student").document(studentId).get().addOnSuccessListener(documentSnapshot -> {
            listener.onCheckExists(documentSnapshot.exists());
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi kiểm tra mã sinh viên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    interface OnCheckExistsListener {
        void onCheckExists(boolean exists);
    }


}
