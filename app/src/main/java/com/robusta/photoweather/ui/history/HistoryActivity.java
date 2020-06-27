package com.robusta.photoweather.ui.history;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.robusta.photoweather.R;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity implements HistoryContractor.View {
    private static final int REQUEST_READ = 1052;
    private RecyclerView historyRecycler;
    private HistoryContractor.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyRecycler = findViewById(R.id.history_recycler);
        presenter = new HistoryPresenter(this);

        //checking permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //requesting permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    REQUEST_READ);
            return;
        } else {
            presenter.getImagesFromDir();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted.
                    presenter.getImagesFromDir();
                } else {
                    // Permission denied
                    Toast.makeText(this, R.string.give_read, Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    @Override
    public void setData(ArrayList<String> paths) {
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        historyRecycler.setLayoutManager(layoutManager);
        historyRecycler.setAdapter(new RecyclerAdapter(paths));
    }
}
