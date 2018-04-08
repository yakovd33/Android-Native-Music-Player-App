package android.example.com.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.security.Key;
import java.util.List;

public class Search extends AppCompatActivity {
    List<AudioModel> musicList;
    ListView searchList;
    EditText searchBox;
    final AudioHelperSearch searchHelper = new AudioHelperSearch();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().hide();

        searchList = (ListView) findViewById(R.id.searchList);
        MusicHelper musicHelper = new MusicHelper();
        musicList = musicHelper.getAllAudioFromDevice(this);
        Search.CustomAdapter customAdapter = new Search.CustomAdapter();
        searchList.setAdapter(customAdapter);

        // Handle search
        searchBox = (EditText) findViewById(R.id.searchInput);
        searchBox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    return false;
                }

                musicList = searchHelper.getSongsByName(Search.this, searchBox.getText().toString());
                Search.CustomAdapter customAdapter = new Search.CustomAdapter();
                searchList.setAdapter(customAdapter);
                return true;
            }
        });

        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AudioModel songResultModel = searchHelper.getSongById(Search.this, musicList.get(i).getId());

                Log.i("Song id > ", Long.toString(musicList.get(i).getId()));
                if (songResultModel != null) {
                    Log.i("Selected song name > ", songResultModel.getaName());
                    Bundle conData = new Bundle();
                    conData.putLong("result", musicList.get(i).getId());
                    Intent intent = new Intent();
                    intent.putExtras(conData);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
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
                String coverPath = getCoverArtPath(musicList.get(i).getAlbum_id(), Search.this);

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
