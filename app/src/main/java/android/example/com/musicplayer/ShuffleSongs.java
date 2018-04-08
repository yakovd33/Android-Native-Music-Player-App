package android.example.com.musicplayer;

import java.util.List;
import java.util.Random;

/**
 * Created by yakov on 1/28/2018.
 */

public class ShuffleSongs {
    List<AudioModel> suffledList;

    public List<AudioModel> shuffle(List<AudioModel> musicList) {
        suffledList = shuffleList(musicList);
        return suffledList;
    }

    public List<AudioModel> shuffleList(List<AudioModel> a) {
        List<AudioModel> tmpList = a;

        int n = tmpList.size();
        Random random = new Random();
        random.nextInt();
        for (int i = 0; i < n; i++) {
            int change = i + random.nextInt(n - i);
            swap(tmpList, i, change);
        }

        return tmpList;
    }

    private void swap(List<AudioModel> a, int i, int change) {
        AudioModel helper = a.get(i);
        a.set(i, a.get(change));
        a.set(change, helper);
    }
}
