package com.example.myapplication.ui.student;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.CourseAdapter;
import com.example.myapplication.models.Course;
import com.example.myapplication.repositories.AuthRepository;
import com.example.myapplication.repositories.CourseRepository;
import com.example.myapplication.ui.auth.LoginActivity;
import com.example.myapplication.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView  rvCourses;
    private EditText      etSearch;
    private ProgressBar   progressBar;
    private TextView      tvEmpty;
    private CourseAdapter courseAdapter;
    private final List<Course> courseList = new ArrayList<>();

    private CourseRepository courseRepository;
    private SessionManager   sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        courseRepository = new CourseRepository();
        sessionManager   = new SessionManager(this);

        rvCourses   = findViewById(R.id.rvCourses);
        etSearch    = findViewById(R.id.etSearch);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty     = findViewById(R.id.tvEmpty);

        findViewById(R.id.btnMyCourses).setOnClickListener(v ->
                startActivity(new Intent(this, MyCoursesActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        courseAdapter = new CourseAdapter(courseList, course -> {
            Intent intent = new Intent(this, CourseDetailActivity.class);
            intent.putExtra("courseId", course.getId());
            startActivity(intent);
        });

        // Changed from GridLayoutManager to LinearLayoutManager to show courses under each other
        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        rvCourses.setAdapter(courseAdapter);

        setupSearch();
        loadCourses();
    }

    private void loadCourses() {
        progressBar.setVisibility(View.VISIBLE);
        courseRepository.getAllCourses(courses -> {
            progressBar.setVisibility(View.GONE);
            courseList.clear();
            courseList.addAll(courses);
            courseAdapter.updateList(courseList);
            tvEmpty.setVisibility(courses.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                courseAdapter.filter(s.toString());
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
