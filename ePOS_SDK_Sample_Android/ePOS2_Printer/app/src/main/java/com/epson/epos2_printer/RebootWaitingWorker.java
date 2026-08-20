package com.epson.epos2_printer;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;

public class RebootWaitingWorker {
    private static final int RESTART_INTERVAL = 60000;//millseconds

    public static boolean reconnect(Printer printer, int maxWait, int interval) {
        int timeout = 0;
        do{
            try {
                printer.disconnect();
                break;
            }
            catch(Exception e) {
                try {
                    Thread.sleep(interval*1000);
                }
                catch(InterruptedException ex) {
                }
            }
            timeout += interval;    // Not correct measuring. When result is not TIMEOUT, wraptime do not equal interval.

            if(timeout > maxWait) {
                return false;
            }
        }while(true);

        // Sleep RESTART_INTERVAL sec due to some printer do not available immediately after power on. see your printer's spec sheet.
        // Please set the sleep time according to the printer.
        try {
            Thread.sleep(RESTART_INTERVAL);
        }
        catch(InterruptedException e) {
        }

        timeout += RESTART_INTERVAL / 1000;

        do {
            long connectStartTime = System.currentTimeMillis();
            try {
                // For USB, change the target to "USB:".
                // Because USB port changes each time the printer restarts.
                // Please refer to the manual for details.
                printer.connect(MainActivity.mEditTarget.getText().toString(), Printer.PARAM_DEFAULT);
                break;
            }
            catch (Exception e) {
                if(((Epos2Exception) e).getErrorStatus()== Epos2Exception.ERR_CONNECT) {
                    try {
                        Thread.sleep(interval * 1000);
                    } catch (InterruptedException ex) {
                    }
                }
            }

            long connectElapsedTime = (System.currentTimeMillis() - connectStartTime) / 1000;
            timeout += (interval+connectElapsedTime);    // Not correct measuring. When result is not TIMEOUT, wraptime do not equal interval.

            if(timeout > maxWait) {
                return false;
            }
        }while(true);

        return true;
    }

}
