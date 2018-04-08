package android.example.com.musicplayer;

/**
 * Created by yakov on 1/14/2018.
 */

public class AudioModel {
    long id;
    long album_id;
    String aPath;
    String aName;
    String aAlbum;
    String aArtist;
    String cover;
    String title;

    public void setId (long id) {
        this.id = id;
    }

    public long getId () {
        return this.id;
    }

    public long getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(long album_id) {
        this.album_id = album_id;
    }

    public String getaPath() {
        return aPath;
    }

    public void setaPath(String aPath) {
        this.aPath = aPath;
    }

    public String getaName() {
        return aName;
    }

    public void setaName(String aName) {
        this.aName = aName;
    }

    public String getaAlbum() {
        return aAlbum;
    }

    public void setaAlbum(String aAlbum) {
        this.aAlbum = aAlbum;
    }

    public String getaArtist() {
        return aArtist;
    }

    public void setaArtist(String aArtist) {
        this.aArtist = aArtist;
    }

    public void setCover (String cover) {
        this.cover = cover;
    }

    public String getCover () {
        return this.cover;
    }

    public void setTitle (String title) {
        this.title = title;
    }

    public String getTitle () {
        return this.title;
    }
}