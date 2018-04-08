package android.example.com.musicplayer;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov on 1/26/2018.
 */

public class AlbumsHelper {
    public static List<AlbumModel> getAllAlbumsFromDevice (final Context context) {
        final List<AlbumModel> albums = new ArrayList<>();

        String[] mProjection = {
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Artists.ARTIST,
        };

        Cursor artistCursor = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                mProjection,
                null,
                null,
                MediaStore.Audio.Artists.ARTIST + " ASC"
        );

        if (artistCursor != null) {
            while (artistCursor.moveToNext()) {
                AlbumModel albumModel = new AlbumModel();
                albumModel.setId(artistCursor.getLong(0));
                albumModel.setName(artistCursor.getString(1));
                albumModel.setArtist(artistCursor.getString(2));
                albums.add(albumModel);
            }
        }
        artistCursor.close();

        return albums;
    }
}
