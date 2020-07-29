import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:surface_duo_platform_interface/hinge_value_notifier.dart';
import 'package:surface_duo_platform_interface/surface_duo_platform_interface.dart';

/// An implementation of [SurfaceDuoPlatform] using method channels
class MethodChannelSurfaceDuo extends SurfaceDuoPlatform {
  final MethodChannel _methodChannel =
      MethodChannel('io.frozor.surfaceduo.methods');
  final EventChannel _hingeAngleEventChannel =
      EventChannel('io.frozor.surfaceduo.events.hinge');
  HingeValueNotifier<int> _hingeAngle;
  StreamSubscription _hingeAngleSubscription;

  @override
  Future<bool> isDeviceSurfaceDuo() {
    return _methodChannel.invokeMethod<bool>('isDeviceSurfaceDuo');
  }

  @override
  Future<bool> isAppSpanned() {
    return _methodChannel.invokeMethod<bool>('isAppSpanned');
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

  void addHingeListener() {
    _hingeAngleSubscription =
        _hingeAngleEventChannel.receiveBroadcastStream().listen((angle) {
      _hingeAngle.value = angle;
    });
  }

  void removeHingeListener() {
    _hingeAngleSubscription.cancel();
  }

  @override
  ValueNotifier<int> getHingeValueNotifier() {
    if (_hingeAngle == null) {
      _hingeAngle = HingeValueNotifier<int>(0,
          onFirstListenerAdded: addHingeListener,
          onLastListenerRemoved: removeHingeListener);
    }
    return _hingeAngle;
  }
}
