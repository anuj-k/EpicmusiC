package com.example.epicmusic;

import androidx.appcompat.app.AppCompatActivity;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chibde.visualizer.LineVisualizer;

public class MainActivity extends AppCompatActivity {

    //class objects
    private Animation fade_in, fade_out;
    private Button playPauseButton;
    private CardView cardView;
    private ImageView vinylArt;
    private LineVisualizer lineVisualizer;
    private MediaPlayer mediaPlayer;
    private ObjectAnimator rotateCard;
    private SeekBar seekBar;
    private TextView playTime, totalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mediaPlayer initializing
        mediaPlayer = MediaPlayer.create(this, R.raw.omw);

        //id referencing
        playPauseButton = findViewById(R.id.playPauseButton);
        vinylArt = findViewById(R.id.albumArt_vinylArt);
        cardView = findViewById(R.id.albumArt_cardView);
        lineVisualizer = findViewById(R.id.lineViz);
        seekBar = findViewById(R.id.seeker);
        playTime = findViewById(R.id.runningTime);
        totalTime = findViewById(R.id.totalTime);

        //initialize total-time string
        totalTime.setText(songTime(mediaPlayer.getDuration()));

        //animation initializing
        fade_in = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
        fade_out = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);

        //rotate animation for album art
        rotateCard = ObjectAnimator.ofFloat(cardView, "rotation", 0, 360);
        rotateCard.setInterpolator(new LinearInterpolator());
        rotateCard.setDuration(2500);
        rotateCard.setRepeatCount(ObjectAnimator.INFINITE);
        rotateCard.setRepeatMode(ObjectAnimator.RESTART);

        //line visualizer attributes
        lineVisualizer.setStrokeWidth(2);
        lineVisualizer.setColor(ContextCompat.getColor(this, R.color.av_light_blue));

        //library
        /*Bundle songBundle = getIntent().getExtras();

        ArrayList<String> songList = (ArrayList) songBundle.getParcelableArrayList("songList");
        int pos = songBundle.getInt("songIndex");
        Uri uri = Uri.parse(songList.get(pos));

        if(songList.isEmpty()) { mediaPlayer = MediaPlayer.create(this, R.raw.omw); }
        else { mediaPlayer = MediaPlayer.create(this, uri); }*/

        //seek-bar implementation
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {

                    mediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //update playing-time
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(mediaPlayer != null) {
                    try {
                        Message message = new Message();
                        message.what = mediaPlayer.getCurrentPosition();

                        handler.sendMessage(message);

                        Thread.sleep(1000);
                    }catch (InterruptedException ignored) {

                    }
                }
            }
        }).start();
    }

    //play-pause button function
    public void playPauseSong(View view) {

        if(!mediaPlayer.isPlaying()) {

            //change icon to pause
            playPauseButton.startAnimation(fade_out);
            playPauseButton.setBackgroundResource(R.drawable.pausebutton_icon);
            playPauseButton.startAnimation(fade_in);

            //initialize audio visualization
            lineVisualizer.setPlayer(mediaPlayer.getAudioSessionId());

            //circular album art with centered vinyl graphic
            cardView.setRadius(1000);
            vinylArt.setVisibility(View.VISIBLE);

            //start rotate animation
            if(mediaPlayer.getCurrentPosition() == 0) { rotateCard.start(); }
            else { rotateCard.resume(); }

            //start or resume playing
            mediaPlayer.start();
        }
        else {

            //change icon to play
            playPauseButton.startAnimation(fade_out);
            playPauseButton.setBackgroundResource(R.drawable.playbutton_icon);
            playPauseButton.startAnimation(fade_in);

            //pause rotate animation
            rotateCard.pause();

            //pause playing
            mediaPlayer.pause();
        }
    }

    //stop button function
    public void stopSong (View view) {

        //revert album art to non-playing state
        rotateCard.end();
        cardView.setRadius(30);

        //stop playing
        mediaPlayer.pause();
        mediaPlayer.seekTo(0);

        //change icon to play if playing stops
        if (!mediaPlayer.isPlaying()) {
            playPauseButton.startAnimation(fade_out);
            playPauseButton.setBackgroundResource(R.drawable.playbutton_icon);
            playPauseButton.startAnimation(fade_in);
        }
    }

    //return time-text string
    public String songTime(int time) {

        int sec = time / 1000 % 60;

        if(sec < 10) { return (time / 1000 / 60) + ":0" + sec; }
        else { return (time / 1000 / 60) + ":" + sec; }
    }

    //updating seek-bar and play-time
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint("SetTextIl8n")

        @Override
        public void handleMessage(Message message) {
            int progress = message.what;
            seekBar.setProgress(progress);

            playTime.setText(songTime(progress));
        }
    };
}