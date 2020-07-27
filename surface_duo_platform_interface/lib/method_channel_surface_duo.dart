import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:surface_duo_platform_interface/surface_duo_platform_interface.dart';

/// An implementation of [SurfaceDuoPlatform] using method channels
class MethodChannelSurfaceDuo extends SurfaceDuoPlatform {
  final MethodChannel _methodChannel =
      MethodChannel('io.frozor.surfaceduo.methods');
  final EventChannel _hingeAngleEventChannel =
      EventChannel('io.frozor.surfaceduo.events.hinge');
  ValueNotifier<int> _hingeAngle;

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

  @override
  Future<ValueNotifier<int>> getHingeValueNotifier() async {
    if (_hingeAngle != null) {
      return _hingeAngle;
    }

    // TODO: Cancel listening when possible to avoid extra memory usage
    _hingeAngle = ValueNotifier<int>(0);
    _hingeAngleEventChannel.receiveBroadcastStream().listen((angle) {
      _hingeAngle.value = angle;
    });

    return _hingeAngle;
  }
}
