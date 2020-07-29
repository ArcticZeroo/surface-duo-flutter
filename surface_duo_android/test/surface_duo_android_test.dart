import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:surface_duo_android/surface_duo_android.dart';

void main() {
  const MethodChannel channel = MethodChannel('surface_duo_android');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await SurfaceDuoAndroid.platformVersion, '42');
  });
}
