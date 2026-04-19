package com.example.myapplication.ui.instructor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.example.myapplication.R;
import com.example.myapplication.adapters.CourseAdapter;
import com.example.myapplication.models.Course;
import com.example.myapplication.repositories.AuthRepository;
import com.example.myapplication.repositories.CourseRepository;
import com.example.myapplication.ui.auth.LoginActivity;
import com.example.myapplication.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class InstructorDashboardActivity extends AppCompatActivity {

    private RecyclerView                 rvCourses;
    private ExtendedFloatingActionButton fabCreate;
    private ProgressBar                  progressBar;
    private TextView                     tvWelcome;
    private View                         emptyState;
    private CourseAdapter                courseAdapter;
    private final List<Course>           courseList = new ArrayList<>();

    private CourseRepository courseRepository;
    private SessionManager   sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_dashboard);

        courseRepository = new CourseRepository();
        sessionManager   = new SessionManager(this);

        rvCourses   = findViewById(R.id.rvCourses);
        fabCreate   = findViewById(R.id.fabCreate);
        progressBar = findViewById(R.id.progressBar);
        emptyState  = findViewById(R.id.emptyState);
        tvWelcome   = findViewById(R.id.tvWelcome);

        tvWelcome.setText("Welcome, " + sessionManager.getUserName());

        courseAdapter = new CourseAdapter(courseList, course -> {
            Intent intent = new Intent(this, ManageCourseActivity.class);
            intent.putExtra("courseId", course.getId());
            startActivity(intent);
        });

        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        rvCourses.setAdapter(courseAdapter);

        fabCreate.setOnClickListener(v ->
                startActivity(new Intent(this, CreateCourseActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourses();
    }

    private void loadCourses() {
        progressBar.setVisibility(View.VISIBLE);
        courseRepository.getInstructorCourses(sessionManager.getUserId(), courses -> {
            progressBar.setVisibility(View.GONE);
            courseList.clear();
            courseList.addAll(courses);
            courseAdapter.updateList(courseList);
            
            if (emptyState != null) {
                emptyState.setVisibility(courses.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void logout() {
        new AuthRepository().logout();
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
