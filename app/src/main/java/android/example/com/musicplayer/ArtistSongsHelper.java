package android.example.com.musicplayer;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov on 1/26/2018.
 */

public class ArtistSongsHelper {
    public static List<AudioModel> getArtistSongs (final Context context, String artist, boolean onlyLast) {
        List<AudioModel> songs = new ArrayList<>();

        String[] projection = {
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.Media._ID
        };
        String selection = MediaStore.Audio.Media.ARTIST + "=?";
        String[] selectionArgs = { artist };
        String sort = onlyLast ? " _id asc limit 1" : null;

        Cursor c = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sort);

        if (c != null) {
            while (c.moveToNext()) {
                AudioModel audioModel = new AudioModel();
                long ALBUM_ID = c.getLong(0);
                String path = c.getString(1);
                String album = c.getString(2);
                String title = c.getString(3);
                long _id = c.getLong(4);

                String name = path.substring(path.lastIndexOf("/") + 1);

                audioModel.setaName(name);
                audioModel.setaAlbum(album);
                audioModel.setaArtist(artist);
                audioModel.setId(_id);
                audioModel.setAlbum_id(ALBUM_ID);
                audioModel.setaPath(path);
                audioModel.setTitle(title);

                songs.add(audioModel);
            }
            c.close();
        }

        return songs;
    }
}
