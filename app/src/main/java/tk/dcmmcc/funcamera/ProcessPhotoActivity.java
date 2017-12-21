package tk.dcmmcc.funcamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ProcessPhotoActivity extends AppCompatActivity {

    private Bitmap photoBitMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_photo);

        //receive bitmap
        if(getIntent().hasExtra("byteArray")) {
            photoBitMap = BitmapFactory.decodeByteArray(
                    getIntent().getByteArrayExtra("byteArray"), 0,
                    getIntent().getByteArrayExtra("byteArray").length);
        }
    }
}
