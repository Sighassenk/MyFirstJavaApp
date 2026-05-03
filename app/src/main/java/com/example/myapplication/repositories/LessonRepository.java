package com.example.myapplication.repositories;

import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
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
        db.collection("lessons")
                .whereEqualTo("sectionId", sectionId)
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Lesson> lessons = snap.toObjects(Lesson.class);
                    listener.onSuccess(lessons);
                })
                .addOnFailureListener(e -> {
                    db.collection("lessons")
                            .whereEqualTo("sectionId", sectionId)
                            .get()
                            .addOnSuccessListener(snap -> {
                                List<Lesson> lessons = snap.toObjects(Lesson.class);
                                java.util.Collections.sort(lessons, (l1, l2) -> Integer.compare(l1.getOrder(), l2.getOrder()));
                                listener.onSuccess(lessons);
                            })
                            .addOnFailureListener(e2 -> listener.onSuccess(new ArrayList<>()));
                });
    }

    public void updateLesson(Lesson lesson, int oldDuration, OnCompleteListener<Void> listener) {
        db.collection("lessons").document(lesson.getId())
                .set(lesson)
                .addOnSuccessListener(aVoid -> {
                    // Adjust course total duration if duration changed
                    int diff = lesson.getDuration() - oldDuration;
                    if (diff != 0) {
                        db.collection("courses").document(lesson.getCourseId())
                                .update("totalDuration", FieldValue.increment(diff));
                    }
                })
                .addOnCompleteListener(listener);
    }

    public void deleteLesson(Lesson lesson, OnCompleteListener<Void> listener) {
        db.collection("lessons").document(lesson.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Update course stats
                    db.collection("courses").document(lesson.getCourseId())
                            .update("totalDuration", FieldValue.increment(-lesson.getDuration()));
                })
                .addOnCompleteListener(listener);
    }
}
