package io.frozor.surfaceduo

import android.app.Activity
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.View
import androidx.annotation.NonNull;
import com.microsoft.device.dualscreen.core.ScreenHelper
import com.microsoft.device.dualscreen.core.ScreenMode
import com.microsoft.device.dualscreen.core.manager.ScreenModeListener

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** SurfaceDuoPlugin */
public class SurfaceDuoPlugin : FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler, ActivityAware {
    private val _methodChannelName = "io.frozor.surfaceduo.methods"
    private val _hingeEventChannelName = "io.frozor.surfaceduo.events.hinge"
    private val _screenModeEventChannelName = "io.frozor.surfaceduo.events.screenmode"
    private val _hingeAngleSensorName = "Hinge Angle"

    private lateinit var _methodChannel: MethodChannel
    private lateinit var _hingeEventChannel: EventChannel
    private lateinit var _screenModeEventChannel: EventChannel
    private lateinit var _context: Context
    private var _sensorManager: SensorManager? = null
    private var _hingeAngleSensor: Sensor? = null
    private var _hingeAngleSensorListener: SensorEventListener? = null
    private var _activity: Activity? = null
    private var _hingeAngle = Surface.ROTATION_0
    private var _screenModeListener: View.OnLayoutChangeListener? = null

    //  private lateinit var _activity: Activity
    private var _methodHandlers: Map<String, (call: MethodCall, result: Result) -> Unit> = mapOf(
            "isDeviceSurfaceDuo" to ::onIsDeviceSurfaceDuo,
            "isAppDualScreen" to ::onIsAppDualScreen,
            "getHingeMask" to ::onGetHingeMask,
            "getHingeAngle" to ::onGetHingeAngle
    )

    /**
     * FlutterPlugin lifecycle event for attaching to the flutter engine
     */
    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        _methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, _methodChannelName)
//    methodChannel = MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor, METHOD_CHANNEL_NAME)
        _methodChannel.setMethodCallHandler(this);

        _hingeEventChannel = EventChannel(flutterPluginBinding.binaryMessenger, _hingeEventChannelName)
        _hingeEventChannel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                setupHingeSensor(arguments, events)
                onListenToHingeSensor(arguments, events)
            }

            override fun onCancel(arguments: Any?) {
                onCancelListeningToHingeSensor(arguments)
            }
        })

        _screenModeEventChannel = EventChannel(flutterPluginBinding.binaryMessenger, _screenModeEventChannelName)
        _screenModeEventChannel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                onListenToScreenMode(arguments, events)
            }

            override fun onCancel(arguments: Any?) {
                onCancelListeningToScreenMode(arguments)
            }
        })

        _context = flutterPluginBinding.applicationContext
    }

    /**
     * FlutterPlugin lifecycle event for detaching from the flutter engine
     */
    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        _methodChannel.setMethodCallHandler(null)
    }

    /**
     * MethodCallHandler event for handling method calls
     */
    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (_methodHandlers.containsKey(call.method)) {
            val handler = _methodHandlers[call.method]
            try {
                handler?.invoke(call, result)
            } catch (e: Throwable) {
                result.error("exception" /*errorCode*/, e.message, null /*errorDetails*/)
            }
        } else {
            result.notImplemented()
        }
    }

    /**
     * EventChannel.StreamHandler event for beginning listening
     *
     * It is likely that there is a use case which requires args to be able to configure the delay
     */
    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        onListenToHingeSensor(arguments, events)
        onListenToScreenMode(arguments, events)
    }

    /**
     * EventChannel.StreamHandler event for cancelling listening
     */
    override fun onCancel(arguments: Any?) {
        onCancelListeningToHingeSensor(arguments)
        onCancelListeningToScreenMode(arguments)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        _activity = binding.activity
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        _activity = null
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    private fun setupHingeSensor(arguments: Any?, events: EventChannel.EventSink?) {
        if (_sensorManager == null) {
            _sensorManager = _context.getSystemService(SENSOR_SERVICE) as SensorManager
        }

        if (_hingeAngleSensor == null) {
            _hingeAngleSensor = _sensorManager
                    ?.getSensorList(Sensor.TYPE_ALL)
                    ?.first { sensor -> sensor.name.contains(_hingeAngleSensorName) }
        }

        _hingeAngleSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val angle = (event?.values?.get(0) ?: Surface.ROTATION_0) as Int
                _hingeAngle = angle
                events?.success(angle)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                /* no-op */
            }
        }
    }

    private fun onListenToHingeSensor(arguments: Any?, events: EventChannel.EventSink?) {
        setupHingeSensor(arguments, events)
        _sensorManager?.registerListener(
                _hingeAngleSensorListener,
                _hingeAngleSensor,
                SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun onListenToScreenMode(arguments: Any?, events: EventChannel.EventSink?) {
        _screenModeListener = View.OnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            run {
                val screenMode = if (ScreenHelper.isDualMode(_context)) {
                    ScreenMode.DUAL_SCREEN
                } else {
                    ScreenMode.SINGLE_SCREEN
                }
                events?.success(screenMode.id)
            }
        }
        _activity?.window?.decorView?.rootView?.addOnLayoutChangeListener(_screenModeListener)
    }

    private fun onCancelListeningToHingeSensor(arguments: Any?) {
        _sensorManager?.unregisterListener(_hingeAngleSensorListener, _hingeAngleSensor)
        _hingeAngleSensorListener = null
    }

    private fun onCancelListeningToScreenMode(arguments: Any?) {
        _activity?.window?.decorView?.rootView?.removeOnLayoutChangeListener(_screenModeListener)
        _screenModeListener = null
    }

    private fun onIsDeviceSurfaceDuo(call: MethodCall, result: Result) {
        result.success(ScreenHelper.isDeviceSurfaceDuo(_context))
    }

    private fun onIsAppDualScreen(call: MethodCall, result: Result) {
        result.success(ScreenHelper.isDualMode(_context))
    }

    private fun onGetHingeMask(call: MethodCall, result: Result) {
        val rect = ScreenHelper.getHinge(_context)
                ?: return result.error("hinge-missing" /*errorCode*/, "Hinge rect was not found", null /*errorDetails*/)
        val rectMap = mapOf(
                "left" to rect.left,
                "top" to rect.top,
                "right" to rect.right,
                "bottom" to rect.bottom
        )
        result.success(rectMap)
    }

    private fun onGetHingeAngle(call: MethodCall, result: Result) {
        result.success(_hingeAngle)
    }
}
