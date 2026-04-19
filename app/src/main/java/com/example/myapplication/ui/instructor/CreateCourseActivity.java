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
import com.example.myapplication.utils.SessionManager;

public class CreateCourseActivity extends AppCompatActivity {

    private EditText    etTitle, etDescription, etPrice, etCategory, etThumbnailUrl;
    private Button      btnCreate;
    private ImageView   ivPreview;
    private ProgressBar progressBar;

    private CourseRepository courseRepository;
    private SessionManager   sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_course);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Course");
        }

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

        // Preview the image as the user types the URL
        etThumbnailUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String url = s.toString().trim();
                if (!url.isEmpty()) {
                    Glide.with(CreateCourseActivity.this)
                         .load(url)
                         .placeholder(android.R.drawable.ic_menu_gallery)
                         .error(android.R.drawable.stat_notify_error)
                         .into(ivPreview);
                }
            }
        });

        btnCreate.setOnClickListener(v -> createCourse());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createCourse() {
        String title       = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr    = etPrice.getText().toString().trim();
        String category    = etCategory.getText().toString().trim();
        String thumbnailUrl = etThumbnailUrl.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Title, description and category are required", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);

        progressBar.setVisibility(View.VISIBLE);
        btnCreate.setEnabled(false);

        saveCourse(title, description, price, category, thumbnailUrl);
    }

    private void saveCourse(String title, String desc, double price,
                            String category, String thumbUrl) {
        Course course = new Course();
        course.setTitle(title);
        course.setDescription(desc);
        course.setPrice(price);
        course.setCategory(category);
        course.setThumbnailUrl(thumbUrl);
        course.setInstructorId(sessionManager.getUserId());
        course.setInstructorName(sessionManager.getUserName());
        course.setRating(0f);
        course.setTotalLessons(0);
        course.setCreatedAt(System.currentTimeMillis());

        courseRepository.createCourse(course, task -> {
            progressBar.setVisibility(View.GONE);
            btnCreate.setEnabled(true);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Course created!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
