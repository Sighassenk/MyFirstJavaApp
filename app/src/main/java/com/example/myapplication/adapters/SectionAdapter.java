package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.Section;
import com.example.myapplication.repositories.LessonRepository;
import com.example.myapplication.ui.instructor.ManageCourseActivity;
import com.example.myapplication.models.Lesson;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {

    private final List<Section> sections;
    private final String        enrollmentId;
    private final boolean       isEnrolled;
    private final int           totalLessons;
    private final Context       context;
    private final LessonRepository lessonRepository = new LessonRepository();

    public SectionAdapter(List<Section> sections, String enrollmentId,
                          boolean isEnrolled, int totalLessons, Context context) {
        this.sections     = sections;
        this.enrollmentId = enrollmentId;
        this.isEnrolled   = isEnrolled;
        this.totalLessons = totalLessons;
        this.context      = context;
    }

    @NonNull @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_section, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        Section section = sections.get(position);
        holder.tvSectionTitle.setText(section.getTitle());

        // Load lessons for this section
        lessonRepository.getLessonsBySection(section.getId(), new OnSuccessListener<List<Lesson>>() {
            @Override
            public void onSuccess(List<Lesson> lessons) {
                LessonAdapter lessonAdapter = new LessonAdapter(
                        lessons, enrollmentId, isEnrolled, totalLessons, context);

                holder.rvLessons.setLayoutManager(new LinearLayoutManager(context));
                holder.rvLessons.setNestedScrollingEnabled(false);
                holder.rvLessons.setAdapter(lessonAdapter);
            }
        });

        // Expand / collapse lessons
        holder.tvSectionTitle.setOnClickListener(v -> {
            boolean visible = holder.rvLessons.getVisibility() == View.VISIBLE;
            holder.rvLessons.setVisibility(visible ? View.GONE : View.VISIBLE);
            if (context instanceof ManageCourseActivity) {
                holder.btnAddLesson.setVisibility(visible ? View.GONE : View.VISIBLE);
            }
        });

        // Show Instructor-only controls
        if (context instanceof ManageCourseActivity) {
            holder.btnAddLesson.setVisibility(View.VISIBLE);
            holder.btnDeleteSection.setVisibility(View.VISIBLE);
            
            holder.btnAddLesson.setOnClickListener(v ->
                    ((ManageCourseActivity) context).showAddLessonDialog(section.getId()));
            
            holder.btnDeleteSection.setOnClickListener(v -> 
                    ((ManageCourseActivity) context).deleteSection(section.getId()));
        } else {
            holder.btnAddLesson.setVisibility(View.GONE);
            holder.btnDeleteSection.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return sections.size(); }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView     tvSectionTitle;
        RecyclerView rvLessons;
        Button       btnAddLesson;
        ImageButton  btnDeleteSection;

        SectionViewHolder(View itemView) {
            super(itemView);
            tvSectionTitle   = itemView.findViewById(R.id.tvSectionTitle);
            rvLessons        = itemView.findViewById(R.id.rvLessons);
            btnAddLesson     = itemView.findViewById(R.id.btnAddLesson);
            btnDeleteSection = itemView.findViewById(R.id.btnDeleteSection);
        }
    }
}
