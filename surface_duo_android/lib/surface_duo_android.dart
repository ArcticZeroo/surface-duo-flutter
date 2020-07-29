import 'dart:async';

import 'package:flutter/services.dart';

class SurfaceDuoAndroid {
  static const MethodChannel _channel =
      const MethodChannel('surface_duo_android');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
