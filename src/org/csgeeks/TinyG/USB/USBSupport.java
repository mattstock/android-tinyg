package org.csgeeks.TinyG.USB;

// Most of this ported from libftdi http://www.intra2net.com/en/developer/libftdi/.

import java.util.HashMap;
import java.util.Iterator;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class USBSupport {
	private static final String TAG = "TinyG-USB";
	public static final int USB_VENDOR_ID = 0x0403;
	public static final int USB_PRODUCT_ID = 0x6001;
	public static final long H_CLK = 120000000;
	public static final long C_CLK = 48000000;
	public static final int SIO_RESET = 0x00;
	public static final int SIO_MODEM_CTRL = 0x01;
	public static final int SIO_FLOW_CTRL = 0x02;
	public static final int SIO_SET_BAUD_RATE = 0x03;
	public static final int SIO_SET_DATA_REQUEST = 0x04;
	public static final int SIO_RESET_SIO = 0x00;
	public static final int SIO_RESET_PURGE_RX = 0x01;
	public static final int SIO_RESET_PURGE_TX = 0x02;
	
	static private long ftdi_to_clkbits(int baudrate, long clk, int clk_div) {
	    final int frac_code[] = {0, 3, 2, 4, 1, 5, 6, 7};
		long encoded_divisor;
	    long best_baud = 0;
	    long divisor;
	    long best_divisor;
	    if (baudrate >=  clk/clk_div) {
	        encoded_divisor = 0;
	        best_baud = clk/clk_div;
	    } else if (baudrate >=  clk/(clk_div + clk_div/2)) {
	        encoded_divisor = 1;
	        best_baud = clk/(clk_div + clk_div/2);
	    } else if (baudrate >=  clk/(2*clk_div)) {
	        encoded_divisor = 2;
	        best_baud = clk/(2*clk_div);
	    } else {
	        /* We divide by 16 to have 3 fractional bits and one bit for rounding */
	        divisor = clk*16/clk_div / baudrate;
	        if ((divisor & 1) != 0) /* Decide if to round up or down*/
	            best_divisor = (int) divisor /2 +1;
	        else
	            best_divisor = (int) divisor/2;
	        if(best_divisor > 0x20000)
	            best_divisor = 0x1ffff;
	        best_baud = clk*16/clk_div/best_divisor;
	        if ((best_baud & 1) != 0) /* Decide if to round up or down*/
	            best_baud = best_baud /2 +1;
	        else
	            best_baud = best_baud /2;
	        encoded_divisor = (best_divisor >> 3) | (frac_code[(int)(best_divisor & 0x7)] << 14);
	    }
	    return encoded_divisor;
	} 
	
	static public boolean ftdi_settings(UsbDeviceConnection conn) {
		int value, index; 
		long encoded_divisor;

		encoded_divisor = ftdi_to_clkbits(115200, C_CLK, 16);
		
		// Split into "value" and "index" values
		value = (int) (encoded_divisor & 0xFFFF);
		index = (int) (encoded_divisor >> 16);
		
		if (conn.controlTransfer(UsbConstants.USB_TYPE_VENDOR|UsbConstants.USB_DIR_OUT,
				SIO_RESET, SIO_RESET_SIO, 0, null, 0, 0) < 0) {
			Log.e(TAG, "Reset failed");
			return false;
		}
		if (conn.controlTransfer(UsbConstants.USB_TYPE_VENDOR|UsbConstants.USB_DIR_OUT,
				SIO_RESET, SIO_RESET_PURGE_RX, 0, null, 0, 0) < 0) {
			Log.e(TAG, "purge RX failed");
			return false;
		}
		if (conn.controlTransfer(UsbConstants.USB_TYPE_VENDOR|UsbConstants.USB_DIR_OUT,
				SIO_RESET, SIO_RESET_PURGE_TX, 0, null, 0, 0) < 0) {
			Log.e(TAG, "purge TX failed");
			return false;
		}
		if (conn.controlTransfer(UsbConstants.USB_TYPE_VENDOR|UsbConstants.USB_DIR_OUT,
				SIO_SET_BAUD_RATE, value, index, null, 0, 0) < 0) {
			Log.e(TAG, "baud set failed");
			return false;
		}
		if (conn.controlTransfer(UsbConstants.USB_TYPE_VENDOR|UsbConstants.USB_DIR_OUT,
				SIO_FLOW_CTRL, 0, (0x4 << 8), null, 0, 0) < 0) {
			Log.e(TAG, "flow control XONXOFF set failed");
			return false;
		}
		if (conn.controlTransfer(UsbConstants.USB_TYPE_VENDOR|UsbConstants.USB_DIR_OUT,
				SIO_SET_DATA_REQUEST, 0, 0, null, 0, 0) < 0) {
			Log.e(TAG, "line settings failed");
			return false;
		}
		return true;
	}
	
	// Iterate over attached USB devices looking for TinyG.
	// It doesn't handle multiple devices - it will pick one
	// at random.
	static public UsbDevice loadFTDI(UsbManager m) {
        HashMap<String, UsbDevice> deviceList = m.getDeviceList();
        Iterator<UsbDevice> dI = deviceList.values().iterator();
        while (dI.hasNext()) {
        	UsbDevice d = dI.next();
        	if ((d.getProductId() == USB_PRODUCT_ID) && (d.getVendorId() == USB_VENDOR_ID)) {
        		Log.d(TAG, "got FTDI USB: " + d.getDeviceName());
        		return d;
        	}
        }	
        return null;
	}

}
