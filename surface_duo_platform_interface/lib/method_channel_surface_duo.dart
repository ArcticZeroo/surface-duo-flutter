import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:surface_duo_platform_interface/event_channel_value_notifier.dart';
import 'package:surface_duo_platform_interface/surface_duo_platform_interface.dart';

/// An implementation of [SurfaceDuoPlatform] using method channels
class MethodChannelSurfaceDuo extends SurfaceDuoPlatform {
  final MethodChannel _methodChannel =
      MethodChannel('io.frozor.surfaceduo.methods');
  final EventChannelValueNotifier<int> _hingeAngleValueNotifier =
      EventChannelValueNotifier('io.frozor.surfaceduo.events.hinge', 0);
  final EventChannelValueNotifier<ScreenMode> _screenModeValueNotifier =
      EventChannelValueNotifier(
          'io.frozor.surfaceduo.events.screenmode', ScreenMode.singleScreen,
          transformEvent: (screenModeId) {
    switch (screenModeId) {
      case 0:
        return ScreenMode.singleScreen;
      case 1:
        return ScreenMode.dualScreen;
      case 2:
        return ScreenMode.unknown;
    }
  });

  @override
  Future<bool> isDeviceSurfaceDuo() {
    return _methodChannel.invokeMethod<bool>('isDeviceSurfaceDuo');
  }

  @override
  Future<bool> isAppDualScreen() {
    return _methodChannel.invokeMethod<bool>('isAppDualScreen');
  }

  @override
  Future<Rect> getHingeMask() async {
    var rectMap =
        await _methodChannel.invokeMapMethod<String, int>('getHingeMask');
    return Rect.fromLTRB(rectMap['left'] as double, rectMap['top'] as double,
        rectMap['right'] as double, rectMap['bottom'] as double);
  }

  @override
  Future<int> getHingeAngle() {
    return _methodChannel.invokeMethod<int>('getHingeAngle');
  }

  @override
  ValueNotifier<int> getHingeValueNotifier() {
    return _hingeAngleValueNotifier;
  }

  @override
  ValueNotifier<ScreenMode> getScreenModeValueNotifier() {
    return _screenModeValueNotifier;
  }
}
