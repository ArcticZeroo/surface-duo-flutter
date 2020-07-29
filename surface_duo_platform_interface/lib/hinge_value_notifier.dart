import 'package:flutter/foundation.dart';

class HingeValueNotifier<T> extends ValueNotifier<T> {
  int _listenerCount = 0;
  final VoidCallback _onFirstListenerAdded;
  final VoidCallback _onLastListenerRemoved;

  HingeValueNotifier(T value,
      {@required VoidCallback onFirstListenerAdded,
      @required VoidCallback onLastListenerRemoved})
      : _onFirstListenerAdded = onFirstListenerAdded,
        _onLastListenerRemoved = onLastListenerRemoved,
        super(value);

  @override
  void addListener(listener) {
    super.addListener(listener);
    _listenerCount++;
    if (_listenerCount == 1) {
      _onFirstListenerAdded();
    }
  }

  @override
  void removeListener(listener) {
    super.removeListener(listener);
    _listenerCount--;
    if (_listenerCount == 0) {
      _onLastListenerRemoved();
    }
  }
}
