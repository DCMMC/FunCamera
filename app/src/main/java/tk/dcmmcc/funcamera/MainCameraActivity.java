package tk.dcmmcc.funcamera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * 用Camera2 API 实现的简单相机界面
 */
public class MainCameraActivity extends AppCompatActivity
        implements TextureView.SurfaceTextureListener{
    private TextureView mPreviewView;
    private Handler mHandler = new Handler();
    private CaptureRequest.Builder mPreviewBuilder;
    private ImageReader mImageReader;
    private String mCameraId;
    private Size mPreviewSize;
    private CameraDevice mCameraDevice;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mPreviewSession;
    //相机是否是后置摄像头还是前置摄像头
    //默认为后置摄像头
    private boolean isLensFacingBack = true;
    //闪光灯是否激活, 默认不激活
    private boolean isFlashOn = false;
    private ImageView flashBtn;

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            //开启预览
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    };

    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            try {
                //创建捕获请求
                mCaptureRequest = mPreviewBuilder.build();
                mPreviewSession = session;
                //设置反复捕获数据的请求，这样预览界面就会一直有数据显示
                mPreviewSession.setRepeatingRequest(mCaptureRequest, mSessionCaptureCallback,
                        mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

        }
    };

    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            //重启预览
            restartPreview();
        }
    };


    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_camera);

        //check permission
        int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        //设置隐藏状态栏 SDK >= 19
        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(option);

        HandlerThread mThreadHandler = new HandlerThread("CAMERA2");
        mThreadHandler.start();
        mHandler = new Handler(mThreadHandler.getLooper());
        mPreviewView = (TextureView) findViewById(R.id.camera_textureView);
        mPreviewView.setSurfaceTextureListener(this);


        Button mTakePhoto = (Button) findViewById(R.id.takepicture);
        mTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    //获取屏幕方向
                    int rotation = getWindowManager().getDefaultDisplay().getRotation();
                    //设置CaptureRequest输出到mImageReader
                    //CaptureRequest添加imageReaderSurface，不加的话就会导致ImageReader的onImageAvailable()方法不会回调
                    mPreviewBuilder.addTarget(mImageReader.getSurface());
                    //设置拍照方向
                    mPreviewBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(rotation));
                    //聚焦
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                    //停止预览
                    mPreviewSession.stopRepeating();
                    //开始拍照，然后回调上面的接口重启预览，因为mPreviewBuilder设置ImageReader作为target，
                    //所以会自动回调ImageReader的onImageAvailable()方法保存图片
                    mPreviewSession.capture(mPreviewBuilder.build(), mSessionCaptureCallback,
                            null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        //闪光灯
        flashBtn = (ImageView) findViewById(R.id.flashBtn);
        //前后置摄像头切换
        ImageView changeCamera = (ImageView) findViewById(R.id.change);
        flashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnLight();
            }
        });
        changeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mPreviewSession.stopRepeating();
                    CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                    isLensFacingBack = !isLensFacingBack;
                    mCameraDevice.close();
                    setupCamera();
                    openCamera();
                } catch (CameraAccessException ca) {
                    Log.e("W", "切换相机出现异常.");
                    Toast.makeText(MainCameraActivity.this, R.string.change_camera_err,
                            Toast.LENGTH_LONG).show();
                    ca.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        setupCamera();
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /**
     * 闪光灯开关
     */
    private void turnLight() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED || manager == null) {
                Toast.makeText(this, R.string.no_camera_permission, Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                mPreviewBuilder.set(CaptureRequest.FLASH_MODE, isFlashOn ?
                        CameraMetadata.FLASH_MODE_OFF : CameraMetadata.FLASH_MODE_TORCH);
                mPreviewSession.capture(mPreviewBuilder.build(), mSessionCaptureCallback, null);
                if (isFlashOn) {
                    flashBtn.setImageResource(R.drawable.camera_flash_off);
                    isFlashOn = false;
                    Toast.makeText(this, R.string.close_torch, Toast.LENGTH_SHORT)
                            .show();

                } else {
                    flashBtn.setImageResource(R.drawable.camera_flash_on);
                    isFlashOn = true;
                    Toast.makeText(this, R.string.open_torch, Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                Toast.makeText(this, R.string.not_support_camera_flash, Toast.LENGTH_SHORT)
                        .show();
            }
        } catch (CameraAccessException ca) {
            Log.w(MainActivity.class.getName(), "打开闪光灯出错, 请检查权限");
            Toast.makeText(this, R.string.flash_camera_err, Toast.LENGTH_SHORT)
                    .show();
            ca.printStackTrace();
        }
    }

    /**
     * 设置相机(获取camera的ID)
     */
    private void setupCamera() {
        //获取摄像头的管理者CameraManager
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED || manager == null) {
                Toast.makeText(this, R.string.no_camera_permission, Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            //遍历所有摄像头
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == (isLensFacingBack ?
                        CameraCharacteristics.LENS_FACING_BACK : CameraCharacteristics.LENS_FACING_FRONT)) {
                    //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
                    StreamConfigurationMap map = characteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                    // 对于静态图像捕获，我们使用最大的可用尺寸。
                    mPreviewSize = Collections.max(
                            Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                            new Comparator<Size>() {
                                @Override
                                public int compare(Size lhs, Size rhs) {
                                    return Long.signum(lhs.getWidth() * lhs.getHeight()
                                            - rhs.getHeight() * rhs.getWidth());
                                }
                            });
                    mCameraId = id;
                    break;
                }

            }
        } catch (CameraAccessException e) {
            Log.w(MainActivity.class.getName(), "调用相机出错, 请检查权限");
            Toast.makeText(this, R.string.no_camera_permission, Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    /**
     * 打开相机
     */
    private void openCamera() {
        //获取摄像头的管理者CameraManager
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        //检查权限
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED || manager == null) {
                Toast.makeText(this, R.string.no_camera_permission, Toast.LENGTH_SHORT)
                    .show();
                return;
            }
            //打开相机，第一个参数指示打开哪个摄像头，第二个参数stateCallback为相机的状态回调接口，
            //第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            manager.openCamera(mCameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            Log.w(MainActivity.class.getName(), "调用相机出错, 请检查权限");
            Toast.makeText(this, R.string.no_camera_permission, Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    /**
     * 开始预览
     */
    private void startPreview() {
        SurfaceTexture mSurfaceTexture = mPreviewView.getSurfaceTexture();

        //设置TextureView的缓冲区大小
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

        //获取Surface显示预览数据
        Surface mSurface = new Surface(mSurfaceTexture);

        setupImageReader();

        //获取ImageReader的Surface
        Surface imageReaderSurface = mImageReader.getSurface();

        try {
            //创建CaptureRequestBuilder，TEMPLATE_PREVIEW比表示预览请求
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //设置Surface作为预览数据的显示界面
            mPreviewBuilder.addTarget(mSurface);
            //创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            mCameraDevice.createCaptureSession(Arrays.asList(mSurface, imageReaderSurface), mSessionStateCallback,
                    null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重新启动相机预览
     */
    private void restartPreview() {
        try {
            //执行setRepeatingRequest方法就行了，注意mCaptureRequest是之前开启预览设置的请求
            mPreviewSession.setRepeatingRequest(mCaptureRequest, null, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置图片输出为JPEG
     */
    private void setupImageReader() {

        //前三个参数分别是需要的尺寸和格式，最后一个参数代表每次最多获取几帧数据，这里的2代表ImageReader中
        //最多可以获取两帧图像流
        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(),
                ImageFormat.JPEG, 2);

        //监听ImageReader的事件，当有图像流数据可用时会回调onImageAvailable方法，它的参数就是预览帧数据，
        //可以对这帧数据进行处理
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mHandler.post(new ImageSaver(reader.acquireNextImage()));
            }
        }, mHandler);
    }

    /**
     * 存储照片
     */
    public class ImageSaver implements Runnable {

        private Image mImage;
        private File mFile;

        ImageSaver(Image image) {
            this.mImage = image;
        }

        @Override
        public void run() {
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.US);

            String fname = "IMG_" +
                    sdf.format(new Date())
                    + ".jpg";
            mFile = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + "FunCamera");
            //mFile = new File(getApplication().getExternalFilesDir(null), fname);

            try {
                File f = new File(mFile.getAbsolutePath());
                if (f.isFile())
                    if (f.delete())
                        throw new IOException("删除文件错误");
                if (!f.exists())
                    if (!f.mkdirs())
                        throw new IOException("写入文件夹错误");
                f = new File(mFile.getAbsolutePath() + File.separator
                        + fname);
                if (!f.exists())
                    if (!f.createNewFile())
                        throw new IOException("写入文件错误");
            } catch (IOException e) {
                // 写入文件错误
                Toast.makeText(MainCameraActivity.this, "写入照片出错!", Toast.LENGTH_LONG)
                        .show();
                Log.e("E", "写入文件路径 " + mFile + File.separator
                        + fname + " 错误");
                e.printStackTrace();
            }
            try (FileOutputStream output = new FileOutputStream(mFile.getAbsolutePath()
                    + File.separator + fname)) {
                // 转跳到照片修改界面
                try {
                    ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);

                    Bitmap bitmap =
                            BitmapFactory.decodeByteArray(bytes, 0,
                                    bytes.length, null);
                    if (bitmap != null) {
                        //put bitmap in intentExtra
                        ByteArrayOutputStream bs = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bs);
                        Intent intent = new Intent(MainCameraActivity.this,
                                ProcessPhotoActivity.class);
                        intent.putExtra("byteArray", bs.toByteArray());
                        intent.putExtra("fName", mFile.getAbsolutePath() + File.separator
                            + fname);

                        startActivity(intent);

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, output);

                        Toast.makeText(MainCameraActivity.this, "File save in " + mFile.getAbsolutePath(), Toast.LENGTH_LONG)
                                .show();
                        if (!mFile.exists() || !mFile.canRead()) {
                            Log.e("E", "拍照文件不存在或者无权限");
                            Toast.makeText(MainCameraActivity.this, "拍照文件不存在或者无权限",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // 其次把文件插入到系统图库
                        try {
                            MediaStore.Images.Media.insertImage(getContentResolver(),
                                    mFile.getAbsolutePath(), fname, null);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        // 最后通知图库更新
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.parse("file://" + Uri.fromFile(mFile))));

                        return;
                    }
                    Log.e("E", "Bitmap为空!");
                    Toast.makeText(MainCameraActivity.this,
                            "Bitmap为空!",
                            Toast.LENGTH_LONG).show();
                } catch (Exception ioe) {
                    Log.e("E", "Fetal error: Open Photo File error");
                    Toast.makeText(MainCameraActivity.this, R.string.open_photo_file_err,
                            Toast.LENGTH_LONG).show();
                    ioe.printStackTrace();
                }
            } catch (IOException e) {
                Toast.makeText(MainCameraActivity.this, "写入照片出错!", Toast.LENGTH_LONG)
                        .show();
                Log.e("E", "打开文件路径 " + mFile + " 错误");
                e.printStackTrace();
            }
        }
    }
}
