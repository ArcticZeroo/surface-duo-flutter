import 'dart:async';

import 'package:flutter/widgets.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:surface_duo_platform_interface/method_channel_surface_duo.dart';

abstract class SurfaceDuoPlatform extends PlatformInterface {
  /// A static token to verify that this platform interface is being extended
  /// rather than implemented.
  static final Object _token = new Object();

  static SurfaceDuoPlatform _instance = MethodChannelSurfaceDuo();

  /// The default instance of [SurfaceDuoPlatform] to use.
  ///
  /// Defaults to [MethodChannelSurfaceDuo].
  static SurfaceDuoPlatform get instance => _instance;

  /// Platform-specific plugins should set this with their own platform-specific
  /// class that extends [SurfaceDuoPlatform] when they register themselves.
  static set instance(SurfaceDuoPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  SurfaceDuoPlatform() : super(token: _token);

  /// Returns `true` if this device is a surface duo
  Future<bool> isDeviceSurfaceDuo() {
    throw UnimplementedError('isDeviceSurfaceDuo is not implemented');
  }

  /// Returns `true` if the device is a surface duo, and this app is spanned
  /// across both screens of the device.
  Future<bool> isAppSpanned() {
    throw UnimplementedError('isAppSpanned is not implemented');
  }

  Future<Rect> getHingeMask() {
    throw UnimplementedError('getHingeMask is not implemented');
  }

  /// Returns the current hinge angle as a double
  Future<int> getHingeAngle() {
    throw UnimplementedError('getHingeAngle is not implemented');
  }

  /// Returns a change notifier that updates each time the hinge
  /// angle changes.
  ValueNotifier<int> getHingeValueNotifier() {
    throw UnimplementedError('getHingeChangeNotifier is not implemented');
  }
}
