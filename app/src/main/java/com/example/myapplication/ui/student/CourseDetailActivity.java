package com.example.myapplication.ui.student;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseDetailActivity extends AppCompatActivity {

    private TextView    tvTitle, tvDescription, tvInstructor, tvPrice, tvRating, tvCategory;
    private ImageView   ivThumbnail;
    private ProgressBar progressBar;
    private com.google.android.material.progressindicator.LinearProgressIndicator progressCourse;
    private RecyclerView rvSections;
    private Button      btnEnroll, btnSubmitReview;
    private RatingBar   ratingBar;
    private View        cardReview;

    private CourseRepository    courseRepository;
    private EnrollmentRepository enrollmentRepository;
    private SessionManager      sessionManager;

    private String   courseId, userId, enrollmentId;
    private boolean  isEnrolled = false;
    private List<String> completedLessons = new ArrayList<>();
    private Course currentCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        
        cardReview      = findViewById(R.id.cardReview);
        ratingBar       = findViewById(R.id.ratingBar);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        rvSections.setLayoutManager(new LinearLayoutManager(this));
        rvSections.setNestedScrollingEnabled(false);

        loadCourseDetails();

        btnEnroll.setOnClickListener(v -> {
            if (isEnrolled) return;
            enrollInCourse();
        });

        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCourseDetails() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUtils.getDb().collection("courses").document(courseId)
                .get()
                .addOnSuccessListener(doc -> {
                    currentCourse = doc.toObject(Course.class);
                    if (currentCourse == null) return;

                    tvTitle.setText(currentCourse.getTitle());
                    tvDescription.setText(currentCourse.getDescription());
                    tvInstructor.setText("By " + currentCourse.getInstructorName());
                    tvCategory.setText(currentCourse.getCategory());
                    tvPrice.setText(currentCourse.getPrice() == 0 ? "Free" : String.format("$%.2f", currentCourse.getPrice()));
                    tvRating.setText(String.format("%.1f ★ (%d reviews)", currentCourse.getRating(), currentCourse.getRatingCount()));

                    if (currentCourse.getThumbnailUrl() != null && !currentCourse.getThumbnailUrl().isEmpty()) {
                        Glide.with(this).load(currentCourse.getThumbnailUrl()).into(ivThumbnail);
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
                completedLessons = enrollment.getCompletedLessons();
                if (completedLessons == null) completedLessons = new ArrayList<>();

                btnEnroll.setText("Enrolled");
                btnEnroll.setEnabled(false);
                cardReview.setVisibility(View.VISIBLE);

                // Show progress bar
                float pct = enrollment.getProgress() * 100;
                progressCourse.setVisibility(View.VISIBLE);
                progressCourse.setProgress((int) pct);
                
                // Set existing rating if any
                if (currentCourse.getUserRatings() != null && currentCourse.getUserRatings().containsKey(userId)) {
                    ratingBar.setRating(currentCourse.getUserRatings().get(userId));
                }
            }
            loadSections();
        });
    }

    private void loadSections() {
        courseRepository.getSections(courseId, sections -> {
            SectionAdapter adapter = new SectionAdapter(
                    sections, enrollmentId, isEnrolled, completedLessons, this);
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

    private void submitReview() {
        float rating = ratingBar.getRating();
        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitReview.setEnabled(false);
        
        Map<String, Object> updates = new HashMap<>();
        Map<String, Float> ratings = currentCourse.getUserRatings();
        if (ratings == null) ratings = new HashMap<>();
        
        ratings.put(userId, rating);
        currentCourse.setUserRatings(ratings);
        currentCourse.updateRating();
        
        updates.put("userRatings", ratings);
        updates.put("rating", currentCourse.getRating());
        updates.put("ratingCount", currentCourse.getRatingCount());

        FirebaseUtils.getDb().collection("courses").document(courseId)
                .update(updates)
                .addOnSuccessListener(v -> {
                    btnSubmitReview.setEnabled(true);
                    tvRating.setText(String.format("%.1f ★ (%d reviews)", currentCourse.getRating(), currentCourse.getRatingCount()));
                    Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnSubmitReview.setEnabled(true);
                    Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                });
    }
}