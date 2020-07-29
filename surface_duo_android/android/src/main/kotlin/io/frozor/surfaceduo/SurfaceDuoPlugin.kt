package io.frozor.surfaceduo

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import androidx.annotation.NonNull;
import com.microsoft.device.dualscreen.core.ScreenHelper

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** SurfaceDuoPlugin */
public class SurfaceDuoPlugin : FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
    private val _methodChannelName = "io.frozor.surfaceduo.methods"
    private val _eventChannelName = "io.frozor.surfaceduo.events.hinge"
    private val _hingeAngleSensorName = "Hinge Angle"

    private lateinit var _methodChannel: MethodChannel
    private lateinit var _eventChannel: EventChannel
    private lateinit var _context: Context
    private lateinit var _sensorManager: SensorManager
    private lateinit var _hingeAngleSensor: Sensor
    private lateinit var _hingeAngleSensorListener: SensorEventListener
    private var _hingeAngle = Surface.ROTATION_0

    //  private lateinit var _activity: Activity
    private var _methodHandlers: Map<String, (call: MethodCall, result: Result) -> Unit> = mapOf(
            "isDeviceSurfaceDuo" to ::onIsDeviceSurfaceDuo,
            "isAppSpanned" to ::onIsAppSpanned,
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
        _eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, _eventChannelName)
        _eventChannel.setStreamHandler(this)

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
        setupHingeSensor(arguments, events)
        _sensorManager.registerListener(
                _hingeAngleSensorListener,
                _hingeAngleSensor,
                SensorManager.SENSOR_DELAY_NORMAL)
    }

    /**
     * EventChannel.StreamHandler event for cancelling listening
     */
    override fun onCancel(arguments: Any?) {
        _sensorManager.unregisterListener(_hingeAngleSensorListener, _hingeAngleSensor)
    }

    private fun setupHingeSensor(arguments: Any?, events: EventChannel.EventSink?) {
        _sensorManager = _context.getSystemService(SENSOR_SERVICE) as SensorManager
        _hingeAngleSensor = _sensorManager
                .getSensorList(Sensor.TYPE_ALL)
                .first { sensor -> sensor.name.contains(_hingeAngleSensorName) }
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

    private fun onIsDeviceSurfaceDuo(call: MethodCall, result: Result) {
        result.success(ScreenHelper.isDeviceSurfaceDuo(_context))
    }

    private fun onIsAppSpanned(call: MethodCall, result: Result) {
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
