package com.example.myapplication.repositories;

import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.myapplication.models.Lesson;
import com.example.myapplication.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class LessonRepository {

    private final FirebaseFirestore db = FirebaseUtils.getDb();
    private static final String TAG = "LessonRepository";

    public void addLesson(Lesson lesson, OnCompleteListener<Void> listener) {
        String id = db.collection("lessons").document().getId();
        lesson.setId(id);
        db.collection("lessons").document(id)
                .set(lesson)
                .addOnCompleteListener(listener);
    }

    public void getLessonsBySection(String sectionId, OnSuccessListener<List<Lesson>> listener) {
        Log.d(TAG, "Fetching lessons for section: " + sectionId);
        db.collection("lessons")
                .whereEqualTo("sectionId", sectionId)
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Lesson> lessons = snap.toObjects(Lesson.class);
                    Log.d(TAG, "Successfully fetched " + lessons.size() + " lessons for section: " + sectionId);
                    listener.onSuccess(lessons);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Ordered query failed for lessons (missing index?), falling back to unordered", e);
                    db.collection("lessons")
                            .whereEqualTo("sectionId", sectionId)
                            .get()
                            .addOnSuccessListener(snap -> {
                                List<Lesson> lessons = snap.toObjects(Lesson.class);
                                Log.d(TAG, "Successfully fetched " + lessons.size() + " lessons (unordered) for section: " + sectionId);
                                listener.onSuccess(lessons);
                            })
                            .addOnFailureListener(e2 -> {
                                Log.e(TAG, "Fallback lesson query failed", e2);
                                listener.onSuccess(new ArrayList<>());
                            });
                });
    }

    public void getLessonsByCourse(String courseId, OnSuccessListener<List<Lesson>> listener) {
        db.collection("lessons")
                .whereEqualTo("courseId", courseId)
                .get()
                .addOnSuccessListener(snap -> listener.onSuccess(snap.toObjects(Lesson.class)))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching lessons by course", e);
                    listener.onSuccess(new ArrayList<>());
                });
    }

    public void updateLesson(Lesson lesson, OnCompleteListener<Void> listener) {
        db.collection("lessons").document(lesson.getId())
                .set(lesson)
                .addOnCompleteListener(listener);
    }

    public void deleteLesson(String lessonId, OnCompleteListener<Void> listener) {
        db.collection("lessons").document(lessonId)
                .delete()
                .addOnCompleteListener(listener);
    }
}
