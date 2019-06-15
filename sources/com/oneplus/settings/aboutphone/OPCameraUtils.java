package com.oneplus.settings.aboutphone;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.support.v17.leanback.media.MediaPlayerGlue;
import java.util.List;

public class OPCameraUtils {
    public static final int CAMERA_FACING_BACK = 0;
    public static final int CAMERA_FACING_FRONT = 1;
    public static final int CAMERA_NONE = 2;

    public static int HasBackCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == 0) {
                return i;
            }
        }
        return 2;
    }

    public static int HasFrontCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == 1) {
                return i;
            }
        }
        return 2;
    }

    public static String getCameraPixels(int paramInt) {
        if (paramInt == 2) {
            return "none";
        }
        Camera localCamera = Camera.open(paramInt);
        Parameters localParameters = localCamera.getParameters();
        localParameters.set("camera-id", 1);
        List<Size> localList = localParameters.getSupportedPictureSizes();
        if (localList == null) {
            return "none";
        }
        int i;
        int[] heights = new int[localList.size()];
        int[] widths = new int[localList.size()];
        for (i = 0; i < localList.size(); i++) {
            Size size = (Size) localList.get(i);
            int sizehieght = size.height;
            int sizewidth = size.width;
            heights[i] = sizehieght;
            widths[i] = sizewidth;
        }
        i = getMaxNumber(heights) * getMaxNumber(widths);
        localCamera.release();
        return String.valueOf(i / MediaPlayerGlue.FAST_FORWARD_REWIND_STEP);
    }

    public static int getMaxNumber(int[] paramArray) {
        int i = 0;
        int temp = paramArray[0];
        while (i < paramArray.length) {
            if (temp < paramArray[i]) {
                temp = paramArray[i];
            }
            i++;
        }
        return temp;
    }
}
