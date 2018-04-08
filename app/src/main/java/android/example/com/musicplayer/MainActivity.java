package android.example.com.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView mainMusiclv;
    List<AudioModel> musicList;
    MediaPlayer currentlyPlaying;
    AudioModel currentSongModel;

    TextView underFlowSongName, underFlowSongArtist;
    ImageButton underFlowSongCover;
    Button playPauseBtn;

    int[] IMAGES = { R.drawable.first, R.drawable.second};
    String[] NAMES = { "First", "Second" };
    String[] DESCRIPTIONS = { "This is the first", "This is the second" };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MusicHelper musicHelper = new MusicHelper();
        musicList = musicHelper.getAllAudioFromDevice(this);
        currentlyPlaying = new MediaPlayer();
        playPauseBtn = (Button) findViewById(R.id.playPauseBtn);

        mainMusiclv = (ListView) findViewById(R.id.mainMusicListView);
        CustomAdapter customAdapter = new CustomAdapter();
        mainMusiclv.setAdapter(customAdapter);

        underFlowSongName = (TextView) findViewById(R.id.underFlowSongName);
        underFlowSongArtist = (TextView) findViewById(R.id.underFlowArtist);
        underFlowSongCover = (ImageButton) findViewById(R.id.udnerFlowSongCover);

        mainMusiclv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Play item on list
                Log.i("item: ", musicList.get(i).getaPath());
                currentSongModel = musicList.get(i);
                try {
                    currentlyPlaying.stop();
                    currentlyPlaying.release();
                    currentlyPlaying = new MediaPlayer();
                    playPauseBtn.setText("Pause");
                    currentlyPlaying.setDataSource(musicList.get(i).getaPath());
                    currentlyPlaying.prepare();
                    currentlyPlaying.start();
                    underFlowSongName.setText(musicList.get(i).getaName());
                    underFlowSongArtist.setText(musicList.get(i).getaArtist());

                    String coverPath = getCoverArtPath(musicList.get(i).getId(), MainActivity.this);

                    if (coverPath != null) {
                        if (!coverPath.isEmpty()) {
                            File imgFile = new File(coverPath);

                            if (imgFile.exists()) {
                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                underFlowSongCover.setImageBitmap(myBitmap);
                            }
                        }
                    }

                    Intent[] intents = { new Intent("com.android.yakovmusic.metachanged"),
                            new Intent("com.android.yakovmusic.queuechanged")
                    };

                    for (int it = 0; it < intents.length; it++) {
                        intents[it].putExtra("command", "");
                        intents[it].putExtra("songid", currentSongModel.getId());
                        intents[it].putExtra("artist", currentSongModel.getaArtist());
                        intents[it].putExtra("album", currentSongModel.getaAlbum());
                        intents[it].putExtra("track", currentSongModel.getTitle());
                        intents[it].putExtra("albumid", currentSongModel.getId());
                        intents[it].putExtra("playing", true);
                        sendBroadcast(intents[it]);
                    }
                    Log.i("Broadcast sent", "true");
                } catch (Exception e) {
                    Log.i("Broadcast exception", e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (currentlyPlaying.isPlaying()) {
                    currentlyPlaying.pause();
                    playPauseBtn.setText("Play");
                } else {
                    currentlyPlaying.start();
                    playPauseBtn.setText("Pause");
                }
            }
        });

        underFlowSongCover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (currentSongModel != null) {
                    Intent i = new Intent(getBaseContext(), songDetails.class);
                    i.putExtra("song_name", currentSongModel.getTitle());
                    i.putExtra("song_album", currentSongModel.getaAlbum());
                    i.putExtra("song_cover", getCoverArtPath(currentSongModel.getId(), getBaseContext()));
                    startActivity(i);
                }
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

    class CustomAdapter extends BaseAdapter {
        public int getCount () {
            return musicList.size();
        }

        public Object getItem (int i) {
            return null;
        }

        public long getItemId (int i) {
            return 0;
        }

        public View getView (int i, View view, ViewGroup viewGroup) {
            try {
                view = getLayoutInflater().inflate(R.layout.music_list_item, null);

                ImageView songCover = (ImageView) view.findViewById(R.id.musicMainListItemSongImage);
                TextView songName = (TextView) view.findViewById(R.id.musicMainListItemSongName);
                TextView songArtist = (TextView) view.findViewById(R.id.musicMainListItemSongArtist);

                songName.setText(musicList.get(i).getTitle());
                songArtist.setText(musicList.get(i).getaArtist());
                String coverPath = getCoverArtPath(musicList.get(i).getId(), MainActivity.this);

                if (!coverPath.isEmpty()) {
                    File imgFile = new File(coverPath);

                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        songCover.setImageBitmap(myBitmap);
                    }
                }
            } catch (NullPointerException exception) {
                Log.i("nullll", "llll");
            }

            return view;
        }
    }
}