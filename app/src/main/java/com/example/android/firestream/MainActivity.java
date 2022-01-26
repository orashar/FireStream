package com.example.android.firestream;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private ImageView playPauseBtn;
    private TextView startTime, endTime;
    private Button selectVideo;
    private ProgressBar progressBar, bufferBar;

    private Uri videoUri;

    private boolean isPlaying = false;

    private int currrentTimer = 0, totalDuration = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.videoview);
        playPauseBtn = findViewById(R.id.play_pause);
        progressBar = findViewById(R.id.progressbar);
        bufferBar = findViewById(R.id.bufferbar);
        startTime = findViewById(R.id.start_time);
        endTime = findViewById(R.id.end_time);
        selectVideo = findViewById(R.id.select_video);

        selectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), 1);
            }
        });

        findViewById(R.id.skip_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.seekTo(getProgress() + 10000);
            }
        });

        progressBar.setMax(100);

        videoUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/firestream-95bf1.appspot.com/o/EMIWAY%20-%20KHATAM%20KARONA%20(Prod%20by%20PSYIK).mp4?alt=media&token=03ab0a11-0a82-401e-aa96-b29747376e94");

        initPlayer(videoUri);
    }

    public void initPlayer(Uri uri){

        videoView.setVideoURI(uri);
        videoView.requestFocus();

        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if(what == mp.MEDIA_INFO_BUFFERING_START){
                    Log.v("Buffering", "buffering starts");
                    bufferBar.setVisibility(View.VISIBLE);
                } else if(what == mp.MEDIA_INFO_BUFFERING_END){

                    Log.v("Buffering", "buffering ends");
                    bufferBar.setVisibility(View.GONE);
                } else {

                    Log.v("Buffering", "buffering else");
                    bufferBar.setVisibility(View.GONE);
                }

                return false;
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        videoView.resume();
                    }
                });


                totalDuration = mp.getDuration()/1000;
                String totalDurationString = String.format("%02d:%02d", totalDuration / 60, totalDuration % 60);
                endTime.setText(totalDurationString);
            }
        });

        videoView.start();
        isPlaying = true;

        playPauseBtn.setImageResource(R.drawable.ic_pause);

        new VideoProgress().execute();

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying){
                    videoView.pause();
                    playPauseBtn.setImageResource(R.drawable.ic_play);
                    isPlaying = false;
                } else{
                    videoView.start();
                    playPauseBtn.setImageResource(R.drawable.ic_pause);
                    isPlaying = true;
                }
            }
        });
    }

    public class VideoProgress extends AsyncTask<Void, Integer, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            do {
                if(isPlaying) {
                    try {

                        currrentTimer = getCurrentPosition() / 1000;

                        int currentPercent = currrentTimer*100/totalDuration;

                        publishProgress(currentPercent);

                    } catch (Exception e){

                    }
                }

            } while (getProgress() <= 100);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

                progressBar.setProgress(values[0]);

                String currentTimerString = String.format("%02d:%02d", currrentTimer/60, currrentTimer%60);
                startTime.setText(currentTimerString);


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == 1){
                Uri selectedUri = data.getData();

                initPlayer(selectedUri);
            }
        }
    }


    private int getProgress() {
        return progressBar.getProgress();
    }

    private int getCurrentPosition() {
        return videoView.getCurrentPosition();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isPlaying = false;
    }

    /*
    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.start();
    }*/
}
