package com.vmuzey.honeywell_plugin

import android.content.Context
import com.honeywell.aidc.*
import com.honeywell.aidc.AidcManager.CreatedCallback
import com.honeywell.aidc.BarcodeReader.BarcodeListener


class HoneywellScannerNative(private val context: Context) : HoneywellScanner(context),
    CreatedCallback,
    BarcodeListener {
    private var initialazing = false
    private var pendingResume = false

    private lateinit var scannerManager: AidcManager

    private lateinit var scanner: BarcodeReader

    private lateinit var properties: MutableMap<String, Any>

    init {
        initialazing = true
        AidcManager.create(context, this)
    }

    override fun onCreated(aidcManager: AidcManager) {
        try {
            scannerManager = aidcManager
            scanner = scannerManager.createBarcodeReader()
            scanner.addBarcodeListener(this)
            try {
                scanner.setProperty(
                    BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                    BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL
                )
                scanner.setProperty(BarcodeReader.PROPERTY_DATA_PROCESSOR_LAUNCH_BROWSER, false)
            } catch (e: UnsupportedPropertyException) {
                onError(e)
            }
            initProperties()
            scanner.setProperties(properties)
            isAvailable = true
            initialazing = false
            startScanner()
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
//        Do nothing with unrecognized code due to an incomplete scanning
//        if(barcodeFailureEvent != null) onError(new Exception(barcodeFailureEvent.toString()));
    }

    private fun initProperties() {
        properties = mutableMapOf()
        properties[BarcodeReader.PROPERTY_AZTEC_ENABLED] = false
        properties[BarcodeReader.PROPERTY_CODABAR_ENABLED] = false
        properties[BarcodeReader.PROPERTY_CODE_39_ENABLED] = false
        properties[BarcodeReader.PROPERTY_CODE_93_ENABLED] = false
        properties[BarcodeReader.PROPERTY_CODE_128_ENABLED] = false
        properties[BarcodeReader.PROPERTY_DATAMATRIX_ENABLED] = false
        properties[BarcodeReader.PROPERTY_EAN_8_ENABLED] = false
        properties[BarcodeReader.PROPERTY_EAN_13_ENABLED] = false
        properties[BarcodeReader.PROPERTY_MAXICODE_ENABLED] = false
        properties[BarcodeReader.PROPERTY_PDF_417_ENABLED] = false
        // QR Code is the default code type for Honeywell Barcode Reader
        properties[BarcodeReader.PROPERTY_QR_CODE_ENABLED] = true

        properties[BarcodeReader.PROPERTY_RSS_ENABLED] = false
        properties[BarcodeReader.PROPERTY_RSS_EXPANDED_ENABLED] = false
        properties[BarcodeReader.PROPERTY_UPC_A_ENABLE] = false
        properties[BarcodeReader.PROPERTY_UPC_E_ENABLED] = false
    }

    override fun setProperties(mapProperties: Map<String, Any>?) {
        if (mapProperties == null) return
        scanner.setProperties(properties)
    }

    private fun scan(state: Boolean) {
        try {
            scanner.aim(state)
            scanner.light(state)
            scanner.decode(state)
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
        scanner.release()
        pendingResume = true
        return true
    }

    override fun startScanner(): Boolean {
        try {
            scanner.claim()
        } catch (e: IllegalStateException) {
            try {
                scanner = scannerManager.createBarcodeReader()
                scanner.addBarcodeListener(this)
                scanner.claim()
            } catch (e1: Exception) {
                AidcManager.create(context, this)
            }
        }
        return true
    }

    override fun stopScanner(): Boolean {
        pendingResume = false
        try {
            scanner.removeBarcodeListener(this)
            scanner.release()
            scanner.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            scannerManager.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }
}