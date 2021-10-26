package com.giserpeng.ntripshare.ntrip;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

class InsecureBluetooth {
    InsecureBluetooth() {
    }

    private static BluetoothSocket createRfcommSocketToServiceRecord(BluetoothDevice bluetoothDevice, int i, UUID uuid, boolean z) throws IOException {
        try {
            Constructor declaredConstructor = BluetoothSocket.class.getDeclaredConstructor(new Class[]{Integer.TYPE, Integer.TYPE, Boolean.TYPE, Boolean.TYPE, BluetoothDevice.class, Integer.TYPE, ParcelUuid.class});
            if (declaredConstructor != null) {
                declaredConstructor.setAccessible(true);
                Field declaredField = BluetoothSocket.class.getDeclaredField("TYPE_RFCOMM");
                declaredField.setAccessible(true);
                int intValue = ((Integer) declaredField.get(null)).intValue();
                ParcelUuid parcelUuid = uuid != null ? new ParcelUuid(uuid) : null;
                return (BluetoothSocket) declaredConstructor.newInstance(new Object[]{Integer.valueOf(intValue), Integer.valueOf(-1), Boolean.valueOf(false), Boolean.valueOf(true), bluetoothDevice, Integer.valueOf(i), parcelUuid});
            }
            throw new RuntimeException("can't find the constructor for socket");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static BluetoothSocket createRfcommSocketToServiceRecord(BluetoothDevice bluetoothDevice, UUID uuid, boolean z) throws IOException {
        return createRfcommSocketToServiceRecord(bluetoothDevice, -1, uuid, z);
    }
}