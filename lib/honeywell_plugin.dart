// ignore_for_file: constant_identifier_names

import 'dart:async';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'scanner_callback.dart';

class HoneywellPlugin {
  static const _SET_PROPERTIES = "setProperties";
  static const _IS_AVAILABLE = "isAvailable";
  static const _START_SCANNING = "startScanning";
  static const _STOP_SCANNING = "stopScanning";
  static const _START_SCANNER = "startScanner";
  static const _RESUME_SCANNER = "resumeScanner";
  static const _PAUSE_SCANNER = "pauseScanner";
  static const _STOP_SCANNER = "stopScanner";
  static const _ON_DECODED = "onDecoded";
  static const _ON_ERROR = "onError";

  static const MethodChannel _channel = MethodChannel('honeywell_plugin');
  ScannerCallBack _scannerCallBack;

  HoneywellPlugin({required ScannerCallBack scannerCallBack})
      : _scannerCallBack = scannerCallBack {
    _channel.setMethodCallHandler(_onMethodCall);
  }

  set scannerCallBack(ScannerCallBack scannerCallBack) =>
      _scannerCallBack = scannerCallBack;

  Future _onMethodCall(MethodCall call) {
    try {
      switch (call.method) {
        case _ON_DECODED:
          onDecoded(call.arguments);
          break;
        case _ON_ERROR:
          onError(Exception(call.arguments));
          break;
        default:
          debugPrint("${call.arguments}");
      }
    } catch (e) {
      debugPrint("$e");
    }
    return Future.value(null);
  }

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  ///Called when decoder has successfully decoded the code
  ///<br>
  ///Note that this method always called on a worker thread
  ///
  ///@param code Encapsulates the result of decoding a barcode within an image
  void onDecoded(String? code) {
    _scannerCallBack.onDecoded(code);
  }

  ///Called when error has occurred
  ///<br>
  ///Note that this method always called on a worker thread
  ///
  ///@param error Exception that has been thrown
  void onError(Exception error) {
    _scannerCallBack.onError(error);
  }

  /// Set Honeywell scan properties
  Future setProperties(Map<String?, dynamic> mapProperties) {
    return _channel.invokeMethod(_SET_PROPERTIES, mapProperties);
  }

  /// This method starts scanning for a qr/barcode.
  ///
  /// This method will start the qr/barcode scanning in the same way
  /// as pressing the hardware scan button on the device, the scanner
  /// will search for a barcode until either it finds one, time-outs
  /// or the [stopScanning] method is called.
  Future startScanning() {
    return _channel.invokeMethod(_START_SCANNING);
  }

  /// This method stops active scanning.
  ///
  /// This method can be used to stop the scanner after
  /// [startScanning] was called. If the device is not
  /// currently scanning this will have no effect.
  Future stopScanning() {
    return _channel.invokeMethod(_STOP_SCANNING);
  }

  /// This method returns bool value
  ///
  /// If returned value is true it means
  /// that you can use honeywell scann hardware
  ///
  /// If returned value is false it means
  /// that you can't use honeywell scann hardware
  ///
  /// Firsly, you must call [startScanner]
  Future<bool?> isAvailable() async {
    if (kIsWeb || !Platform.isAndroid) return false;
    return _channel.invokeMethod(_IS_AVAILABLE);
  }

  /// This method is used to turn on the decoder hardware in the device.
  ///
  /// It will retry to turn on the decoder hardware every 250ms until
  /// success or timeout. This must be called before any other decoder
  /// methods. Until this method is called the decoder hardware is not
  /// initialized. Use [stopScanner] to shut down the decoder
  /// hardware.
  Future startScanner() {
    return _channel.invokeMethod(_START_SCANNER);
  }

  /// This method is used to turn off the decoder hardware in the device.
  ///
  /// Once this method is called the decoder hardware is deactivated.
  /// No scanning methods can be called until [startScanner]
  /// is called again to re-initialize the decoder hardware.
  Future stopScanner() {
    return _channel.invokeMethod(_STOP_SCANNER);
  }

  Future resumeScaner() {
    return _channel.invokeMethod(_RESUME_SCANNER);
  }

  Future pauseScaner() {
    return _channel.invokeMethod(_PAUSE_SCANNER);
  }
}
