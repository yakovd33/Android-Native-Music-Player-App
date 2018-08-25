package android.example.com.musicplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
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
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TabbedDisplay extends AppCompatActivity implements SensorEventListener {
    List<AudioModel> musicList;
    List<ArtistModel> artists;
    List<AlbumModel> albums;
    List<AudioModel> suffledList = null;
    boolean isShuffled = false;
    int shuffledIndex = 0;
    int songIndex = 0;
    Button shufflePlayBtn;
    private static final int SEARCH_REQ_CODE = 845;

    Button favoritesBtn;

    ListView mainMusiclv;
    public static MediaPlayer currentlyPlaying;
    public static AudioModel currentSongModel;
    TextView underFlowSongName, underFlowSongArtist;
    ImageButton underFlowSongCover;
    static Button playPauseBtn;
    static Button prevSongBtn;
    static Button nextSongBtn;
    Intent searchIntent = null;

    String curTab = "songs";

    View currentSongListItem;

    private long lastUpdate = -1;
    private float x, y, z;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 2500;
    private SensorManager sensorMgr;

    final MediaPlayer.OnCompletionListener songCompleteListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            nextSong();
            currentlyPlaying.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    nextSong();
                }
            });
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed_display);

        ActivityCompat.requestPermissions(TabbedDisplay.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do
                    withPermissions();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(TabbedDisplay.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void withPermissions () {
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        MusicHelper musicHelper = new MusicHelper();
        musicList = musicHelper.getAllAudioFromDevice(this);
        currentlyPlaying = new MediaPlayer();
        artists = ArtistHelper.getAllArtistsFromDevice(getBaseContext());
        albums = AlbumsHelper.getAllAlbumsFromDevice(getBaseContext());

        playPauseBtn = (Button) findViewById(R.id.playPauseBtn);
        prevSongBtn = (Button) findViewById(R.id.prevSongBtn);
        nextSongBtn = (Button) findViewById(R.id.nextSongBtn);
        mainMusiclv = (ListView) findViewById(R.id.mainMusicListView);
        CustomAdapter customAdapter = new CustomAdapter();
        mainMusiclv.setAdapter(customAdapter);

        underFlowSongName = (TextView) findViewById(R.id.underFlowSongName);
        underFlowSongArtist = (TextView) findViewById(R.id.underFlowArtist);
        underFlowSongCover = (ImageButton) findViewById(R.id.udnerFlowSongCover);

        mainMusiclv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Play item on list
                //Log.i("item: ", musicList.get(i).getaPath());

                if (isShuffled) {
                    shuffledIndex = i;
                } else {
                    songIndex = i;
                }

                if (curTab.equals("songs")) {
                    Log.i("choosing songs", "hehehe");
                    currentSongModel = musicList.get(i);
                    try {
                        currentlyPlaying.stop();
                        currentlyPlaying.release();
                        currentlyPlaying = new MediaPlayer();
                        playPauseBtn.setText("Pause");
                        currentlyPlaying.setDataSource(musicList.get(i).getaPath());
                        currentlyPlaying.prepare();
                        currentlyPlaying.start();
                        underFlowSongName.setText(musicList.get(i).getTitle());
                        underFlowSongArtist.setText(musicList.get(i).getaArtist());

                        if (currentSongListItem != null) {
                            ImageView previousSongItemPlayBtn = (ImageView) currentSongListItem.findViewById(R.id.currentlyPlaying);
                            TextView previousSongNameView = (TextView) currentSongListItem.findViewById(R.id.musicMainListItemSongName);
                            previousSongItemPlayBtn.setVisibility(View.GONE);
                            previousSongNameView.setTextColor(Color.BLACK);
                        }

                        ImageView currentlyPlayingPlayBtn = (ImageView) view.findViewById(R.id.currentlyPlaying);
                        TextView currentSongNameView = (TextView) view.findViewById(R.id.musicMainListItemSongName);
                        currentlyPlayingPlayBtn.setVisibility(View.VISIBLE);
                        currentSongNameView.setTextColor(Color.parseColor("#105870"));

                        currentSongListItem = view;

                        String coverPath = getCoverArtPath(musicList.get(i).getAlbum_id(), TabbedDisplay.this);

                        if (coverPath != null) {
                            if (!coverPath.isEmpty()) {
                                File imgFile = new File(coverPath);

                                if (imgFile.exists()) {
                                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                    underFlowSongCover.setImageBitmap(myBitmap);
                                }
                            }
                        } else {
                            underFlowSongCover.setImageResource(R.drawable.default_cover);
                        }

                        Intent[] intents = {new Intent("com.android.yakovmusic.metachanged"),
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

                if (curTab.equals("artists")) {
                    ArtistSongsHelper artistSongsHelper = new ArtistSongsHelper();
                    musicList = artistSongsHelper.getArtistSongs(getBaseContext(), artists.get(i).getName(), false);
                    CustomAdapter customAdapter = new CustomAdapter();
                    mainMusiclv.setAdapter(customAdapter);
                    curTab = "songs";
                }

                if (curTab.equals("albums")) {
                    AlbumsSongsHelper albumsSongsHelper = new AlbumsSongsHelper();
                    musicList = albumsSongsHelper.getArtistSongs(getBaseContext(), Long.toString(albums.get(i).getId()));
                    CustomAdapter customAdapter = new CustomAdapter();
                    mainMusiclv.setAdapter(customAdapter);
                    curTab = "songs";
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

        shufflePlayBtn = (Button) findViewById(R.id.shuffleBtn);
        shufflePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curTab = "songs";
                shuffledIndex = 0;
                ShuffleSongs shuffleSongs = new ShuffleSongs();
                suffledList = shuffleSongs.shuffle(musicList);
                currentSongModel = suffledList.get(shuffledIndex);
                musicList = suffledList;
                CustomAdapter customAdapter1 = new CustomAdapter();
                mainMusiclv.setAdapter(customAdapter1);
                isShuffled = true;
                currentSongListItem = getViewByPosition(0, mainMusiclv);

                try {
                    currentlyPlaying.stop();
                    currentlyPlaying.reset();
                    currentlyPlaying = new MediaPlayer();
                    currentlyPlaying.setDataSource(currentSongModel.getaPath());
                    currentlyPlaying.prepare();
                    currentlyPlaying.start();
                    currentlyPlaying.setOnCompletionListener(songCompleteListener);
                    underFlowSongName.setText(currentSongModel.getTitle());
                    underFlowSongArtist.setText(currentSongModel.getaArtist());

                    String coverPath = getCoverArtPath(currentSongModel.getAlbum_id(), TabbedDisplay.this);

                    if (coverPath != null) {
                        if (!coverPath.isEmpty()) {
                            File imgFile = new File(coverPath);

                            if (imgFile.exists()) {
                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                underFlowSongCover.setImageBitmap(myBitmap);
                            }
                        }
                    } else {
                        underFlowSongCover.setImageResource(R.drawable.default_cover);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        underFlowSongCover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (currentSongModel != null) {
                    Intent i = new Intent(getBaseContext(), songDetails.class);
                    i.putExtra("song_name", currentSongModel.getTitle());
                    i.putExtra("song_album", currentSongModel.getaAlbum());
                    i.putExtra("song_cover", getCoverArtPath(currentSongModel.getAlbum_id(), getBaseContext()));
                    startActivity(i);
                }
            }
        });

        nextSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextSong();
            }
        });

        prevSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevSong();
            }
        });

        favoritesBtn = (Button) findViewById(R.id.getFavoritesBtn);
        favoritesBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                curTab = "songs";
                favoriteDatabaseHelper favSongsHelper = new favoriteDatabaseHelper(getBaseContext());
                musicList = favSongsHelper.getFavsModels(getBaseContext());
                CustomAdapter customAdapter1 = new CustomAdapter();
                mainMusiclv.setAdapter(customAdapter1);
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
            if (musicList != null) {
                return musicList.size();
            } else {
                return 0;
            }
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
                ImageView playBtn = (ImageView) view.findViewById(R.id.currentlyPlaying);

                songName.setText(musicList.get(i).getTitle());
                songArtist.setText(musicList.get(i).getaArtist());
                String coverPath = getCoverArtPath(musicList.get(i).getAlbum_id(), TabbedDisplay.this);

                if (currentSongModel != null) {
                    if (musicList.get(i).getTitle().equals(currentSongModel.getTitle())) {
                        Log.i("Same song > ", musicList.get(i).getaName());
                        songName.setTextColor(Color.parseColor("#105870"));
                        playBtn.setVisibility(View.VISIBLE);
                    }
                }

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

    class CustomArtistsAdapter extends BaseAdapter {
        public int getCount () {
            return artists.size();
        }

        public Object getItem (int i) {
            return artists.get(i);
        }

        public long getItemId (int i) {
            return artists.get(i).getId();
        }

        public View getView (int i, View view, ViewGroup viewGroup) {
            try {
                view = getLayoutInflater().inflate(R.layout.artist_list_item, null);

                ImageView artistCover = (ImageView) view.findViewById(R.id.musicMainListItemArtistImage);
                TextView artistName = (TextView) view.findViewById(R.id.musicArtistsListItemArtistName);
                TextView artistDetails = (TextView) view.findViewById(R.id.musicArtistsListItemArtistDetails);

                artistName.setText(artists.get(i).getName());
                artistDetails.setText(Integer.toString(artists.get(i).getAlbumsCount()) + " Albums " + Integer.toString(artists.get(i).getSongsCount()) + " Songs");
                ArtistSongsHelper artistSongHelper = new ArtistSongsHelper();
                List<AudioModel> lastSong = artistSongHelper.getArtistSongs(getBaseContext(), artists.get(i).getName(), true);
                AudioModel lastSongModel = lastSong.get(0);
                Log.i("artist songs count > ", Long.toString(lastSongModel.getAlbum_id()));
                String coverPath = getCoverArtPath(lastSongModel.getAlbum_id(), TabbedDisplay.this);

                if (!coverPath.isEmpty()) {
                    File imgFile = new File(coverPath);

                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        artistCover.setImageBitmap(myBitmap);
                    }
                } else {
                    artistCover.setImageResource(R.drawable.default_cover);
                }
            } catch (NullPointerException exception) {
                exception.printStackTrace();
                Log.i("nullll", exception.getMessage() + "1");
            }

            return view;
        }
    }

    class CustomAlbumsAdapter extends BaseAdapter {
        public int getCount () {
            return albums.size();
        }

        public Object getItem (int i) {
            return albums.get(i);
        }

        public long getItemId (int i) {
            return albums.get(i).getId();
        }

        public View getView (int i, View view, ViewGroup viewGroup) {
            try {
                view = getLayoutInflater().inflate(R.layout.album_list_item, null);

                ImageView albumCover = (ImageView) view.findViewById(R.id.musicMainListItemAlbumImage);
                TextView albumName = (TextView) view.findViewById(R.id.musicArtistsListItemAlbumName);
                TextView albumDetails = (TextView) view.findViewById(R.id.musicArtistsListItemAlbumDetails);

                albumName.setText(albums.get(i).getName());
                albumDetails.setText(albums.get(i).getArtist());
                String coverPath = getCoverArtPath(albums.get(i).getId(), TabbedDisplay.this);

                if (!coverPath.isEmpty()) {
                    File imgFile = new File(coverPath);

                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        albumCover.setImageBitmap(myBitmap);
                    }
                }
            } catch (NullPointerException exception) {
                Log.i("nullll", "llll");
            }

            return view;
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        BottomNavigationView bottomNavigationView;

        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
            curTab = item.getTitle().toString().toLowerCase();

            switch (item.getItemId()) {
                case R.id.navigation_artists :
                    CustomArtistsAdapter customArtistsAdapter = new CustomArtistsAdapter();
                    mainMusiclv.setAdapter(customArtistsAdapter);
                    return true;
                case R.id.navigation_songs :
                    if (!isShuffled) {
                        MusicHelper musicHelper = new MusicHelper();
                        musicList = musicHelper.getAllAudioFromDevice(getBaseContext());
                    }
                    CustomAdapter customAdapter = new CustomAdapter();
                    mainMusiclv.setAdapter(customAdapter);
                    return true;
                case R.id.navigation_albums :
                    AlbumsHelper albumsHelper = new AlbumsHelper();
                    albums = albumsHelper.getAllAlbumsFromDevice(getBaseContext());
                    CustomAlbumsAdapter customAlbumsAdapter = new CustomAlbumsAdapter();
                    mainMusiclv.setAdapter(customAlbumsAdapter);
                    return true;
            }
            return false;
        }
    };

    protected void onResume() {
        super.onResume();
        if(sensorMgr == null) {
            sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
            boolean accelSupported = sensorMgr.registerListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);

            if (!accelSupported) {
                // on accelerometer on this device
                sensorMgr.unregisterListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                x = sensorEvent.values[0];
                y = sensorEvent.values[1];
                z = sensorEvent.values[2];

                float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;
                if (speed > SHAKE_THRESHOLD) {
                    if (searchIntent == null) {
                        searchIntent = new Intent(TabbedDisplay.this, Search.class);
                        startActivityForResult(searchIntent, SEARCH_REQ_CODE);
                    }
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEARCH_REQ_CODE) {
            searchIntent = null;
            if(resultCode == Activity.RESULT_OK){
                long resultSongId = data.getLongExtra("result", 1);
                AudioHelperSearch audioHelperSearch = new AudioHelperSearch();
                AudioModel resultSongModel = audioHelperSearch.getSongById(TabbedDisplay.this, resultSongId);
                currentSongModel = resultSongModel;
                currentlyPlaying.release();
                currentlyPlaying = new MediaPlayer();
                playPauseBtn.setText("Pause");
                try {
                    currentlyPlaying.stop();
                    currentlyPlaying.setDataSource(resultSongModel.getaPath());
                    currentlyPlaying.prepare();
                    currentlyPlaying.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                underFlowSongName.setText(resultSongModel.getTitle());
                underFlowSongArtist.setText(resultSongModel.getaArtist());

                String coverPath = getCoverArtPath(resultSongModel.getAlbum_id(), TabbedDisplay.this);

                if (coverPath != null) {
                    if (!coverPath.isEmpty()) {
                        File imgFile = new File(coverPath);

                        if (imgFile.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            underFlowSongCover.setImageBitmap(myBitmap);
                        }
                    }
                } else {
                    underFlowSongCover.setImageResource(R.drawable.default_cover);
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void nextSong () {
        if (isShuffled) {
            shuffledIndex++;

            if (shuffledIndex > suffledList.size() - 1) {
                shuffledIndex = 0;
            }

            currentSongModel = suffledList.get(shuffledIndex);
            currentlyPlaying.stop();
            currentlyPlaying.release();
            currentlyPlaying = new MediaPlayer();
            try {
                currentlyPlaying.setDataSource(currentSongModel.getaPath());
                currentlyPlaying.prepare();
                currentlyPlaying.start();
                underFlowSongName.setText(currentSongModel.getTitle());
                underFlowSongArtist.setText(currentSongModel.getaArtist());
                currentlyPlaying.setOnCompletionListener(songCompleteListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            songIndex++;

            if (songIndex > musicList.size() - 1) {
                songIndex = 0;
            }

            currentSongModel = musicList.get(songIndex);
            currentlyPlaying.stop();
            currentlyPlaying.release();
            currentlyPlaying = new MediaPlayer();
            try {
                currentlyPlaying.setDataSource(currentSongModel.getaPath());
                currentlyPlaying.prepare();
                currentlyPlaying.start();
                underFlowSongName.setText(currentSongModel.getTitle());
                underFlowSongArtist.setText(currentSongModel.getaArtist());
                currentlyPlaying.setOnCompletionListener(songCompleteListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        View newListItem = getViewByPosition(shuffledIndex, mainMusiclv);
        ImageView previousSongItemPlayBtn = (ImageView) currentSongListItem.findViewById(R.id.currentlyPlaying);
        TextView previousSongNameView = (TextView) currentSongListItem.findViewById(R.id.musicMainListItemSongName);
        previousSongItemPlayBtn.setVisibility(View.GONE);
        previousSongNameView.setTextColor(Color.BLACK);
        currentSongListItem = newListItem;
        ImageView newSongItemPlayBtn = (ImageView) currentSongListItem.findViewById(R.id.currentlyPlaying);
        TextView newSongNameView = (TextView) currentSongListItem.findViewById(R.id.musicMainListItemSongName);
        newSongItemPlayBtn.setVisibility(View.VISIBLE);
        newSongNameView.setHintTextColor(Color.parseColor("#105870"));

        String coverPath = getCoverArtPath(currentSongModel.getAlbum_id(), TabbedDisplay.this);

        if (coverPath != null) {
            if (!coverPath.isEmpty()) {
                File imgFile = new File(coverPath);

                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    underFlowSongCover.setImageBitmap(myBitmap);
                }
            }
        } else {
            underFlowSongCover.setImageResource(R.drawable.default_cover);
        }
    }

    private void prevSong () {
        if (isShuffled) {
            shuffledIndex--;

            if (shuffledIndex < 0) {
                shuffledIndex = suffledList.size() - 1;
            }

            currentSongModel = suffledList.get(shuffledIndex);
            currentlyPlaying.stop();
            currentlyPlaying.release();
            currentlyPlaying = new MediaPlayer();
            try {
                currentlyPlaying.setDataSource(currentSongModel.getaPath());
                currentlyPlaying.prepare();
                currentlyPlaying.start();
                underFlowSongName.setText(currentSongModel.getTitle());
                underFlowSongArtist.setText(currentSongModel.getaArtist());
                currentlyPlaying.setOnCompletionListener(songCompleteListener);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            songIndex--;

            songIndex--;

            if (songIndex < 0) {
                songIndex = musicList.size() - 1;
            }

            currentSongModel = musicList.get(songIndex);
            currentlyPlaying.stop();
            currentlyPlaying.release();
            currentlyPlaying = new MediaPlayer();
            try {
                currentlyPlaying.setDataSource(currentSongModel.getaPath());
                currentlyPlaying.prepare();
                currentlyPlaying.start();
                underFlowSongName.setText(currentSongModel.getTitle());
                underFlowSongArtist.setText(currentSongModel.getaArtist());
                currentlyPlaying.setOnCompletionListener(songCompleteListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        View newListItem = getViewByPosition(shuffledIndex, mainMusiclv);
        ImageView previousSongItemPlayBtn = (ImageView) currentSongListItem.findViewById(R.id.currentlyPlaying);
        TextView previousSongNameView = (TextView) currentSongListItem.findViewById(R.id.musicMainListItemSongName);
        previousSongItemPlayBtn.setVisibility(View.GONE);
        previousSongNameView.setTextColor(Color.BLACK);
        currentSongListItem = newListItem;
        ImageView newSongItemPlayBtn = (ImageView) currentSongListItem.findViewById(R.id.currentlyPlaying);
        TextView newSongNameView = (TextView) currentSongListItem.findViewById(R.id.musicMainListItemSongName);
        newSongItemPlayBtn.setVisibility(View.VISIBLE);
        newSongNameView.setHintTextColor(Color.parseColor("#105870"));
        String coverPath = getCoverArtPath(currentSongModel.getAlbum_id(), TabbedDisplay.this);

        if (coverPath != null) {
            if (!coverPath.isEmpty()) {
                File imgFile = new File(coverPath);

                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    underFlowSongCover.setImageBitmap(myBitmap);
                }
            }
        } else {
            underFlowSongCover.setImageResource(R.drawable.default_cover);
        }
    }

    public View getViewByPosition(int pos, ListView listView) {
        try {
            final int firstListItemPosition = listView
                    .getFirstVisiblePosition();
            final int lastListItemPosition = firstListItemPosition
                    + listView.getChildCount() - 1;

            if (pos < firstListItemPosition || pos > lastListItemPosition) {
                //This may occure using Android Monkey, else will work otherwise
                return listView.getAdapter().getView(pos, null, listView);
            } else {
                final int childIndex = pos - firstListItemPosition;
                return listView.getChildAt(childIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}