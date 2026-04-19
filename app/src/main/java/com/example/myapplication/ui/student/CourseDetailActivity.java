package com.example.myapplication.ui.student;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapters.SectionAdapter;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.repositories.CourseRepository;
import com.example.myapplication.repositories.EnrollmentRepository;
import com.example.myapplication.utils.FirebaseUtils;
import com.example.myapplication.utils.SessionManager;

public class CourseDetailActivity extends AppCompatActivity {

    private TextView    tvTitle, tvDescription, tvInstructor, tvPrice, tvRating, tvCategory;
    private ImageView   ivThumbnail;
    private ProgressBar progressCourse, progressBar;
    private RecyclerView rvSections;
    private Button      btnEnroll;

    private CourseRepository    courseRepository;
    private EnrollmentRepository enrollmentRepository;
    private SessionManager      sessionManager;

    private String   courseId, userId, enrollmentId;
    private boolean  isEnrolled = false;
    private int      totalLessons = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Course Details");
        }

        courseId = getIntent().getStringExtra("courseId");
        courseRepository    = new CourseRepository();
        enrollmentRepository = new EnrollmentRepository();
        sessionManager      = new SessionManager(this);
        userId              = sessionManager.getUserId();

        tvTitle       = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvInstructor  = findViewById(R.id.tvInstructor);
        tvPrice       = findViewById(R.id.tvPrice);
        tvRating      = findViewById(R.id.tvRating);
        tvCategory    = findViewById(R.id.tvCategory);
        ivThumbnail   = findViewById(R.id.ivThumbnail);
        rvSections    = findViewById(R.id.rvSections);
        btnEnroll     = findViewById(R.id.btnEnroll);
        progressBar   = findViewById(R.id.progressBar);
        progressCourse = findViewById(R.id.progressCourse);

        rvSections.setLayoutManager(new LinearLayoutManager(this));
        rvSections.setNestedScrollingEnabled(false);

        loadCourseDetails();

        btnEnroll.setOnClickListener(v -> {
            if (isEnrolled) return;
            enrollInCourse();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCourseDetails() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUtils.getDb().collection("courses").document(courseId)
                .get()
                .addOnSuccessListener(doc -> {
                    Course course = doc.toObject(Course.class);
                    if (course == null) return;

                    totalLessons = course.getTotalLessons();
                    tvTitle.setText(course.getTitle());
                    tvDescription.setText(course.getDescription());
                    tvInstructor.setText("By " + course.getInstructorName());
                    tvCategory.setText(course.getCategory());
                    tvPrice.setText(course.getPrice() == 0 ? "Free" : String.format("$%.2f", course.getPrice()));
                    tvRating.setText(String.format("%.1f ★", course.getRating()));

                    if (course.getThumbnailUrl() != null) {
                        Glide.with(this).load(course.getThumbnailUrl()).into(ivThumbnail);
                    }

                    progressBar.setVisibility(View.GONE);
                    checkEnrollmentThenLoadSections();
                });
    }

    private void checkEnrollmentThenLoadSections() {
        enrollmentRepository.getEnrollmentByUserAndCourse(userId, courseId, enrollment -> {
            if (enrollment != null) {
                isEnrolled   = true;
                enrollmentId = enrollment.getId();
                btnEnroll.setText("Already Enrolled");
                btnEnroll.setEnabled(false);

                // Show progress bar
                float pct = enrollment.getProgress() * 100;
                progressCourse.setVisibility(View.VISIBLE);
                progressCourse.setProgress((int) pct);
            }
            loadSections();
        });
    }

    private void loadSections() {
        courseRepository.getSections(courseId, sections -> {
            SectionAdapter adapter = new SectionAdapter(
                    sections, enrollmentId, isEnrolled, totalLessons, this);
            rvSections.setAdapter(adapter);
        });
    }

    private void enrollInCourse() {
        btnEnroll.setEnabled(false);
        enrollmentRepository.enroll(userId, courseId, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Enrolled successfully!", Toast.LENGTH_SHORT).show();
                checkEnrollmentThenLoadSections();
            } else {
                btnEnroll.setEnabled(true);
                Toast.makeText(this, "Enrollment failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}