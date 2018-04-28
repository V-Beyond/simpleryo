package com.simpleryo.leyotang.aliyun;

/**
 * Created by jingdan on 2017/8/31.
 */

public interface ProgressCallback<Request, Result> extends Callback<Request, Result> {
    void onProgress(Request request, long currentSize, long totalSize);
}
