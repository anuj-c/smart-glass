package com.smart_g

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

object Permissions {
  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  fun cameraPermission(permissionIntent: PendingIntent, intent: Intent, context: Context, actionUsbPermission: String, usbManager: UsbManager) {
    Log.d("TAG", "BroadcastReceiver onReceive")
    try{
      when (intent.action) {
        actionUsbPermission -> {
          Log.d("TAG", "BroadcastReceiver onReceive2")
          synchronized(this) {
            val device: UsbDevice? =
              intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
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
          val device: UsbDevice? =
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
          if (device != null) {
            Log.d("TAG", "USB attached: ${device.deviceName}")
          }
          device?.apply {
            usbManager.requestPermission(device, permissionIntent)
          }
        }
      }
    }catch(e: Exception) {
      Log.d("TAG", "$e")
    }
  }
}