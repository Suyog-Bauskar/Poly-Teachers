package com.suyogbauskar.attenteachers;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.pojos.StudentData;
import com.suyogbauskar.attenteachers.pojos.Subject;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SubmissionActivity extends AppCompatActivity {

    private FirebaseUser user;
    private TableLayout table;
    private boolean isFirstRow, isFirstYear;
    private TextView noStudentsFoundView;
    private Button createDetentionListBtn;
    private String subjectCodeTeacher, department, selectedDivision;
    private int selectedSemester;
    private ValueEventListener valueEventListener;
    private Map<Integer, StudentData> tempMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);
        setTitle("Submission");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user = FirebaseAuth.getInstance().getCurrentUser();
        tempMap = new TreeMap<>();
        SharedPreferences sharedPreferences2 = getSharedPreferences("teacherDataPref", MODE_PRIVATE);
        department = sharedPreferences2.getString("department", "");
        isFirstYear = false;

        findAllViews();
        createDetentionListBtn.setOnClickListener(v -> createDetentionList());
        selectSemester();
    }

    private void createDetentionList() {
        Toast.makeText(this, "Creating Detention List...", Toast.LENGTH_SHORT).show();
        int rowNo = 0, columnNo = 0;
        String filename;
        if (selectedSemester == 1 || selectedSemester == 2) {
            filename = department + selectedSemester + "-" + selectedDivision + " Detention List";
        } else {
            filename = department + selectedSemester + " Detention List";
        }
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet xssfSheet;
        XSSFRow xssfRow;
        XSSFCell xssfCell;

        try {
            xssfSheet = xssfWorkbook.createSheet("Detention List");
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        xssfRow = xssfSheet.createRow(rowNo);

        xssfCell = xssfRow.createCell(columnNo);
        xssfCell.setCellValue("Roll No");

        columnNo++;

        xssfCell = xssfRow.createCell(columnNo);
        xssfCell.setCellValue("Name");

        columnNo++;

        xssfCell = xssfRow.createCell(columnNo);
        xssfCell.setCellValue("Manual");

        columnNo++;

        xssfCell = xssfRow.createCell(columnNo);
        xssfCell.setCellValue("Micro Project");

        rowNo++;

        if (tempMap.size() > 0) {
            for (Map.Entry<Integer, StudentData> entry1 : tempMap.entrySet()) {
                columnNo = 0;

                if (!entry1.getValue().isManual() || !entry1.getValue().isMicroProject()) {
                    xssfRow = xssfSheet.createRow(rowNo);

                    xssfCell = xssfRow.createCell(columnNo);
                    xssfCell.setCellValue(entry1.getValue().getRollNo() + "");

                    columnNo++;

                    xssfCell = xssfRow.createCell(columnNo);
                    xssfCell.setCellValue(entry1.getValue().getFirstname() + " " + entry1.getValue().getLastname());

                    columnNo++;

                    xssfCell = xssfRow.createCell(columnNo);

                    if (entry1.getValue().isManual()) {
                        xssfCell.setCellValue("Yes");
                    } else {
                        xssfCell.setCellValue("No");
                    }

                    columnNo++;

                    xssfCell = xssfRow.createCell(columnNo);

                    if (entry1.getValue().isMicroProject()) {
                        xssfCell.setCellValue("Yes");
                    } else {
                        xssfCell.setCellValue("No");
                    }

                    rowNo++;
                }
            }

            autoSizeAllColumns(xssfWorkbook);

            try {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers");
                dir.mkdir();
                dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers/Detention List");
                dir.mkdir();

                File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers/Detention List/" + filename + ".xlsx");

                filePath.createNewFile();

                FileOutputStream outputStream = new FileOutputStream(filePath);
                xssfWorkbook.write(outputStream);
                outputStream.flush();
                outputStream.close();
                Toast.makeText(this, "Detention List saved in Downloads folder", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No students found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void autoSizeAllColumns(Workbook workbook) {
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getPhysicalNumberOfRows() > 0) {
                Row row = sheet.getRow(sheet.getFirstRowNum());
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    if (columnIndex == 0) {
                        sheet.setColumnWidth(columnIndex, 2000);
                    } else if (columnIndex == 1) {
                        sheet.setColumnWidth(columnIndex, 5500);
                    } else if (columnIndex == 3) {
                        sheet.setColumnWidth(columnIndex, 3500);
                    }
                }
            }
        }
    }

    private void selectSemester() {
        AlertDialog.Builder semesterDialog = new AlertDialog.Builder(SubmissionActivity.this);
        semesterDialog.setTitle("Semester");
        String[] items = {"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6"};
        semesterDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
            which++;
            selectedSemester = which;
            if (selectedSemester == 1 || selectedSemester == 2) {
                AlertDialog.Builder divisionDialog = new AlertDialog.Builder(SubmissionActivity.this);
                divisionDialog.setTitle("Division");
                String[] items2 = {"Division A", "Division B", "Division C"};
                divisionDialog.setSingleChoiceItems(items2, -1, (dialog2, which2) -> {
                    switch (which2) {
                        case 0:
                            selectedDivision = "A";
                            break;
                        case 1:
                            selectedDivision = "B";
                            break;
                        case 2:
                            selectedDivision = "C";
                            break;
                    }
                    isFirstYear = true;
                    getAllSubjectsOfThatSemester();
                    dialog2.dismiss();
                });
                divisionDialog.create().show();
            } else {
                getAllSubjectsOfThatSemester();
            }
            dialog.dismiss();
        });
        semesterDialog.create().show();
    }

    private void getAllSubjectsOfThatSemester() {
        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Subject> currentSemesterSubjectList = new ArrayList<>();
                        boolean rightSemester = false;

                        for (DataSnapshot dsp : snapshot.getChildren()) {
                            if (selectedSemester == dsp.child("semester").getValue(Integer.class)) {
                                rightSemester = true;
                                currentSemesterSubjectList.add(new Subject(dsp.getKey(), dsp.child("subject_short_name").getValue(String.class)));
                            }
                        }

                        if (!rightSemester) {
                            Toast.makeText(SubmissionActivity.this, "You don't teach this semester", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (currentSemesterSubjectList.size() > 1) {
                            AlertDialog.Builder subjectDialog = new AlertDialog.Builder(SubmissionActivity.this);
                            subjectDialog.setTitle("Subjects");
                            String[] items3 = new String[currentSemesterSubjectList.size()];
                            for (int i = 0; i < currentSemesterSubjectList.size(); i++) {
                                items3[i] = currentSemesterSubjectList.get(i).getShortName();
                            }

                            subjectDialog.setSingleChoiceItems(items3, -1, (dialog3, which3) -> {
                                dialog3.dismiss();
                                subjectCodeTeacher = currentSemesterSubjectList.get(which3).getCode();
                                whichDataToShow();
                            });
                            subjectDialog.create().show();
                        } else {
                            subjectCodeTeacher = currentSemesterSubjectList.get(0).getCode();
                            whichDataToShow();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SubmissionActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void whichDataToShow() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showStudentsData(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        };

        if (isFirstYear) {
            FirebaseDatabase.getInstance().getReference("students_data")
                    .orderByChild("queryStringDivision")
                    .equalTo(department + selectedSemester + selectedDivision)
                    .addValueEventListener(valueEventListener);
        } else {
            FirebaseDatabase.getInstance().getReference("students_data")
                    .orderByChild("queryStringSemester")
                    .equalTo(department + selectedSemester)
                    .addValueEventListener(valueEventListener);
        }
    }

    private void showStudentsData(DataSnapshot snapshot) {
        isFirstRow = true;
        table.removeAllViews();
        if (snapshot.getChildrenCount() == 0) {
            noStudentsFoundView.setVisibility(View.VISIBLE);
            createDetentionListBtn.setVisibility(View.GONE);
            return;
        }
        drawTableHeader();
        createDetentionListBtn.setVisibility(View.VISIBLE);
        noStudentsFoundView.setVisibility(View.GONE);
        try {
            for (DataSnapshot ds : snapshot.getChildren()) {
                if (ds.child("isVerified").getValue(Boolean.class)) {
                    tempMap.put(ds.child("rollNo").getValue(Integer.class), new StudentData(
                            ds.child("rollNo").getValue(Integer.class),
                            ds.child("firstname").getValue(String.class),
                            ds.child("lastname").getValue(String.class),
                            ds.child("enrollNo").getValue(Long.class),
                            ds.child("subjects").child(subjectCodeTeacher).child("isManualSubmitted").getValue(Boolean.class),
                            ds.child("subjects").child(subjectCodeTeacher).child("isMicroProjectSubmitted").getValue(Boolean.class)
                    ));
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        for (Map.Entry<Integer, StudentData> entry1 : tempMap.entrySet()) {
            if (entry1.getValue().isManual() && entry1.getValue().isMicroProject()) {
                createTableRow(entry1.getValue().getRollNo(), entry1.getValue().getFirstname() + " " + entry1.getValue().getLastname(), entry1.getValue().getEnrollNo(), "✅", "✅");
            } else if (entry1.getValue().isManual()) {
                createTableRow(entry1.getValue().getRollNo(), entry1.getValue().getFirstname() + " " + entry1.getValue().getLastname(), entry1.getValue().getEnrollNo(), "✅", "❌");
            } else if (entry1.getValue().isMicroProject()) {
                createTableRow(entry1.getValue().getRollNo(), entry1.getValue().getFirstname() + " " + entry1.getValue().getLastname(), entry1.getValue().getEnrollNo(), "❌", "✅");
            } else {
                createTableRow(entry1.getValue().getRollNo(), entry1.getValue().getFirstname() + " " + entry1.getValue().getLastname(), entry1.getValue().getEnrollNo(), "❌", "❌");
            }
        }
    }

    private void findAllViews() {
        table = findViewById(R.id.table);
        noStudentsFoundView = findViewById(R.id.noStudentsFoundView);
        createDetentionListBtn = findViewById(R.id.createDetentionListBtn);
    }

    private void drawTableHeader() {
        TableRow tbRow = new TableRow(SubmissionActivity.this);

        TextView tv0 = new TextView(SubmissionActivity.this);
        TextView tv1 = new TextView(SubmissionActivity.this);
        TextView tv2 = new TextView(SubmissionActivity.this);
        TextView tv3 = new TextView(SubmissionActivity.this);
        TextView tv4 = new TextView(SubmissionActivity.this);

        tv0.setText("Roll No.");
        tv1.setText("Name");
        tv2.setText("Enroll No.");
        tv3.setText("Manual");
        tv4.setText("Micro Project");

        tv0.setTypeface(Typeface.DEFAULT_BOLD);
        tv1.setTypeface(Typeface.DEFAULT_BOLD);
        tv2.setTypeface(Typeface.DEFAULT_BOLD);
        tv3.setTypeface(Typeface.DEFAULT_BOLD);
        tv4.setTypeface(Typeface.DEFAULT_BOLD);

        tv0.setTextSize(18);
        tv1.setTextSize(18);
        tv2.setTextSize(18);
        tv3.setTextSize(18);
        tv4.setTextSize(18);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);
        tv3.setPadding(30, 30, 15, 30);
        tv4.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);
        tv3.setGravity(Gravity.CENTER);
        tv4.setGravity(Gravity.CENTER);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);
        tv3.setTextColor(Color.BLACK);
        tv4.setTextColor(Color.BLACK);

        tv0.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv1.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv2.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv3.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv4.setBackgroundColor(getResources().getColor(R.color.table_header));

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);
        tbRow.addView(tv3);
        tbRow.addView(tv4);

        table.addView(tbRow);
    }

    private void createTableRow(int rollNo, String name, long enrollNo, String manual, String microProject) {
        TableRow tbRow = new TableRow(SubmissionActivity.this);

        tbRow.setTag(enrollNo);

        TextView tv0 = new TextView(SubmissionActivity.this);
        TextView tv1 = new TextView(SubmissionActivity.this);
        TextView tv2 = new TextView(SubmissionActivity.this);
        TextView tv3 = new TextView(SubmissionActivity.this);
        TextView tv4 = new TextView(SubmissionActivity.this);

        tv0.setText(String.valueOf(rollNo));
        tv1.setText(name);
        tv2.setText(String.valueOf(enrollNo));
        tv3.setText(manual);
        tv4.setText(microProject);

        tv0.setTextSize(16);
        tv1.setTextSize(16);
        tv2.setTextSize(16);
        tv3.setTextSize(16);
        tv4.setTextSize(16);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);
        tv3.setPadding(30, 30, 15, 30);
        tv4.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);
        tv3.setGravity(Gravity.CENTER);
        tv4.setGravity(Gravity.CENTER);

        tv0.setBackgroundResource(R.drawable.borders);
        tv1.setBackgroundResource(R.drawable.borders);
        tv2.setBackgroundResource(R.drawable.borders);
        tv3.setBackgroundResource(R.drawable.borders);
        tv4.setBackgroundResource(R.drawable.borders);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);
        tv3.setTextColor(Color.BLACK);
        tv4.setTextColor(Color.BLACK);

        if (isFirstRow) {
            tv0.setBackgroundColor(getResources().getColor(R.color.white));
            tv1.setBackgroundColor(getResources().getColor(R.color.white));
            tv2.setBackgroundColor(getResources().getColor(R.color.white));
            tv3.setBackgroundColor(getResources().getColor(R.color.white));
            tv4.setBackgroundColor(getResources().getColor(R.color.white));
            isFirstRow = false;
        } else {
            tv0.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv1.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv2.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv3.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv4.setBackgroundColor(getResources().getColor(R.color.light_gray));
            isFirstRow = true;
        }

        tbRow.setOnClickListener(view -> {
            long enrollNoFromTag = Long.parseLong(tbRow.getTag().toString());

            FirebaseDatabase.getInstance().getReference("students_data")
                    .orderByChild("enrollNo")
                    .equalTo(enrollNoFromTag)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean isManualSubmitted = false, isMicroProjectSubmitted = false;

                            for (DataSnapshot ds : snapshot.getChildren()) {
                                isManualSubmitted = ds.child("subjects").child(subjectCodeTeacher).child("isManualSubmitted").getValue(Boolean.class);
                                isMicroProjectSubmitted = ds.child("subjects").child(subjectCodeTeacher).child("isMicroProjectSubmitted").getValue(Boolean.class);
                            }

                            final String[] listItems = new String[]{"Manual", "Micro Project"};
                            final boolean[] checkedItems = new boolean[listItems.length];

                            if (isManualSubmitted && isMicroProjectSubmitted) {
                                checkedItems[0] = true;
                                checkedItems[1] = true;
                            } else if (isManualSubmitted) {
                                checkedItems[0] = true;
                            } else if (isMicroProjectSubmitted) {
                                checkedItems[1] = true;
                            } else {
                                checkedItems[0] = false;
                                checkedItems[1] = false;
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(SubmissionActivity.this);
                            builder.setTitle("Choose Items");

                            builder.setMultiChoiceItems(listItems, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked);

                            builder.setPositiveButton("Done", (dialog, which) -> {
                                if (checkedItems[0]) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        ds.child("subjects").child(subjectCodeTeacher).child("isManualSubmitted").getRef().setValue(true);
                                    }
                                } else {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        ds.child("subjects").child(subjectCodeTeacher).child("isManualSubmitted").getRef().setValue(false);
                                    }
                                }
                                if (checkedItems[1]) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        ds.child("subjects").child(subjectCodeTeacher).child("isMicroProjectSubmitted").getRef().setValue(true);
                                    }
                                } else {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        ds.child("subjects").child(subjectCodeTeacher).child("isMicroProjectSubmitted").getRef().setValue(false);
                                    }
                                }
                            });

                            builder.setNegativeButton("CANCEL", (dialog, which) -> {
                            });
                            builder.create();
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(SubmissionActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);
        tbRow.addView(tv3);
        tbRow.addView(tv4);

        table.addView(tbRow);
    }

    @Override
    protected void onDestroy() {
        if (valueEventListener != null) {
            FirebaseDatabase.getInstance().getReference("students_data").removeEventListener(valueEventListener);
        }
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SubmissionActivity.this, HomeActivity.class));
    }
}