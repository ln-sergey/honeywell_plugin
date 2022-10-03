import 'dart:async';

import 'package:flutter/material.dart';

import 'package:honeywell_plugin/honeywell_plugin.dart';
import 'package:honeywell_plugin/scanner_callback.dart';
import 'package:honeywell_plugin/code_format.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp>
    with WidgetsBindingObserver
    implements ScannerCallBack {
  HoneywellPlugin? honeywellScanner;
  String? scannedCode = 'Empty';
  bool scannerEnabled = false;
  bool scannerPaused = false;
  bool? scannerAvailable = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    honeywellScanner = HoneywellPlugin(scannerCallBack: this);
    Timer.periodic(const Duration(microseconds: 300), (timer) async {
      scannerAvailable = await honeywellScanner!.isAvailable();
      setState(() {});
    });
  }

  @override
  void onDecoded(String? result) {
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
            Text(
              'Scanner: ${scannerEnabled ? "Started" : "Stopped"}',
              style:
                  TextStyle(color: scannerEnabled ? Colors.blue : Colors.red),
            ),
            const Divider(
              color: Colors.transparent,
            ),
            Text(
              'Scanner is ${scannerAvailable ?? false ? "Available" : "Unavailable"}',
              style: TextStyle(
                  color: scannerAvailable ?? false ? Colors.blue : Colors.red),
            ),
            const Divider(
              color: Colors.transparent,
            ),
            Text('Scanned code: $scannedCode'),
            const Divider(
              color: Colors.transparent,
            ),
            SwitchListTile(
              title: Text("Pause scanner"),
              value: scannerPaused,
              onChanged: (value) async {
                if (value) {
                  await honeywellScanner!.pauseScaner();
                } else {
                  await honeywellScanner!.resumeScaner();
                }
                setState(() {
                  scannerPaused = value;
                });
              },
            ),
            ElevatedButton(
              child: const Text("Start Scanner"),
              onPressed: () async {
                await honeywellScanner!.startScanner();
                scannerEnabled = true;
                setState(() {});
              },
            ),
            const Divider(
              color: Colors.transparent,
            ),
            ElevatedButton(
              child: const Text("Stop Scanner"),
              onPressed: () async {
                await honeywellScanner!.stopScanner();
                scannerEnabled = false;
                setState(() {});
              },
            ),
            const Divider(
              color: Colors.transparent,
            ),
            GestureDetector(
                onTapDown: (_) async {
                  await honeywellScanner!.startScanning();
                },
                onTapUp: (_) async {
                  await honeywellScanner!.stopScanning();
                },
                child: const Text(
                  'SCANN',
                  style: TextStyle(
                      fontWeight: FontWeight.bold,
                      color: Colors.black,
                      fontSize: 25),
                ))
          ],
        ),
      ),
    );
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    super.didChangeAppLifecycleState(state);
    switch (state) {
      case AppLifecycleState.resumed:
        if (honeywellScanner != null) honeywellScanner!.resumeScaner();
        break;
      case AppLifecycleState.inactive:
        if (honeywellScanner != null) honeywellScanner!.pauseScaner();
        break;
      case AppLifecycleState
          .paused: //AppLifecycleState.paused is used as stopped state because deactivate() works more as a pause for lifecycle
        if (honeywellScanner != null) honeywellScanner!.pauseScaner();
        break;
      case AppLifecycleState.detached:
        if (honeywellScanner != null) honeywellScanner!.pauseScaner();
        break;
      default:
        break;
    }
  }

  @override
  void dispose() {
    if (honeywellScanner != null) honeywellScanner!.stopScanner();
    super.dispose();
  }
}
