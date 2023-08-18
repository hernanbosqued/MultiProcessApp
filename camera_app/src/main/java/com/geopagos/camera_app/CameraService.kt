package com.geopagos.camera_app

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.IBinder
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import androidx.core.app.NotificationCompat

@SuppressLint("InflateParams")
class CameraService : Service() {

    private val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    private val textureView by lazy {
        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        layoutInflater.inflate(R.layout.overlay, null) as TextureView
    }

    private val cameraManager by lazy { getSystemService(Context.CAMERA_SERVICE) as CameraManager }

    private var previewSize: Size = Size(300, 400)

    private var cameraDevice: CameraDevice? = null

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(currentCameraDevice: CameraDevice) {
            cameraDevice = currentCameraDevice
            createCaptureSession()
        }

        override fun onDisconnected(currentCameraDevice: CameraDevice) {
            currentCameraDevice.close()
            cameraDevice = null
        }

        override fun onError(currentCameraDevice: CameraDevice, error: Int) {
            currentCameraDevice.close()
            cameraDevice = null
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground()
        startWithPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCamera()
        windowManager.removeView(textureView)
    }

    private fun startWithPreview() {
        initOverlay()
        initCam()
    }

    private fun initOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.height = previewSize.height
        params.width = previewSize.width
        windowManager.addView(textureView, params)
    }

    @SuppressLint("MissingPermission")
    private fun initCam() {
        cameraManager.cameraIdList.find { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            facing == CameraCharacteristics.LENS_FACING_FRONT
        }?.let { id ->
            cameraManager.openCamera(id, stateCallback, null)
        }
    }

    private fun startForeground() {
        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        }

        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE)
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.app_name))
            .setContentText(getText(R.string.app_name))
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.app_name))
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    private fun createCaptureSession() {
        cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)?.let {
            textureView.surfaceTexture?.setDefaultBufferSize(previewSize.width, previewSize.height)
            val surface = Surface(textureView.surfaceTexture)
            val sessionConfiguration = SessionConfiguration(
                SessionConfiguration.SESSION_REGULAR,
                listOf(OutputConfiguration(surface)),
                mainExecutor,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        it.addTarget(surface)
                        cameraCaptureSession.setRepeatingRequest(it.build(), null, null)
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    }
                }
            )

            cameraDevice?.createCaptureSession(sessionConfiguration)
        }
    }

    private fun stopCamera() {
        cameraDevice?.close()
        cameraDevice = null
    }

    companion object {
        const val ONGOING_NOTIFICATION_ID = 6660
        const val CHANNEL_ID = "cam_service_channel_id"
        const val CHANNEL_NAME = "cam_service_channel_name"
    }
}