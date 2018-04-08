package android.example.com.musicplayer;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov on 1/26/2018.
 */

public class AlbumsSongsHelper {
    public static List<AudioModel> getArtistSongs (final Context context, String album_id) {
        List<AudioModel> songs = new ArrayList<>();

        String[] projection = {
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ARTIST,
        };
        String selection = "album_id = ?";
        String[] selectionArgs = { album_id };

        Cursor c = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);

        if (c != null) {
            while (c.moveToNext()) {
                AudioModel audioModel = new AudioModel();
                long ALBUM_ID = c.getLong(0);
                String path = c.getString(1);
                String album = c.getString(2);
                String title = c.getString(3);
                String artist = c.getString(4);

                String name = path.substring(path.lastIndexOf("/") + 1);

                audioModel.setaName(name);
                audioModel.setaAlbum(album);
                audioModel.setaArtist(artist);
                audioModel.setId(ALBUM_ID);
                audioModel.setaPath(path);
                audioModel.setTitle(title);

                songs.add(audioModel);
            }
            c.close();
        }

        return songs;
    }
}
