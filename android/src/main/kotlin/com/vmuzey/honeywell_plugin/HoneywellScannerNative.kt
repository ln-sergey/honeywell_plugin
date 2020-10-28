package com.vmuzey.honeywell_plugin

import android.content.Context
import android.util.Log
import com.honeywell.aidc.*
import com.honeywell.aidc.AidcManager.CreatedCallback
import com.honeywell.aidc.BarcodeReader.BarcodeListener


class HoneywellScannerNative(context: Context) : HoneywellScanner(context), CreatedCallback, BarcodeListener {
    private var initialized = false
    private var initializing = false
    private var pendingResume = false

    @Transient
    private var scannerManager: AidcManager? = null

    @Transient
    private var scanner: BarcodeReader? = null

    @Transient
    private var properties: MutableMap<String, Any>? = null
    private fun init() {
        initializing = true
        AidcManager.create(context, this)
    }

    override fun onCreated(aidcManager: AidcManager) {
        try {
            scannerManager = aidcManager
            scanner = scannerManager!!.createBarcodeReader()
            // register bar code event listener
            scanner?.addBarcodeListener(this)

            // set the trigger mode to client control
            try {
                scanner?.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL)
                scanner?.setProperty(BarcodeReader.PROPERTY_DATA_PROCESSOR_LAUNCH_BROWSER, false)
            } catch (e: UnsupportedPropertyException) {
                onError(e)
            }
            // register trigger state change listener
            // When using Automatic Trigger control do not need to implement the onTriggerEvent
            // function scanner.addTriggerListener(this);
            if (properties != null) scanner?.setProperties(properties)
            initialized = true
            initializing = false
            if (pendingResume) resumeScanner()
        } catch (e: InvalidScannerNameException) {
            onError(e)
        } catch (e: Exception) {
            onError(e)
        }
    }

    override fun onBarcodeEvent(barcodeReadEvent: BarcodeReadEvent?) {
        if (barcodeReadEvent != null) onDecoded(barcodeReadEvent.barcodeData)
    }

    override fun onFailureEvent(barcodeFailureEvent: BarcodeFailureEvent) {
        //Do nothing with unrecognized code due to an incomplete scanning
//        if(barcodeFailureEvent != null) onError(new Exception(barcodeFailureEvent.toString()));
    }

    private fun initProperties() {
        properties = mutableMapOf()
        properties!![BarcodeReader.PROPERTY_AZTEC_ENABLED] = true
        properties!![BarcodeReader.PROPERTY_CODABAR_ENABLED] = true
        properties!![BarcodeReader.PROPERTY_CODE_39_ENABLED] = true
        properties!![BarcodeReader.PROPERTY_CODE_93_ENABLED] = true
        properties!![BarcodeReader.PROPERTY_CODE_128_ENABLED] = true
        properties!![BarcodeReader.PROPERTY_DATAMATRIX_ENABLED] = true
        properties!![BarcodeReader.PROPERTY_EAN_8_ENABLED] = true
        properties!![BarcodeReader.PROPERTY_EAN_13_ENABLED] = true
        properties!![BarcodeReader.PROPERTY_MAXICODE_ENABLED] = true
        properties!![BarcodeReader.PROPERTY_PDF_417_ENABLED] = true
        properties!![BarcodeReader.PROPERTY_QR_CODE_ENABLED] = true
        properties!![BarcodeReader.PROPERTY_RSS_ENABLED] = true
        properties!![BarcodeReader.PROPERTY_RSS_EXPANDED_ENABLED] = true
        properties!![BarcodeReader.PROPERTY_UPC_A_ENABLE] = true
        properties!![BarcodeReader.PROPERTY_UPC_E_ENABLED] = true
    }

    override fun setProperties(mapProperties: Map<String, Any>?) {
        if (mapProperties == null) return
        initProperties()
        properties!!.putAll(mapProperties)
        if (scanner != null) scanner!!.setProperties(properties)
    }

    private fun scan(state: Boolean) {
        try {
            scanner!!.aim(state)
            scanner!!.light(state)
            scanner!!.decode(state)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun resumeScanner(): Boolean {
        startScanner()
        return true
    }

    override fun startScanning(): Boolean {
        scan(true)
        return true
    }

    override fun stopScanning(): Boolean {
        scan(false)
        return true
    }

    override fun pauseScanner(): Boolean {
        if (scanner != null) {
            // release the scanner claim so we don't get any scanner notifications while paused
            // and the scanner properties are restored to default.
            scanner!!.release()
        }
        pendingResume = false
        return true
    }

    override fun startScanner(): Boolean {
        if (scanner != null) {
            try {
                scanner!!.claim()
            } catch (e: Exception) {
                onError(e)
                return false
            }
        } else {
            pendingResume = true
            if (!initialized && !initializing) init()
        }
        return true
    }

    override fun stopScanner(): Boolean {
        pendingResume = false
        try {
            if (scanner != null) {
                // unregister barcode event listener
                scanner!!.removeBarcodeListener(this)

                // unregister trigger state change listener
                // When using Automatic Trigger control do not need to implement the onTriggerEvent
                // function scanner.removeTriggerListener(this);

                // release the scanner claim so we don't get any scanner notifications while paused
                // and the scanner properties are restored to default.
                scanner!!.release()

                // close BarcodeReader to clean up resources.
                scanner!!.close()
                scanner = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            if (scannerManager != null) {
                // close AidcManager to disconnect from the scanner service.
                // once closed, the object can no longer be used.
                scannerManager!!.close()
                scannerManager = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        initialized = false
        return true
    }

    init {
        init()
    }
}