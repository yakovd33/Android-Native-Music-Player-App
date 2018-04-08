package android.example.com.musicplayer;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov on 1/27/2018.
 */

public class AudioHelperSearch {
    public static List<AudioModel> getSongsByName (final Context context, String searchQuery) {
        List<AudioModel> songs = new ArrayList<>();

        String[] projection = {
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.Media._ID
        };

        String selection = MediaStore.Audio.Media.TITLE + " LIKE ? OR " + MediaStore.Audio.AudioColumns.ARTIST + " LIKE ?";
        String[] selectionArgs = { "%" + searchQuery + "%", "%" + searchQuery + "%" };

        Cursor c = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);

        if (c != null) {
            while (c.moveToNext()) {
                AudioModel audioModel = new AudioModel();
                long ALBUM_ID = c.getLong(0);
                String path = c.getString(1);
                String album = c.getString(2);
                String title = c.getString(3);
                String artist= c.getString(4);
                long _id = c.getLong(5);

                String name = path.substring(path.lastIndexOf("/") + 1);

                audioModel.setaName(name);
                audioModel.setaAlbum(album);
                audioModel.setaArtist(artist);
                audioModel.setAlbum_id(ALBUM_ID);
                audioModel.setaPath(path);
                audioModel.setTitle(title);
                audioModel.setId(_id);

                songs.add(audioModel);
            }
            c.close();
        }

        return songs;
    }

    public static AudioModel getSongById (final Context context, long searchId) {
        AudioModel audioModel = null;

        String[] projection = {
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.Media._ID
        };

        String selection = MediaStore.Audio.Media._ID + "=?";
        String[] selectionArgs = { "" + searchId };

        Cursor c = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);

        if (c != null) {
            while (c.moveToNext()) {
                audioModel = new AudioModel();
                long ALBUM_ID = c.getLong(0);
                String path = c.getString(1);
                String album = c.getString(2);
                String title = c.getString(3);
                String artist= c.getString(4);
                long _id = c.getLong(5);

                String name = path.substring(path.lastIndexOf("/") + 1);

                audioModel.setaName(name);
                audioModel.setaAlbum(album);
                audioModel.setaArtist(artist);
                audioModel.setAlbum_id(ALBUM_ID);
                audioModel.setaPath(path);
                audioModel.setTitle(title);
                audioModel.setId(_id);
            }
            c.close();
        }

        return audioModel;
    }
}
