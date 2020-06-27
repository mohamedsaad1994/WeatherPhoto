package com.robusta.photoweather.ui.history;

import java.util.ArrayList;

public interface HistoryContractor {
    interface View {
        void setData(ArrayList<String> paths);
    }

    interface Presenter {
        void getImagesFromDir();
    }
}
