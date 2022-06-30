package com.vmuzey.honeywell_plugin

import android.content.Context
import android.os.Handler
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar


/** HoneywellPlugin */
class HoneywellPlugin(): FlutterPlugin, MethodCallHandler, ScannerCallBack {
  val METHOD_CHANNEL = "honeywell_plugin"
  val GET_PLATFORM_VERSION = "getPlatformVersion"
  val SET_PROPERTIES = "setProperties"
  val IS_AVAILABLE = "isAvailable"
  val START_SCANNING = "startScanning"
  val STOP_SCANNING = "stopScanning"
  val START_SCANNER = "startScanner"
  val RESUME_SCANNER = "resumeScanner"
  val PAUSE_SCANNER = "pauseScanner"
  val STOP_SCANNER = "stopScanner"
  val ON_DECODED = "onDecoded"
  val ON_ERROR = "onError"

  private lateinit var channel : MethodChannel
  private var handler: Handler = Handler()
  private var scanner: HoneywellScanner? = null
  private var scannerAvailable = false

  constructor(context: Context) : this() {
    try {
      HoneywellScannerNative(context).also { scanner = it }.setScannerCallBack(this)
      scannerAvailable = true
    } catch (e: Exception) {
      scannerAvailable = false
    }
  }

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, METHOD_CHANNEL)
    channel.setMethodCallHandler(this);
    try {
      HoneywellScannerNative(flutterPluginBinding.applicationContext).also { scanner = it }.setScannerCallBack(this)
      scannerAvailable = true
    } catch (e: Exception) {
      scannerAvailable = false
    }
  }

  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      HoneywellPlugin(registrar.context()).apply {
        channel = MethodChannel(registrar.messenger(), METHOD_CHANNEL)
        channel.setMethodCallHandler(this)
      }
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    try {
      when (call.method) {
        GET_PLATFORM_VERSION -> {
          result.success("Android ${android.os.Build.VERSION.RELEASE}")
        }
        SET_PROPERTIES -> {
          scanner?.setProperties(call.arguments as Map<String, Any>)
          result.success(true)
        }
        START_SCANNING -> {
          scanner?.startScanning()
          result.success(true)
        }
        IS_AVAILABLE -> {
          result.success(scanner?.initialized ?: false)
        }
        STOP_SCANNING -> {
          scanner?.stopScanning()
          result.success(true)
        }
        START_SCANNER -> {
          scanner?.startScanner()
          result.success(true)
        }
        RESUME_SCANNER -> {
          scanner?.resumeScanner()
          result.success(true)
        }
        PAUSE_SCANNER -> {
          scanner?.pauseScanner()
          result.success(true)
        }
        STOP_SCANNER -> {
          scanner?.stopScanner()
          result.success(true)
        }
        else -> result.notImplemented()
      }
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
      result.error(e.message.toString(), null, null)
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onDecoded(code: String?) {
    handler.post { channel.invokeMethod(ON_DECODED, code) }
  }

  override fun onError(error: Exception?) {
    handler.post { channel.invokeMethod(ON_ERROR, error?.message) }
  }
}
