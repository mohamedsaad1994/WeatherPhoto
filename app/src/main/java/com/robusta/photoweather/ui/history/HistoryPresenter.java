package com.robusta.photoweather.ui.history;

import java.io.File;
import java.util.ArrayList;

public class HistoryPresenter implements HistoryContractor.Presenter {
    private HistoryContractor.View view;

    public HistoryPresenter(HistoryContractor.View view) {
        this.view = view;
    }

    @Override
    public void getImagesFromDir() {
        ArrayList<String> paths = new ArrayList<String>();// list of file paths
        File[] listFile;

        File file = new File(android.os.Environment.getExternalStorageDirectory(), "PhotoWeatherHistory");
        if (file.isDirectory()) {
            listFile = file.listFiles();
            for (File value : listFile) {
                paths.add(value.getAbsolutePath());
            }
        }
        view.setData(paths);
    }

}
