package com.example.myapplication.ui.instructor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.SectionAdapter;
import com.example.myapplication.models.Lesson;
import com.example.myapplication.models.Section;
import com.example.myapplication.repositories.CourseRepository;
import com.example.myapplication.repositories.LessonRepository;
import com.example.myapplication.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class ManageCourseActivity extends AppCompatActivity {

    private TextView     tvCourseTitle;
    private RecyclerView rvSections;
    private Button       btnAddSection;
    private ProgressBar  progressBar;

    private CourseRepository courseRepository;
    private LessonRepository lessonRepository;

    private String courseId;
    private List<Section> sectionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_course);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        courseId         = getIntent().getStringExtra("courseId");
        courseRepository = new CourseRepository();
        lessonRepository = new LessonRepository();

        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        rvSections    = findViewById(R.id.rvSections);
        btnAddSection = findViewById(R.id.btnAddSection);
        progressBar   = findViewById(R.id.progressBar);

        rvSections.setLayoutManager(new LinearLayoutManager(this));

        btnAddSection.setOnClickListener(v -> showAddSectionDialog());
        loadCourseTitle();
        loadSections();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manage_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_edit_course) {
            Intent intent = new Intent(this, CreateCourseActivity.class);
            intent.putExtra("courseId", courseId);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_delete_course) {
            showDeleteCourseDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCourseTitle() {
        FirebaseUtils.getDb().collection("courses").document(courseId)
                .get()
                .addOnSuccessListener(doc -> {
                    String title = doc.getString("title");
                    if (title != null) tvCourseTitle.setText(title);
                });
    }

    private void loadSections() {
        progressBar.setVisibility(View.VISIBLE);
        courseRepository.getSections(courseId, sections -> {
            progressBar.setVisibility(View.GONE);
            sectionList.clear();
            sectionList.addAll(sections);
            SectionAdapter adapter = new SectionAdapter(
                    sectionList, null, false, null, this);
            rvSections.setAdapter(adapter);
        });
    }

    private void showAddSectionDialog() {
        EditText input = new EditText(this);
        input.setHint("Section title");
        input.setPadding(48, 24, 48, 24);

        new AlertDialog.Builder(this)
                .setTitle("Add Section")
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {
                    String title = input.getText().toString().trim();
                    if (!title.isEmpty()) addSection(title);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addSection(String title) {
        String id = FirebaseUtils.getDb().collection("sections").document().getId();
        Section section = new Section();
        section.setId(id);
        section.setTitle(title);
        section.setCourseId(courseId);
        section.setOrder(sectionList.size());

        FirebaseUtils.getDb().collection("sections").document(id)
                .set(section)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Section added", Toast.LENGTH_SHORT).show();
                    loadSections();
                });
    }
    
    private void showDeleteCourseDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Course?")
                .setMessage("This will permanently remove the course, sections, lessons, and enrollments.")
                .setPositiveButton("Delete", (d, w) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    courseRepository.deleteCourse(courseId, task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Course deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to delete course", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void deleteSection(String sectionId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Section?")
                .setMessage("All lessons in this section will also be deleted.")
                .setPositiveButton("Delete", (d, w) -> {
                    FirebaseUtils.getDb().collection("sections").document(sectionId)
                            .delete()
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, "Section deleted", Toast.LENGTH_SHORT).show();
                                loadSections();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void deleteLesson(Lesson lesson) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Lesson?")
                .setPositiveButton("Delete", (d, w) -> {
                    lessonRepository.deleteLesson(lesson, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Lesson deleted", Toast.LENGTH_SHORT).show();
                            loadSections();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void showEditLessonDialog(Lesson lesson) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_lesson, null);
        EditText etTitle    = dialogView.findViewById(R.id.etLessonTitle);
        EditText etVideoUrl = dialogView.findViewById(R.id.etVideoUrl);
        EditText etDuration = dialogView.findViewById(R.id.etDuration);

        etTitle.setText(lesson.getTitle());
        etVideoUrl.setText(lesson.getVideoUrl());
        etDuration.setText(String.valueOf(lesson.getDuration()));

        new AlertDialog.Builder(this)
                .setTitle("Edit Lesson")
                .setView(dialogView)
                .setPositiveButton("Update", (d, w) -> {
                    String title    = etTitle.getText().toString().trim();
                    String videoUrl = etVideoUrl.getText().toString().trim();
                    String durStr   = etDuration.getText().toString().trim();

                    if (title.isEmpty() || videoUrl.isEmpty()) {
                        Toast.makeText(this, "Title and video URL are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int oldDuration = lesson.getDuration();
                    lesson.setTitle(title);
                    lesson.setVideoUrl(videoUrl);
                    lesson.setDuration(durStr.isEmpty() ? 0 : Integer.parseInt(durStr));

                    lessonRepository.updateLesson(lesson, oldDuration, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Lesson updated", Toast.LENGTH_SHORT).show();
                            loadSections();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void showAddLessonDialog(String sectionId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_lesson, null);
        EditText etTitle    = dialogView.findViewById(R.id.etLessonTitle);
        EditText etVideoUrl = dialogView.findViewById(R.id.etVideoUrl);
        EditText etDuration = dialogView.findViewById(R.id.etDuration);

        new AlertDialog.Builder(this)
                .setTitle("Add Lesson")
                .setView(dialogView)
                .setPositiveButton("Add", (d, w) -> {
                    String title    = etTitle.getText().toString().trim();
                    String videoUrl = etVideoUrl.getText().toString().trim();
                    String durStr   = etDuration.getText().toString().trim();

                    if (title.isEmpty() || videoUrl.isEmpty()) {
                        Toast.makeText(this, "Title and video URL are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int duration = durStr.isEmpty() ? 0 : Integer.parseInt(durStr);
                    Lesson lesson = new Lesson();
                    lesson.setTitle(title);
                    lesson.setVideoUrl(videoUrl);
                    lesson.setDuration(duration);
                    lesson.setSectionId(sectionId);
                    lesson.setCourseId(courseId);
                    lesson.setOrder(0);

                    lessonRepository.addLesson(lesson, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUtils.getDb().collection("courses").document(courseId)
                                    .update("totalDuration", com.google.firebase.firestore.FieldValue.increment(duration));
                            Toast.makeText(this, "Lesson added", Toast.LENGTH_SHORT).show();
                            loadSections();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
