package com.example.myapplication.ui.student;


import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.CourseAdapter;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.repositories.CourseRepository;
import com.example.myapplication.repositories.EnrollmentRepository;
import com.example.myapplication.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class MyCoursesActivity extends AppCompatActivity {

    private RecyclerView  rvCourses;
    private ProgressBar   progressBar;
    private View          emptyState;
    private CourseAdapter courseAdapter;
    private final List<Course> courseList = new ArrayList<>();

    private EnrollmentRepository enrollmentRepository;
    private CourseRepository     courseRepository;
    private SessionManager       sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_courses);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        enrollmentRepository = new EnrollmentRepository();
        courseRepository     = new CourseRepository();
        sessionManager       = new SessionManager(this);

        rvCourses   = findViewById(R.id.rvCourses);
        progressBar = findViewById(R.id.progressBar);
        emptyState  = findViewById(R.id.emptyState);

        findViewById(R.id.btnBrowse).setOnClickListener(v -> finish());

        courseAdapter = new CourseAdapter(courseList, course -> {
            Intent intent = new Intent(this, CourseDetailActivity.class);
            intent.putExtra("courseId", course.getId());
            startActivity(intent);
        });

        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        rvCourses.setAdapter(courseAdapter);

        loadMyCourses();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadMyCourses() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = sessionManager.getUserId();

        enrollmentRepository.getEnrollments(userId, enrollments -> {
            if (enrollments.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
                return;
            }

            List<String> ids = new ArrayList<>();
            for (Enrollment e : enrollments) ids.add(e.getCourseId());

            courseRepository.getCoursesByIds(ids, courses -> {
                progressBar.setVisibility(View.GONE);
                courseList.clear();
                courseList.addAll(courses);
                courseAdapter.updateList(courseList);
                emptyState.setVisibility(courses.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }
}
