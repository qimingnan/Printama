package com.anggastudio.printama;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class Printama {

    public static final int CENTER = -1;
    public static final int RIGHT = -2;
    public static final int LEFT = 0;
    public static final int FULL_WIDTH = -1;
    public static final int ORIGINAL_WIDTH = 0;
    private static Printama printama;
    private final PrinterUtil util;
    private BluetoothDevice printer;

    public Printama(Context context) {
        Pref.init(context);
        printer = getPrinter();
        util = new PrinterUtil(printer);
    }

    public void printTest() {
        printama.connect(printama -> {
            printama.printText(Printama.CENTER,
                    "------------------\n" +
                            "Print Test\n" +
                            "------------------\n");
            printama.feedPaper();
            printama.close();
        });
    }

    public static Printama with(Context context, Callback callback) {
        Printama printama = new Printama(context);
        callback.printama(printama);
        return printama;
    }

    public static Printama with(Context context) {
        printama = new Printama(context);
        return printama;
    }

    private static BluetoothDevice getPrinter() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice printer = null;
        for (BluetoothDevice device : defaultAdapter.getBondedDevices()) {
            if (device.getName().equalsIgnoreCase(Pref.getString(Pref.SAVED_DEVICE))) {
                printer = device;
            }
        }
        return printer;
    }

    public void printText(String text) {
        printText(LEFT, text);
    }

    public void printText(int align, String text) {
        util.setAlign(align);
        util.printText(text);
    }

    public void setLineSpacing(int lineSpacing) {
        util.setLineSpacing(lineSpacing);
    }

    public void setBold(boolean bold) {
        util.setBold(bold);
    }

    public void feedPaper() {
        util.feedPaper();
    }

    public void close() {
        new Handler().postDelayed(util::finish, 2000);
    }

    public void connect(final OnConnected onConnected) {
        connect(onConnected, null);
    }

    public void connect(final OnConnected onConnected, final OnFailed onFailed) {
        util.connectPrinter(() -> {
            if (onConnected != null) onConnected.onConnected(this);
        }, () -> {
            if (onFailed != null) onFailed.onFailed("Failed to connect printer");
        });
    }

    public boolean printImage(Bitmap bitmap) {
        return util.printImage(bitmap);
    }

    public boolean printImage(int alignment, Bitmap bitmap, int width) {
        return util.printImage(alignment, bitmap, width);
    }

    public static void showPrinterList(FragmentActivity activity, OnConnectPrinter onConnectPrinter) {
        Pref.init(activity);
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null && !defaultAdapter.getBondedDevices().isEmpty()) {
            FragmentManager fm = activity.getSupportFragmentManager();
            DeviceListFragment fragment = DeviceListFragment.newInstance();
            fragment.setDeviceList(defaultAdapter.getBondedDevices());
            fragment.setOnConnectPrinter(onConnectPrinter);
            fragment.show(fm, "DeviceListFragment");
        } else {
            onConnectPrinter.onConnectPrinter("failed to connect printer");
        }
    }

    public boolean printImage(Bitmap bitmap, int width) {
        return util.printImage(bitmap, width);
    }

    public void printDashedLine() {
        util.printDashedLine();
    }

    public void printDoubleDashedLine() {
        util.printDoubleDashedLine();
    }

    public void addNewLine() {
        util.addNewLine();
    }

    public void addNewLine(int count) {
        util.addNewLine(count);
    }

    public boolean isConnected() {
        return util.isConnected();
    }

    public BluetoothDevice getConnectedPrinter() {
        return getPrinter();
    }

    public static Bitmap getBitmapFromVector(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        return getBitmapFromVector(drawable);
    }

    public static Bitmap getBitmapFromVector(Drawable drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = drawable != null ? (DrawableCompat.wrap(drawable)).mutate() : null;
        }
        if (drawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }

    public interface OnConnected {
        void onConnected(Printama printama);
    }

    public interface OnFailed {
        void onFailed(String message);
    }

    public interface OnConnectPrinter {
        void onConnectPrinter(String printerName);
    }

    public interface Callback {
        void printama(Printama printama);
    }


}
