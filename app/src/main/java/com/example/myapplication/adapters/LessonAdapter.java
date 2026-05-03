package com.example.myapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.Lesson;
import com.example.myapplication.ui.instructor.ManageCourseActivity;
import com.example.myapplication.ui.student.VideoPlayerActivity;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private final List<Lesson> lessons;
    private final String       enrollmentId;
    private final boolean      isEnrolled;
    private final List<String>  completedLessons;
    private final Context      context;

    public LessonAdapter(List<Lesson> lessons, String enrollmentId,
                         boolean isEnrolled, List<String> completedLessons, Context context) {
        this.lessons      = lessons;
        this.enrollmentId = enrollmentId;
        this.isEnrolled   = isEnrolled;
        this.completedLessons = completedLessons;
        this.context      = context;
    }

    @NonNull @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);

        holder.tvLessonTitle.setText(lesson.getTitle());

        int mins = lesson.getDuration() / 60;
        int secs = lesson.getDuration() % 60;
        holder.tvDuration.setText(String.format("%d:%02d", mins, secs));

        boolean isDone = completedLessons != null && completedLessons.contains(lesson.getId());
        
        if (isDone) {
            holder.tvLessonTitle.setTextColor(Color.parseColor("#4CAF50")); // Green color
        } else {
            holder.tvLessonTitle.setTextColor(Color.BLACK);
        }

        // Use standard icons
        holder.ivLock.setImageResource(
                isEnrolled ? android.R.drawable.ic_media_play : android.R.drawable.ic_lock_lock);

        // Instructor actions
        if (context instanceof ManageCourseActivity) {
            holder.layoutInstructorActions.setVisibility(View.VISIBLE);
            holder.btnEditLesson.setOnClickListener(v -> ((ManageCourseActivity) context).showEditLessonDialog(lesson));
            holder.btnDeleteLesson.setOnClickListener(v -> ((ManageCourseActivity) context).deleteLesson(lesson));
            holder.ivLock.setVisibility(View.GONE); // Hide play/lock icon in manage screen to save space
        } else {
            holder.layoutInstructorActions.setVisibility(View.GONE);
            holder.ivLock.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof ManageCourseActivity) return; // No playback in manage screen

            if (!isEnrolled) {
                Toast.makeText(context, "Enroll to watch this lesson", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra("videoUrl",      lesson.getVideoUrl());
            intent.putExtra("lessonId",      lesson.getId());
            intent.putExtra("lessonTitle",   lesson.getTitle());
            intent.putExtra("enrollmentId",  enrollmentId);
            intent.putExtra("courseId",      lesson.getCourseId());
            intent.putExtra("lessonDuration", lesson.getDuration());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return lessons.size(); }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView  tvLessonTitle, tvDuration;
        ImageView ivLock;
        LinearLayout layoutInstructorActions;
        View btnEditLesson, btnDeleteLesson;

        LessonViewHolder(View itemView) {
            super(itemView);
            tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
            tvDuration    = itemView.findViewById(R.id.tvDuration);
            ivLock        = itemView.findViewById(R.id.ivLock);
            layoutInstructorActions = itemView.findViewById(R.id.layoutInstructorActions);
            btnEditLesson   = itemView.findViewById(R.id.btnEditLesson);
            btnDeleteLesson = itemView.findViewById(R.id.btnDeleteLesson);
        }
    }
}
