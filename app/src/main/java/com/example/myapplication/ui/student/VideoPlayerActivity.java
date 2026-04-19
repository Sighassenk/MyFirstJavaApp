package com.example.myapplication.ui.student;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.example.myapplication.R;
import com.example.myapplication.repositories.EnrollmentRepository;
import com.example.myapplication.utils.FirebaseUtils;

public class VideoPlayerActivity extends AppCompatActivity {

    private static final String TAG = "VideoPlayerActivity";
    private ExoPlayer            player;
    private PlayerView           playerView;
    private EnrollmentRepository enrollmentRepo;

    private String enrollmentId, lessonId, lessonTitle, courseId;
    private int    lessonDuration = 0;
    private int    courseTotalDuration = 1; // Default to 1 to avoid div by zero

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        enrollmentId   = getIntent().getStringExtra("enrollmentId");
        lessonId       = getIntent().getStringExtra("lessonId");
        lessonTitle    = getIntent().getStringExtra("lessonTitle");
        courseId       = getIntent().getStringExtra("courseId");
        lessonDuration = getIntent().getIntExtra("lessonDuration", 0);
        String videoUrl = getIntent().getStringExtra("videoUrl");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(lessonTitle != null ? lessonTitle : "Video Lesson");
        }

        if (videoUrl == null || videoUrl.isEmpty()) {
            Toast.makeText(this, "Error: Invalid Video URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        enrollmentRepo = new EnrollmentRepository();
        playerView     = findViewById(R.id.playerView);

        // Fetch total course duration for progress tracking
        FirebaseUtils.getDb().collection("courses").document(courseId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("totalDuration")) {
                        courseTotalDuration = doc.getLong("totalDuration").intValue();
                        if (courseTotalDuration <= 0) courseTotalDuration = 1;
                    }
                });

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        Log.d(TAG, "Attempting to play video from: " + videoUrl);
        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    markComplete();
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Log.e(TAG, "Player Error: " + error.getMessage(), error);
                Toast.makeText(VideoPlayerActivity.this, "Playback Error: " + error.getErrorCodeName(), Toast.LENGTH_LONG).show();
            }
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

    private void markComplete() {
        if (enrollmentId == null || lessonId == null) return;
        
        enrollmentRepo.completeLesson(enrollmentId, lessonId, lessonDuration, courseTotalDuration, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "✓ Progress Updated!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) player.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
