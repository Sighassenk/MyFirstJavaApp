package com.example.myapplication.repositories;

import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Section;
import com.example.myapplication.models.Lesson;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CourseRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "CourseRepository";

    public void getAllCourses(OnSuccessListener<List<Course>> listener) {
        db.collection("courses")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Course> courses = snapshot.toObjects(Course.class);
                    listener.onSuccess(courses);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching all courses", e);
                    listener.onSuccess(new ArrayList<>());
                });
    }

    public void getTrendingCourses(OnSuccessListener<List<Course>> listener) {
        db.collection("courses")
                .orderBy("enrollmentCount", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Course> courses = snapshot.toObjects(Course.class);
                    listener.onSuccess(courses);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching trending courses", e);
                    // Fallback to getting any 3 courses if index not ready
                    db.collection("courses")
                            .limit(3)
                            .get()
                            .addOnSuccessListener(snap -> listener.onSuccess(snap.toObjects(Course.class)));
                });
    }

    public void getCoursesByCategory(String category, OnSuccessListener<List<Course>> listener) {
        db.collection("courses")
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(snapshot ->
                        listener.onSuccess(snapshot.toObjects(Course.class)))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching courses by category", e);
                    listener.onSuccess(new ArrayList<>());
                });
    }

    public void getInstructorCourses(String instructorId, OnSuccessListener<List<Course>> listener) {
        db.collection("courses")
                .whereEqualTo("instructorId", instructorId)
                .get()
                .addOnSuccessListener(snapshot ->
                        listener.onSuccess(snapshot.toObjects(Course.class)))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching instructor courses", e);
                    listener.onSuccess(new ArrayList<>());
                });
    }

    public void createCourse(Course course, OnCompleteListener<Void> listener) {
        String id = db.collection("courses").document().getId();
        course.setId(id);
        db.collection("courses").document(id)
                .set(course)
                .addOnCompleteListener(listener);
    }

    public void updateCourse(Course course, OnCompleteListener<Void> listener) {
        db.collection("courses").document(course.getId())
                .set(course)
                .addOnCompleteListener(listener);
    }

    public void deleteCourse(String courseId, OnCompleteListener<Void> listener) {
        db.collection("courses").document(courseId)
                .delete()
                .addOnCompleteListener(listener);
    }

    public void getSections(String courseId, OnSuccessListener<List<Section>> listener) {
        db.collection("sections")
                .whereEqualTo("courseId", courseId)
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Section> sections = snapshot.toObjects(Section.class);
                    listener.onSuccess(sections);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Ordered query failed, falling back to manual sort", e);
                    db.collection("sections")
                            .whereEqualTo("courseId", courseId)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                List<Section> sections = snapshot.toObjects(Section.class);
                                Collections.sort(sections, (s1, s2) -> Integer.compare(s1.getOrder(), s2.getOrder()));
                                listener.onSuccess(sections);
                            })
                            .addOnFailureListener(e2 -> listener.onSuccess(new ArrayList<>()));
                });
    }

    public void getLessons(String sectionId, OnSuccessListener<List<Lesson>> listener) {
        db.collection("lessons")
                .whereEqualTo("sectionId", sectionId)
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshot ->
                        listener.onSuccess(snapshot.toObjects(Lesson.class)))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Ordered lesson query failed, falling back to manual sort", e);
                    db.collection("lessons")
                            .whereEqualTo("sectionId", sectionId)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                List<Lesson> lessons = snapshot.toObjects(Lesson.class);
                                Collections.sort(lessons, (l1, l2) -> Integer.compare(l1.getOrder(), l2.getOrder()));
                                listener.onSuccess(lessons);
                            })
                            .addOnFailureListener(e2 -> listener.onSuccess(new ArrayList<>()));
                });
    }
    
    public void getCoursesByIds(List<String> ids, OnSuccessListener<List<Course>> listener) {
        if (ids == null || ids.isEmpty()) {
            listener.onSuccess(new ArrayList<>());
            return;
        }
        db.collection("courses")
                .whereIn("id", ids)
                .get()
                .addOnSuccessListener(snap -> listener.onSuccess(snap.toObjects(Course.class)))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching courses by IDs", e);
                    listener.onSuccess(new ArrayList<>());
                });
    }
}
