package com.vmuzey.honeywell_plugin

import android.content.Context

abstract class HoneywellScanner(context: Context) : ScannerCallBack {
    var initialized = false
    protected val context: Context
    private var scannerCallBack: ScannerCallBack? = null
    fun setScannerCallBack(scannerCallBack: ScannerCallBack?) {
        this.scannerCallBack = scannerCallBack
    }

    /**
     * Called when decoder has successfully decoded the code
     * <br></br>
     * Note that this method always called on a worker thread
     *
     * @param code Encapsulates the result of decoding a barcode within an image
     */
    override fun onDecoded(code: String?) {
        if (scannerCallBack != null) scannerCallBack!!.onDecoded(code)
    }

    /**
     * Called when error has occurred
     * <br></br>
     * Note that this method always called on a worker thread
     *
     * @param error Exception that has been thrown
     */
    override fun onError(error: Exception?) {
        if (scannerCallBack != null) scannerCallBack!!.onError(error)
    }

    abstract fun setProperties(mapProperties: Map<String, Any>?)
    abstract fun startScanner(): Boolean
    abstract fun resumeScanner(): Boolean
    abstract fun startScanning(): Boolean
    abstract fun stopScanning(): Boolean
    abstract fun pauseScanner(): Boolean
    abstract fun stopScanner(): Boolean

    init {
        this.context = context
    }
}