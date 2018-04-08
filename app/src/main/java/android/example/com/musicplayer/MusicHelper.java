package android.example.com.musicplayer;

/**
 * Created by yakov on 1/14/2018.
 */

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MusicHelper {
    public List<AudioModel> getAllAudioFromDevice(final Context context) {

        final List<AudioModel> tempAudioList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.ArtistColumns.ARTIST,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.Media._ID
        };
        Cursor c = context.getContentResolver().query(uri, projection, null, null, null);

        if (c != null) {
            while (c.moveToNext()) {
                AudioModel audioModel = new AudioModel();
                long ALBUM_ID = c.getLong(0);
                String path = c.getString(1);
                String album = c.getString(2);
                String artist = c.getString(3);
                String title = c.getString(4);
                long _id = c.getLong(5);

                String name = path.substring(path.lastIndexOf("/") + 1);

                audioModel.setaName(name);
                audioModel.setaAlbum(album);
                audioModel.setaArtist(artist);
                audioModel.setAlbum_id(ALBUM_ID);
                audioModel.setaPath(path);
                audioModel.setTitle(title);
                audioModel.setId(_id);
                tempAudioList.add(audioModel);
            }
            c.close();
        }

        return tempAudioList;
    }
}
