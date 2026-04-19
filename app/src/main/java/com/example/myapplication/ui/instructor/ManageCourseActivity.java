package com.example.myapplication.ui.instructor;

import android.os.Bundle;
import android.view.LayoutInflater;
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
        
        // Long click on course title to delete course
        tvCourseTitle.setOnLongClickListener(v -> {
            showDeleteCourseDialog();
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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
                    sectionList, null, false, 0, this);
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
                .setMessage("This will permanently remove the course and all its content.")
                .setPositiveButton("Delete", (d, w) -> {
                    FirebaseUtils.getDb().collection("courses").document(courseId)
                            .delete()
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, "Course deleted", Toast.LENGTH_SHORT).show();
                                finish();
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
                            // Increment totalLessons and totalDuration on the course doc
                            FirebaseUtils.getDb().collection("courses").document(courseId)
                                    .update("totalLessons", com.google.firebase.firestore.FieldValue.increment(1),
                                            "totalDuration", com.google.firebase.firestore.FieldValue.increment(duration));
                            Toast.makeText(this, "Lesson added", Toast.LENGTH_SHORT).show();
                            loadSections();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
