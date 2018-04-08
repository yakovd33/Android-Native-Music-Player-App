package android.example.com.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.concurrent.TimeUnit;


public class songDetails extends AppCompatActivity {
    TextView songName, songArtist;
    ImageView songCover;
    TextView currTimeTv, durationTv;
    SeekBar songSeek;
    Button prevBtn, playPauseBtn, nextBtn;
    ImageButton favCurSong;
    favoriteDatabaseHelper favSongHelper;

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_details);
        Intent intent = getIntent();
        AudioModel currentSongModel = TabbedDisplay.currentSongModel;
        favSongHelper = new favoriteDatabaseHelper(getBaseContext());

        songName = (TextView) findViewById(R.id.songName);
        songArtist = (TextView) findViewById(R.id.songArtist);
        songCover = (ImageView) findViewById(R.id.songDetsCover);
        currTimeTv = (TextView) findViewById(R.id.songDetsCurDur);
        durationTv = (TextView) findViewById(R.id.songDetsTotalDur);

        String coverPath = getCoverArtPath(currentSongModel.getAlbum_id(), songDetails.this);

        if (coverPath != null) {
            if (!coverPath.isEmpty()) {
                File imgFile = new File(coverPath);

                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    songCover.setImageBitmap(myBitmap);
                }
            }
        } else {
            songCover.setImageResource(R.drawable.default_cover);
        }

        songName.setText(currentSongModel.getTitle());
        songArtist.setText(currentSongModel.getaArtist());

        int mediaPosition = TabbedDisplay.currentlyPlaying.getCurrentPosition();
        int posSeconds = (int) (mediaPosition / 1000) % 60 ;
        int posMinutes = (int) ((mediaPosition / (1000*60)) % 60);
        int posHours = (int) ((mediaPosition / (1000*60*60)) % 24);
        currTimeTv.setText(String.format("%d:%02d", posMinutes, posSeconds));

        final int mediaDuration = TabbedDisplay.currentlyPlaying.getDuration();
        int songTotalSeconds = mediaDuration / 1000;
        int durSeconds = (int) (mediaDuration / 1000) % 60 ;
        int durMinutes = (int) ((mediaDuration / (1000*60)) % 60);
        int durHours = (int) ((mediaDuration / (1000*60*60)) % 24);
        durationTv.setText(String.format("%d:%02d", durMinutes, durSeconds));

        songSeek = (SeekBar) findViewById(R.id.songSeek);
        songSeek.setMax(songTotalSeconds);
        songSeek.setProgress(mediaPosition / 1000);

        Thread t = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                int mediaPosition = TabbedDisplay.currentlyPlaying.getCurrentPosition();
                                int posSeconds = (int) (mediaPosition / 1000) % 60 ;
                                int posMinutes = (int) ((mediaPosition / (1000*60)) % 60);
                                int posHours = (int) ((mediaPosition / (1000*60*60)) % 24);
                                currTimeTv.setText(String.format("%d:%02d", posMinutes, posSeconds));
                                songSeek.setProgress(mediaPosition / 1000);
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        t.start();

        prevBtn = (Button) findViewById(R.id.songDetsPrevBtn);
        playPauseBtn = (Button) findViewById(R.id.songDetsPlayBtn);
        nextBtn = (Button) findViewById(R.id.songDetsNextBtn);

        prevBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TabbedDisplay.prevSongBtn.callOnClick();
            }
        });

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TabbedDisplay.playPauseBtn.callOnClick();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TabbedDisplay.nextSongBtn.callOnClick();
            }
        });

        favCurSong = (ImageButton) findViewById(R.id.favCurSong);

        if (favSongHelper.find(TabbedDisplay.currentSongModel.getId())) {
            favCurSong.setImageResource(R.drawable.heart_active);
            favCurSong.setTag("on");
        }

        favCurSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (favCurSong.getTag() == "off") {
                    favCurSong.setImageResource(R.drawable.heart_active);
                    favSongHelper.add(TabbedDisplay.currentSongModel.getId());
                    favCurSong.setTag("on");
                } else {
                    favCurSong.setImageResource(R.drawable.heart);
                    favSongHelper.delete(TabbedDisplay.currentSongModel.getId());
                    favCurSong.setTag("off");
                }
            }
        });

        songSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int mediaPosition = TabbedDisplay.currentlyPlaying.getCurrentPosition();
                int posSeconds = (int) (mediaPosition / 1000) % 60 ;
                int posMinutes = (int) ((mediaPosition / (1000*60)) % 60);
                int posHours = (int) ((mediaPosition / (1000*60*60)) % 24);
                currTimeTv.setText(String.format("%d:%02d", posMinutes, posSeconds));
                if (fromUser) {
                    TabbedDisplay.currentlyPlaying.seekTo(progress * 1000);
                }

                if (mediaPosition == mediaDuration) {
                    final Intent intent = getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private static String getCoverArtPath(long albumId, Context context) {
        Cursor albumCursor = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + " = ?",
                new String[]{Long.toString(albumId)},
                null
        );

        boolean queryResult = albumCursor.moveToFirst();
        String result = null;
        if (queryResult) {
            result = albumCursor.getString(0);
        }

        albumCursor.close();
        return result;
    }
}
