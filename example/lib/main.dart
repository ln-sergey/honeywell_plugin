import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:honeywell_plugin/honeywell_plugin.dart';
import 'package:honeywell_plugin/scanner_callback.dart';
import 'package:honeywell_plugin/code_format.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> with WidgetsBindingObserver implements ScannerCallBack {
  HoneywellPlugin honeywellScanner = HoneywellPlugin();
  String scannedCode = 'Empty';
  bool scannerEnabled = false;
  bool scannerAvailable = false;
  bool scan1DFormats = true;
  bool scan2DFormats = true;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    honeywellScanner.scannerCallBack = this;
    updateScanProperties();
    Timer.periodic(Duration(microseconds: 300), (timer) async {
      scannerAvailable = await honeywellScanner.isAvailable();
    });
  }

  updateScanProperties() {
    List<CodeFormat> codeFormats = [];
    if (scan1DFormats ?? false)
      codeFormats.addAll(CodeFormatUtils.ALL_1D_FORMATS);
    if (scan2DFormats ?? false)
      codeFormats.addAll(CodeFormatUtils.ALL_2D_FORMATS);
    honeywellScanner.setProperties(
        CodeFormatUtils.getAsPropertiesComplement(codeFormats));
  }

  @override
  void onDecoded(String result) {
    setState(() {
      scannedCode = result;
    });
  }

  @override
  void onError(Exception error) {
    setState(() {
      scannedCode = error.toString();
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Honeywell scanner example'),
        ),
        body: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text('Scanner: ${scannerEnabled ? "Started" : "Stopped"}',
              style: TextStyle(color: scannerEnabled ? Colors.blue : Colors.red),),
            Divider(
              color: Colors.transparent,
            ),
            Text('Scanner is ${scannerAvailable ? "Available" : "Unavailable"}',
              style: TextStyle(color: scannerEnabled ? Colors.blue : Colors.red),),
            Divider(
              color: Colors.transparent,
            ),
            Text('Scanned code: $scannedCode'),
            Divider(
              color: Colors.transparent,
            ),
            SwitchListTile(
              title: Text("Scan 1D Codes"),
              subtitle: Text("like Code-128, Code-39, Code-93, etc"),
              value: scan1DFormats,
              onChanged: (value) {
                scan1DFormats = value;
                updateScanProperties();
                setState(() {});
              },
            ),
            SwitchListTile(
              title: Text("Scan 2D Codes"),
              subtitle: Text("like QR, Data Matrix, Aztec, etc"),
              value: scan2DFormats,
              onChanged: (value) {
                scan2DFormats = value;
                updateScanProperties();
                setState(() {});
              },
            ),
            RaisedButton(
              child: Text("Start Scanner"),
              onPressed: () {
                honeywellScanner.startScanner();
                scannerEnabled = true;
                setState(() {});
              },
            ),
            Divider(
              color: Colors.transparent,
            ),
            RaisedButton(
              child: Text("Stop Scanner"),
              onPressed: () {
                honeywellScanner.stopScanner();
                scannerEnabled = false;
                setState(() {});
              },
            ),
            Divider(
              color: Colors.transparent,
            ),
            GestureDetector(
              onTapDown: (tapDownDetails) {
                honeywellScanner.startScanning();
              },
              onTapUp: (tapUpDetails) {
                honeywellScanner.stopScanning();
              },
              child: Text('SCANN', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.black, fontSize: 25),))
          ],
        ),
      ),
    );
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    super.didChangeAppLifecycleState(state);
    if(state == null) return;
    switch(state){
      case AppLifecycleState.resumed:
        if(honeywellScanner != null) honeywellScanner.resumeScanner();
        break;
      case AppLifecycleState.inactive:
        if(honeywellScanner != null) honeywellScanner.pauseScanner();
        break;
      case AppLifecycleState.paused://AppLifecycleState.paused is used as stopped state because deactivate() works more as a pause for lifecycle
        if(honeywellScanner != null) honeywellScanner.pauseScanner();
        break;
      case AppLifecycleState.detached:
        if(honeywellScanner != null) honeywellScanner.pauseScanner();
        break;
      default:
        break;
    }
  }

  @override
  void dispose() {
    if(honeywellScanner != null) honeywellScanner.stopScanner();
    super.dispose();
  }
}
