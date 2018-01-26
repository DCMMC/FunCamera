package org.tensorflow.demo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * TensorFlow Stylize 工具
 * Created by kevin on 1/26/18.
 */
public class StylizeUtil {
    private static final String INPUT_NODE = "input";
    private static final String STYLE_NODE = "style_num";
    private static final String MODEL_FILE = "file:///android_asset/stylize_quantized.pb";
    private static final String OUTPUT_NODE = "transformer/expand/conv3/conv/Sigmoid";
    private static final int NUM_STYLES = 26;

    /**
     * 注意: bitmap 不能太大, 否则 OOM
     */
    public static void stylizeImage(final Bitmap bitmap, final Context context, final int position,
                             final float value) {
        // validate
        if (position < 0 || position >= NUM_STYLES || value < 0.0f || value > 1.0f) {
            Log.e("E", "stylizeImage 的参数 position 或 value 错误!");
            return;
        }
        int[] intValues = new int[bitmap.getWidth() * bitmap.getHeight()];
        float[] floatValues = new float[bitmap.getWidth() * bitmap.getHeight() * 3];
        float[] styleVals = new float[NUM_STYLES];
        TensorFlowInferenceInterface inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);

        // 设置风格
        styleVals[position] = value;

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF) / 255.0f;
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;
            floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f;
        }

        // Copy the input data into TensorFlow.
        inferenceInterface.feed(
                INPUT_NODE, floatValues, 1, bitmap.getWidth(),
                bitmap.getHeight(), 3);
        inferenceInterface.feed(STYLE_NODE, styleVals, NUM_STYLES);

        inferenceInterface.run(new String[] {OUTPUT_NODE}, false);
        //floatValues = new float[(bitmap.getWidth() % 2 == 0 ?bitmap.getWidth() :
        //        bitmap.getWidth() + 3) *
        //        (bitmap.getHeight() % 2 == 0 ? bitmap.getHeight() : bitmap.getHeight() + 3)
        //        * 3];
        inferenceInterface.fetch(OUTPUT_NODE, floatValues);

        for (int i = 0; i < intValues.length; ++i) {
            intValues[i] =
                    0xFF000000
                            | (((int) (floatValues[i * 3] * 255)) << 16)
                            | (((int) (floatValues[i * 3 + 1] * 255)) << 8)
                            | ((int) (floatValues[i * 3 + 2] * 255));
        }

        bitmap.setPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    }
}
