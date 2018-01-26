package com.yyx.beautifylib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.muzhi.camerasdk.library.filter.GPUImageView;
import com.muzhi.camerasdk.library.filter.util.ImageFilterTools;
import com.yyx.beautifylib.R;
import com.yyx.beautifylib.sticker.DrawableSticker;
import com.yyx.beautifylib.sticker.Sticker;
import com.yyx.beautifylib.sticker.StickerView;
import com.yyx.beautifylib.sticker.TextSticker;
import com.yyx.beautifylib.tag.TagViewGroup;
import com.yyx.beautifylib.tag.model.TagGroupModel;
import com.yyx.beautifylib.tag.views.TagImageView;
import com.yyx.beautifylib.utils.BLBitmapUtils;
import com.yyx.beautifylib.utils.FilterUtils;

import org.tensorflow.demo.utils.StylizeUtil;

import java.util.List;

/**
 *
 * Created by Administrator on 2017/4/15.
 */
public class BLBeautifyImageView extends FrameLayout {
    private Context mContext;
    private StickerView mStickerView;
    private GPUImageView mGpuImageView;
    private TagImageView mTagGroupLayout;

    public BLBeautifyImageView(@NonNull Context context) {
        this(context, null);
    }

    public BLBeautifyImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BLBeautifyImageView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    private void init() {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.bl_beautify_image_view, this, true);
        mStickerView = (StickerView) rootView.findViewById(R.id.bl_sticker_view);
        mGpuImageView = (GPUImageView) rootView.findViewById(R.id.bl_gpu_image_view);
        mTagGroupLayout = (TagImageView) rootView.findViewById(R.id.bl_tag_image_view);

        initStickerView();
    }

    /**
     * TensorFlow Stylize
     * @param index 目标油画渲染器的index, 不做合法性检查
     * @param value 风格化的程度, 0.0f ~ 1.0f
     */
    public void stylize(final int index, final float value) {
        //Bitmap resized = Bitmap.createScaledBitmap(mGpuImageView.getCurrentBitMap(),
        //        mGpuImageView.getWidth(),
        //        mGpuImageView.getHeight(),
        //        false);
        // 获取这个图片的宽和高
        float width = mGpuImageView.getCurrentBitMap().getWidth();
        float height = mGpuImageView.getCurrentBitMap().getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = 1280.0f / width;
        float scaleHeight = 1280.0f / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(mGpuImageView.getCurrentBitMap(), 0, 0, (int) width,
                (int) height, matrix, true);
        //resized.setConfig(Bitmap.Config.ARGB_8888);
        StylizeUtil.stylizeImage(bitmap, mContext, index, value);
        matrix = new Matrix();
        matrix.postScale(2.5f,2.5f);  //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,
                0,0,bitmap.getWidth(), bitmap.getHeight(),matrix,true);
        mGpuImageView.setImage(resizeBmp);
    }

    /********************************** GPUImageView相关 *********************************/
    public void addFilter(ImageFilterTools.FilterType filterType) {
        FilterUtils.addFilter(mContext, filterType, mGpuImageView);
    }

    /**
     * 设置网络加载图片
     *
     * @param url
     */
    public void setImageUrl(String url) {
        Glide.with(mContext)
                .load(url)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        float width = (float) resource.getWidth();
                        float height = (float) resource.getHeight();
                        float ratio = width / height;
                        mGpuImageView.setRatio(ratio);
                        setImage(resource);
                    }
                });
    }

    /**
     * 设置本地路径图片
     *
     * @param path
     */
    public void setImage(String path) {
        mGpuImageView.setImage(path);
    }

    public void setImage(Bitmap bitmap) {
        float width = (float) bitmap.getWidth();
        float height = (float) bitmap.getHeight();
        float ratio = width / height;
        mGpuImageView.setRatio(ratio);
        mGpuImageView.setImage(bitmap);
    }

    public Bitmap getGPUBitmap() {
        return mGpuImageView.getCurrentBitMap();
    }

    public GPUImageView getGPUImageView() {
        return mGpuImageView;
    }


    public String save() {
        return getFilterImage();
    }

