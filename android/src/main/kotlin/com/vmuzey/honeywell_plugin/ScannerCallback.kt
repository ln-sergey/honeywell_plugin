package com.vmuzey.honeywell_plugin

import android.app.Activity

interface ScannerCallBack {
    /**
     * Called when decoder has successfully decoded the code
     * <br></br>
     * Note that this method always called on a worker thread
     *
     * @param code Encapsulates the result of decoding a barcode within an image
     * @see Handler
     *
     * @see Activity.runOnUiThread
     */
    fun onDecoded(code: String?)

    /**
     * Called when error has occurred
     * <br></br>
     * Note that this method always called on a worker thread
     *
     * @param error Exception that has been thrown
     * @see Handler
     *
     * @see Activity.runOnUiThread
     */
    fun onError(error: Exception?)
}