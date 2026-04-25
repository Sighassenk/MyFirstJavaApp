package com.example.myapplication.ui.instructor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Course;
import com.example.myapplication.repositories.CourseRepository;
import com.example.myapplication.utils.FirebaseUtils;
import com.example.myapplication.utils.SessionManager;

public class CreateCourseActivity extends AppCompatActivity {

    private EditText    etTitle, etDescription, etPrice, etCategory, etThumbnailUrl;
    private Button      btnCreate;
    private ImageView   ivPreview;
    private ProgressBar progressBar;

    private CourseRepository courseRepository;
    private SessionManager   sessionManager;
    private String           courseId;
    private boolean          isEditMode = false;
    private Course           existingCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_course);

        courseRepository = new CourseRepository();
        sessionManager   = new SessionManager(this);

        etTitle          = findViewById(R.id.etTitle);
        etDescription    = findViewById(R.id.etDescription);
        etPrice          = findViewById(R.id.etPrice);
        etCategory       = findViewById(R.id.etCategory);
        etThumbnailUrl   = findViewById(R.id.etThumbnailUrl);
        btnCreate        = findViewById(R.id.btnCreate);
        ivPreview        = findViewById(R.id.ivPreview);
        progressBar      = findViewById(R.id.progressBar);

        courseId = getIntent().getStringExtra("courseId");
        if (courseId != null) {
            isEditMode = true;
            btnCreate.setText("Update Course");
            loadExistingCourse();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Course" : "Create Course");
        }

        // Preview image URL
        etThumbnailUrl.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                String url = s.toString().trim();
                if (!url.isEmpty()) {
                    Glide.with(CreateCourseActivity.this).load(url).into(ivPreview);
                }
            }
        });

        btnCreate.setOnClickListener(v -> handleAction());
    }

    private void loadExistingCourse() {
        FirebaseUtils.getDb().collection("courses").document(courseId)
                .get()
                .addOnSuccessListener(doc -> {
                    existingCourse = doc.toObject(Course.class);
                    if (existingCourse != null) {
                        etTitle.setText(existingCourse.getTitle());
                        etDescription.setText(existingCourse.getDescription());
                        etPrice.setText(String.valueOf(existingCourse.getPrice()));
                        etCategory.setText(existingCourse.getCategory());
                        etThumbnailUrl.setText(existingCourse.getThumbnailUrl());
                        Glide.with(this).load(existingCourse.getThumbnailUrl()).into(ivPreview);
                    }
                });
    }

    private void handleAction() {
        String title = etTitle.getText().toString().trim();
        String desc  = etDescription.getText().toString().trim();
        String cat   = etCategory.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String thumb = etThumbnailUrl.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || cat.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);

        progressBar.setVisibility(View.VISIBLE);
        btnCreate.setEnabled(false);

        if (isEditMode) {
            existingCourse.setTitle(title);
            existingCourse.setDescription(desc);
            existingCourse.setCategory(cat);
            existingCourse.setPrice(price);
            existingCourse.setThumbnailUrl(thumb);
            
            courseRepository.updateCourse(existingCourse, task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Course updated!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnCreate.setEnabled(true);
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Course course = new Course();
            course.setTitle(title);
            course.setDescription(desc);
            course.setCategory(cat);
            course.setPrice(price);
            course.setThumbnailUrl(thumb);
            course.setInstructorId(sessionManager.getUserId());
            course.setInstructorName(sessionManager.getUserName());
            course.setCreatedAt(System.currentTimeMillis());
            
            courseRepository.createCourse(course, task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Course created!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnCreate.setEnabled(true);
                    Toast.makeText(this, "Creation failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