//    public void savePic(){
//        String folderName = BLCommonUtils.getApplicationName(mContext);
//        String fileName = System.currentTimeMillis() + ".jpg";
//        mGpuImageView.saveToPictures(folderName, fileName, new GPUImageView.OnPictureSavedListener() {
//            @Override
//            public void onPictureSaved(Uri uri) {
//                EventBus.getDefault().post(new SaveImageEvent(uri.getPath()));
//            }
//        });
//    }

    /**
     * 合并图片
     */
    public String getFilterImage() {
        mGpuImageView.setDrawingCacheEnabled(true);
        Bitmap editbmp = Bitmap.createBitmap(mGpuImageView.getDrawingCache());
        try {
            Bitmap fBitmap = mGpuImageView.capture();
            Bitmap bitmap = Bitmap.createBitmap(fBitmap.getWidth(), fBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas cv = new Canvas(bitmap);
            cv.drawBitmap(fBitmap, 0, 0, null);
            cv.drawBitmap(editbmp, 0, 0, null);

            //最终合并生成图片
            String path = BLBitmapUtils.saveAsBitmap(mContext, bitmap);
            bitmap.recycle();
            return path;

        } catch (Exception e) {
            return "";
        }
    }


    /**********************************StickerView相关*********************************/
    private void initStickerView() {
        mStickerView.configDefaultIcons();
        mStickerView.setLocked(false);
//        mStickerView.setConstrained(true);

        mStickerView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {
            @Override
            public void onStickerClicked(Sticker sticker) {

            }

            @Override
            public void onStickerDeleted(Sticker sticker) {

            }

            @Override
            public void onStickerDragFinished(Sticker sticker) {

            }

            @Override
            public void onStickerZoomFinished(Sticker sticker) {

            }

            @Override
            public void onStickerFlipped(Sticker sticker) {

            }

            @Override
            public void onStickerDoubleTapped(Sticker sticker) {

            }
        });
    }

    /**
     * 添加图片贴图
     * @param drawableId
     */
    public void addSticker(int drawableId) {
        if (drawableId <= 0)
            return;
        Drawable drawable = ContextCompat.getDrawable(mContext, drawableId);
        mStickerView.addSticker(new DrawableSticker(drawable));
    }

    /**
     * 添加文字贴图
     * @param text
     * @param color
     */
    public void addTextSticker(String text, int color){
        TextSticker sticker = new TextSticker(mContext);
        sticker.setText(text);
        sticker.setTextColor(color);
        sticker.setTextAlign(Layout.Alignment.ALIGN_CENTER);
        sticker.resizeText();

        mStickerView.addSticker(sticker);
    }

    /**
     * 添加文字贴图
     * @param text
     */
    public void addTextSticker(String text){
        addTextSticker(text, Color.WHITE);
    }

    public void stickerLocked(boolean lock) {
        mStickerView.setLocked(lock);
    }

    /**********************************TagImageView相关*********************************/
    public void addTagGroup(TagGroupModel model, TagViewGroup.OnTagGroupClickListener listener, boolean editMode) {
        mTagGroupLayout.setEditMode(editMode);
        mTagGroupLayout.addTagGroup(model, listener);
    }

    public void removeTagGroup(TagViewGroup tagViewGroup) {
        mTagGroupLayout.removeTagGroup(tagViewGroup);
    }

    public List<TagGroupModel> getTagGroupModelList() {
        return mTagGroupLayout.getTagGroupModelList();
    }

    public TagGroupModel getTagGroupModel(TagViewGroup group) {
        return mTagGroupLayout.getTagGroupModel(group);
    }

    public void setTagModelList(List<TagGroupModel> tagGroupList) {
        mTagGroupLayout.setTagList(tagGroupList);
    }

    public TagImageView getTagImageView() {
        return mTagGroupLayout;
    }

}
