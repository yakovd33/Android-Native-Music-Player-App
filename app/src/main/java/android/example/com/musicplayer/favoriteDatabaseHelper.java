package android.example.com.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by yakov on 2/1/2018.
 */

public class favoriteDatabaseHelper {
    private SQLiteOpenHelper _openHelper;

    String TABLE_NAME = "favorites";
    String COL1 = "_ID";
    String COL2 = "SONG_ID";
    String COL3 = "DATE";

    public favoriteDatabaseHelper(Context context) {
        _openHelper = new SimpleSQLiteOpenHelper(context);
    }

    class SimpleSQLiteOpenHelper extends SQLiteOpenHelper {
        SimpleSQLiteOpenHelper(Context context) {
            super(context, "main.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL1 +" integer primary key autoincrement, " + COL2 + " integer, " + COL3 + " text)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    public Cursor getAll() {
        SQLiteDatabase db = _openHelper.getReadableDatabase();
        if (db == null) {
            return null;
        }
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public boolean find(long song_id) {
        SQLiteDatabase db = _openHelper.getReadableDatabase();
        if (db == null) {
            return false;
        }

        ContentValues row = new ContentValues();
        Cursor cur = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL2 + " = ? LIMIT 1", new String[] { String.valueOf(song_id) });
        if (!cur.moveToNext()) {
            cur.close();
            db.close();
            return false;
        } else {
            cur.close();
            db.close();
            return true;
        }
    }

    public long add(long song_id) {
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        if (db == null) {
            return 0;
        }
        ContentValues row = new ContentValues();
        row.put(COL2, song_id);
        row.put(COL3, getTimestamp());
        long id = db.insert(TABLE_NAME, null, row);
        db.close();
        return id;
    }

    public void delete(long song_id) {
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        if (db == null) {
            return;
        }
        db.delete(TABLE_NAME, COL2 + " = ?", new String[] { String.valueOf(song_id) });
        db.close();
    }

    public List<AudioModel> getFavsModels (Context context) {
        List<AudioModel> songs = new ArrayList<>();
        AudioHelperSearch searchHelper = new AudioHelperSearch();
        Cursor c = this.getAll();

        while (c.moveToNext()) {
            long song_id = c.getLong(1);
            AudioModel tmpSong = searchHelper.getSongById(context, song_id);
            songs.add(tmpSong);
        }
        c.close();

        return songs;
    }

    private String getTimestamp () {
        return new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.US).format(new Date());
    }
}