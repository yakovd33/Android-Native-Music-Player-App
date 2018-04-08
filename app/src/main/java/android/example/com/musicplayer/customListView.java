package android.example.com.musicplayer;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * Created by yakov on 1/14/2018.
 */

public class customListView extends ArrayAdapter<AudioModel> {
    public customListView(Context context, int resource) {
        super(context, resource);
    }
}
