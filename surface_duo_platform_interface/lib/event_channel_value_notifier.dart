import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

typedef TransformEvent<T> = T Function(dynamic);

// This could be represented by a bool, but is harder to read in the code
enum ListenerWaitingState { waitingForFirstToAdd, waitingForLastToRemove }

/// This class is a [ValueNotifier] whose value updates based on the values
/// given by an event channel. Subscriptions to the event channel are
/// dynamically added and removed when the first listener adds or the last one
/// removes their listeners.
class EventChannelValueNotifier<T> extends ValueNotifier<T> {
  final TransformEvent transformEvent;
  final EventChannel _eventChannel;
  ListenerWaitingState _listenerWaitingState =
      ListenerWaitingState.waitingForFirstToAdd;
  StreamSubscription _eventChannelSubscription;

  EventChannelValueNotifier(String channelName, T value, {this.transformEvent})
      : _eventChannel = EventChannel(channelName),
        super(value);

  @override
  void addListener(listener) {
    super.addListener(listener);
    if (_listenerWaitingState == ListenerWaitingState.waitingForFirstToAdd &&
        hasListeners) {
      _listenerWaitingState = ListenerWaitingState.waitingForLastToRemove;
      _addStreamListener();
    }
  }

  @override
  void removeListener(listener) {
    super.removeListener(listener);
    if (_listenerWaitingState == ListenerWaitingState.waitingForLastToRemove &&
        !hasListeners) {
      _listenerWaitingState = ListenerWaitingState.waitingForFirstToAdd;
      _removeStreamListener();
    }
  }

  void _addStreamListener() {
    _eventChannelSubscription =
        _eventChannel.receiveBroadcastStream().listen((event) {
      if (transformEvent != null) {
        event = transformEvent(event);
      }
      value = event;
    });
  }

  void _removeStreamListener() {
    assert(_eventChannelSubscription != null,
        "Event channel subscription is null during listener removal");
    _eventChannelSubscription.cancel();
  }
}
