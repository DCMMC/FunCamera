package tk.dcmmcc.funcamera;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ss.bottomnavigation.BottomNavigation;
import com.ss.bottomnavigation.events.OnSelectedItemChangeListener;
import com.yyx.beautifylib.model.BLBeautifyParam;
import com.yyx.beautifylib.model.BLPickerParam;
import com.yyx.beautifylib.model.BLResultParam;
import com.yyx.beautifylib.utils.ToastUtils;

import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import tk.dcmmcc.funcamera.tensorflow.StylizeActivity;

public class ProcessPhotoActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {
    //拍的照片的bitmap
    private Bitmap photoBitMap;
    //工具栏
    private HorizontalScrollView toolLists;
    private ImageView image;
    private final int REQUEST_CODE_PERMISSION = 0;
    //private SketchImageView sketchImageView = (SketchImageView) findViewById(R.id.image_main);
    boolean flag = false;
    //图片存储的地址
    private String fName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_photo);

        toolLists = (HorizontalScrollView) findViewById(R.id.list_tools);
        image = (ImageView) findViewById(R.id.image);

        //receive bitmap
        if(getIntent().hasExtra("byteArray")) {
            photoBitMap = BitmapFactory.decodeByteArray(
                    getIntent().getByteArrayExtra("byteArray"), 0,
                    getIntent().getByteArrayExtra("byteArray").length);
        }

        if (getIntent().hasExtra("fName")) {
            fName = getIntent().getStringExtra("fName");
            //保存bitmap到fName
        }

        image.setImageBitmap(photoBitMap);

        BottomNavigation bottomNavigation=(BottomNavigation)findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelected(false);
        bottomNavigation.setOnSelectedItemChangeListener(new OnSelectedItemChangeListener() {
            @Override
            public void onSelectedItemChanged(int itemId) {
                switch (itemId){
                    case R.id.tab_sticker:
                        //toolLists.setVisibility(View.VISIBLE);
                        if (flag)
                            gotoPhotoPickActivity();
                        else
                            flag = true;
                        break;
                    case R.id.tab_filter:
                        Intent intent = new Intent(ProcessPhotoActivity.this,
                                StylizeActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.tab_text_label:
                        //toolLists.setVisibility(View.VISIBLE);
                        gotoPhotoPickActivity();
                        break;
                }
            }
        });
    }

    //跳转图片选择页面
    @AfterPermissionGranted(REQUEST_CODE_PERMISSION)
    private void gotoPhotoPickActivity() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            //BLPickerParam.startActivity(ProcessPhotoActivity.this);
            BLBeautifyParam param = new BLBeautifyParam(Arrays.asList(new String[] {fName}));
            BLBeautifyParam.startActivity(ProcessPhotoActivity.this, param);
        } else {
            EasyPermissions.requestPermissions(this, "图片选择需要以下权限:\n\n1.访问读写权限",
                    REQUEST_CODE_PERMISSION, perms);
        }
    }

    //获取返回结果数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == BLPickerParam.REQUEST_CODE_PHOTO_PICKER) {
            BLResultParam param = data.getParcelableExtra(BLResultParam.KEY);
            List<String> imageList = param.getImageList();

            /*
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(imageList.get(0), options);
            image.setImageBitmap(bitmap);
            */
            Glide.with(ProcessPhotoActivity.this)
                    .load(imageList.get(0))
                    .into(image);

            StringBuilder sb = new StringBuilder();
            for (String path:imageList){
                sb.append(path);
                sb.append("\n");
            }
            ToastUtils.toast(this, sb.toString());
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            Toast.makeText(this, "您拒绝了读取图片的权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,
                this);
    }

}
