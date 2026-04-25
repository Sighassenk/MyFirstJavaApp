package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Course;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList;
    private List<Course> fullList;
    private final OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public CourseAdapter(List<Course> courseList, OnCourseClickListener listener) {
        this.courseList = courseList;
        this.fullList   = new ArrayList<>(courseList);
        this.listener   = listener;
    }

    @NonNull @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);

        holder.tvTitle.setText(course.getTitle());
        holder.tvInstructor.setText("By " + course.getInstructorName());
        holder.tvCategory.setText(course.getCategory());
        holder.tvPrice.setText(course.getPrice() == 0 ? "Free" : String.format("$%.2f", course.getPrice()));
        holder.tvRating.setText(String.format("%.1f", course.getRating()));

        if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(course.getThumbnailUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> listener.onCourseClick(course));
    }

    @Override
    public int getItemCount() { return courseList.size(); }

    public void updateList(List<Course> newList) {
        this.courseList = newList;
        this.fullList   = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        List<Course> filtered = new ArrayList<>();
        if (text.isEmpty()) {
            filtered.addAll(fullList);
        } else {
            String query = text.toLowerCase().trim();
            for (Course c : fullList) {
                if (c.getTitle().toLowerCase().contains(query) ||
                    c.getCategory().toLowerCase().contains(query)) {
                    filtered.add(c);
                }
            }
        }
        courseList = filtered;
        notifyDataSetChanged();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView  tvTitle, tvInstructor, tvCategory, tvRating, tvPrice;

        CourseViewHolder(View itemView) {
            super(itemView);
            ivThumbnail  = itemView.findViewById(R.id.ivThumbnail);
            tvTitle      = itemView.findViewById(R.id.tvTitle);
            tvInstructor = itemView.findViewById(R.id.tvInstructor);
            tvCategory   = itemView.findViewById(R.id.tvCategory);
            tvRating     = itemView.findViewById(R.id.tvRating);
            tvPrice      = itemView.findViewById(R.id.tvPrice);
        }
    }
}
