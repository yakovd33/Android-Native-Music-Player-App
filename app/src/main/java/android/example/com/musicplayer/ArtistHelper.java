package android.example.com.musicplayer;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov on 1/26/2018.
 */

public class ArtistHelper {
    public static List<ArtistModel> getAllArtistsFromDevice (final Context context) {
        final List<ArtistModel> artists = new ArrayList<>();

        String[] mProjection = {
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
        };

        Cursor artistCursor = context.getContentResolver().query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                mProjection, null,
                null,
                MediaStore.Audio.Artists.ARTIST + " ASC"
        );

        if (artistCursor != null) {
            while (artistCursor.moveToNext()) {
                ArtistModel artistModel = new ArtistModel();
                artistModel.setId(artistCursor.getLong(0));
                artistModel.setName(artistCursor.getString(1));
                artistModel.setSongsCount(artistCursor.getInt(2));
                artistModel.setAlbumsCount(artistCursor.getInt(3));
                artists.add(artistModel);
            }
        }
        artistCursor.close();

        return artists;
    }
}