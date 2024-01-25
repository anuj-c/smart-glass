package com.smart_g

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  override fun getMainComponentName(): String = "smart_g"
  private val actionUsbPermission = "com.smart_g.USB_PERMISSION"
  private lateinit var usbManager: UsbManager
  private lateinit var usbReceiver: BroadcastReceiver


  /**
   * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
   * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
   */
  override fun createReactActivityDelegate(): ReactActivityDelegate =
    DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)

    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      Log.d("TAG", "onCreate: MainActivity created")

      usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
      val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(actionUsbPermission), PendingIntent.FLAG_IMMUTABLE)
      val filter = IntentFilter(actionUsbPermission)

      usbReceiver = object : BroadcastReceiver() {
          @RequiresApi(Build.VERSION_CODES.TIRAMISU)
          override fun onReceive(context: Context, intent: Intent) {
              Log.d("TAG", "BroadcastReceiver onReceive")
              when (intent.action) {
                  actionUsbPermission -> {
                      Log.d("TAG", "BroadcastReceiver onReceive2")
                      synchronized(this) {
                          val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                          if (device != null) {
                              Log.d("TAG", "Device: ${device.deviceName}")
                          }
                          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                              device?.apply {
                                  Log.d("TAG", "USB Permission Granted for device: ${device.deviceName}")
                                  // Permission granted, handle the device
                              }
                          } else {
                              Log.d("TAG", "USB Permission Denied for device: ${device?.deviceName}")
                          }
                      }
                  }
                  UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                      val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                      if (device != null) {
                          Log.d("TAG", "USB attached: ${device.deviceName}")
                      }
                      device?.apply {
                          usbManager.requestPermission(device, permissionIntent)
                      }
                  }
              }
          }
      }

      registerReceiver(usbReceiver, filter)

      Log.d("TAG", "BroadcastReceiver registered for USB Permission")
  }

  override fun onResume() {
      super.onResume()
      val filter = IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
      registerReceiver(usbReceiver, filter)
      Log.d("TAG", "BroadcastReceiver registered for USB Device Attached")
  }

  override fun onPause() {
      super.onPause()
      unregisterReceiver(usbReceiver)
      Log.d("TAG", "BroadcastReceiver unregistered")
  }
}
