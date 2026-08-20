=========================================================================
          Epson ePOS SDK for Android Version 2.36.0

          Copyright (C) Seiko Epson Corporation 2015 - 2025. All rights reserved.
=========================================================================

1.Note
The Epson ePOS SDK for Android is an SDK aimed at development engineers who are
developing Android applications for printing on an EPSON TM printer and an EPSON TM-
Intelligent printer.

2. Environment
  [Support OS]
    - Android Version 5.0 to 5.1.1
    - Android Version 6.0 to 6.0.1
    - Android Version 7.0 to 7.1.2
    - Android Version 8.0 to 8.1
    - Android Version 9.0
    - Android Version 10.0
    - Android Version 11.0
    - Android Version 12.0
    - Android Version 13.0
    - Android Version 14.0
    - Android Version 15.0
    - Android Version 16.0

  [Support interface]
    [TM Printer]
      - Wired LAN
      - Wireless LAN
      - Bluetooth
      - USB (TypeA/TypeB/TypeC)
    [TM-Intelligent Printer]
      - Wired LAN
    [TM-T88VI-iHUB]
      - Wired LAN
      - Wireless LAN
      - USB

  [Development environment]
      - Android SDK r15 or later
      - Java Development Kit 7 or later

  [Android Device]
    - Devices that support ARMv5TE
    - Devices that support AArch64
    - Devices that support x86-64
    - Devices that support armeabi-v7a
    - Devices that support x86

3. Supported Products
  For detailed information, please see Epson ePOS SDK for Android User's Manual.

4. Supplied Files

- ePOS2.jar
  Compiled Java class file, archived into a jar format file to allow APIs 
  to be used from Java programs.

- ePOSEasySelect.jar
  A Java class file for selecting a printer easily

- libepos2.so
  Library for function execution (ARMv5TE, AArch64 and x86-64 supported)

- libeposeasyselect.so
  Library for the ePOSEasySelect function execution (ARMv5TE, AArch64 and x86-64 supported)

- ePOS_SDK_Sample_Android.zip
  A sample program file

- DeviceControlProgram_Sample.zip
  This file contains sample device control programs

- EULA.en.txt
  Contains the SOFTWARE LICENSE AGREEMENT

- EULA.ja.txt
  Contains the SOFTWARE LICENSE AGREEMENT (The Japanese-language edition)

- NOTICE.txt
  Lists OSS licenses and copyrights.

- ePOS_SDK_Android_um_en_revx.pdf
  A user's manual

- ePOS_SDK_Android_um_ja_revx.pdf
  A user's manual (The Japanese-language edition)

- ePOS_SDK_Android_Migration_Guide_en_revx.pdf
  A migration guide

- ePOS_SDK_Android_Migration_Guide_ja_revx.pdf
  A migration guide (The Japanese-language edition)

- TM-DT_Peripherals_en_revx.pdf
  This is the TM-DT Series Peripheral Device Control Guide

- TM-DT_Peripherals_ja_revx.pdf
  This is the TM-DT Series Peripheral Device Control Guide (The Japanese-language edition)

- JSON_Spec_sheet_revx.pdf
  JSON specification sheet

- SB-H50_Peripherals_ja_revx.pdf
  This is the SB-H50 Peripheral Device Control Guide (The Japanese-language edition)

- README.en.txt
  This file

- README.ja.txt
  The Japanese-language edition of this file

- OPOS_CCOs_1.14.001.msi
  This is the OPOS CCO installer package

5. Remarks

- For detailed information, please see Epson ePOS SDK for Android User's Manual.

- In the case of USB interface, it is recommended that you obtain permission
  to access theUSB device in the application in advance.
  Noted below, how to get the permission.
   1. Enter the following code into the AndroidManifest.xml file.
      <manifest ...>
          <application>
              <activity ...>
                  <intent-filter>
                      <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
                  </intent-filter>
                  <meta-data android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
                    android:resource="@xml/device_filter" />
              </activity>
          </application>
      </manifest>

   2. Add the res/xml/device_filter.xml in resource file,
      enter the following code into the device_filter.xml file.
      <?xml version="1.0" encoding="utf-8"?>  
      <resources>  
          <usb-device vendor-id="1208" />
      </resources>

  Please select the OK button when you get the permission dialog is displayed.

  If you don't obtain permission to access the USB device in advance,
  there are the following notes when using the connect method.
    - When you select the OK button in the Permissions dialog box, it takes a 
      long time of about 10 seconds to open port.
    - When you select the Cancel button in the Permissions dialog box, it wait
      for a timeout of 30 seconds.

- If you want to set the minifyEnabled to true in the Android Studio,
  please add the followings to the proguard file.

  -keep class com.epson.** { *; }
  -dontwarn com.epson.**

  Proguard file (proguard-rules.pro) is set as follows in build.gradle file.
     buildTypes {
        release {
           proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
     }

- When printing process is repeated, create and destroy the instance of Printer class
  outside the iteration process and do not repeat it at short intervals.

- Call addTextLang API in first on each print data.

- The ePOS2_Printer sample program imports libepos2.so libraries 
  with all supported-architecture: x86_64, x86, armv-7 and arm64-v8a.
  Other sample programs haven't imported all of the supported architectures.
  Please copy/import the correct libraries that matches your architectures 
  for your development/release environment when building the other sample programs.

- In the following cases, please acquire the firmware list using the -AB option of download-FirmwareList.
  -When updating the firmware from 13.** ESC/POS to 13.** ESC/POS on TM-m30III and TM-m30III-H printers.
  -When updating the firmware from 63.** ESC/POS to 63.** ESC/POS on TM-T88VI and TM-T88VII printers.

- If you are using the firmware update feature, please update the SDK to ver 2.36.0 or later by February 2027. 
  This update is required due to the server upgrade scheduled for March 2027.

6. Restriction

- Discovery function of the following TM Intelligent printer doesn't support.

    TM-DT series (TM-DT software version 3.01 or earlier)
    TM-i  series (TM-i firmware version 4.30 or earlier)

  If you turn on the TM Intelligent printer after starting the search, the TM Intelligent
  printer may not be detected. In that case, leave enough time for the TM Intelligent printer
  to become printable, then start the search again.

7. Version History

  Version 2.36.0
    [Function change]
      - Updated the user’s manual.
      - Updated the server used by the firmware update APIs.
    [Bug fix]
      - Fixed an issue where the system could crash during ePOS-Device XML control when other APIs were executed in parallel with the disconnect API.

  Version 2.35.1
    [Bug Fix]
      - Fixed an issue where the connection to the printer could sometimes fail during Secure Printing.

  Version 2.35.0
    [Updated support model]
      - SB-m30 Japanese model
    [Bug fix]
      - Fixed an issue where certain Japanese characters were not encoded correctly when printing QR codes.
   [Addressing Vulnerability]
      - Updated open-source libraries and replaced some components with alternative OSS or OS-native features.

  Version 2.33.2
    [Function change]
      - Improved print speed of Secure Printing.

  Version 2.33.1
    [Function change]
      - Support 16KB page sizes.
    [Updated support OS]
      - Android 16.0
    [Bug fix]
      - Fixed an issue where no response was received from the printer after reconnection.

  Version 2.33.0
    [Function change]
      - For Users in Europe, the Middle East and Africa European Radio Equipment Directive. Changes in the enhanced security specifications.
    [Updated supplied Files]
      - The software license agreement has been updated.

  Version 2.32.0
    [Function change]
      - Improved the quality of the sample program.

  Version 2.31.0a
    [Updated supplied Files]
      - Added SB-H50 Peripheral Device Control Guide.

  Version 2.31.0
    [Function change]
      - Support for updating to cloud service-compatible firmware
    [Updated support OS]
      - Android 15.0
    [Bug fix]
      - Improved the symptom where a crash could occur during the finalize method of classes provided by the ePOS SDK.
      - Improved updateFirmware via USB Type-B.

  Version 2.30.0a
    - The library is the same as Ver.2.30.0.
    [Updated support model]
      - TM-T20IV
      - TM-T20IV-SP
      - TM-T82IV

  Version 2.30.0
    [Updated support model]
      - SB-H50 Japanese model

  Version 2.29.0a
    - The library is the same as Ver.2.29.0.
    [Updated support model]
      - TM-T20IV-L
      - TM-T20X-II
      - TM-T82X-II
      - TM-T82IV-L
      - TM-T83IV

  Version 2.29.0
    [Function change]
      - The sample programs can now be built using Android Studio Giraffe 2022.3.1.

  Version 2.28.0
    [Function change]
      - Fixed an error when connecting via USB/BT in the LFCPrinter class.
    [Updated support model]
      - TM-U220II
    [Bug fix]
      - Fixed issue that could cause a crash when disconnecting after ERR_DISCONNECT occurred when controlling ePOS-Device.

  Version 2.27.0a
    - The library is the same as Ver.2.27.0.
    [Function change]
      - The software license agreement has been updated.
    [Updated support OS]
      - Android 14.0

  Version 2.27.0
    - Added new functions for Japanese users.

  Version 2.26.0
    - Added new functions
      - Added LFCPrinter Class.

  Version 2.25.0
    - Improved the quality of the sample program.

  Version 2.24.0a
    - The library is the same as Ver.2.24.0.
    - Added TM printer support
      - TM-m50II-H
    - Added support for printers on getPrinterSettingEx API
      - TM-m30
      - TM-m50

  Version 2.24.0
    - Added TM printer support
      - TM-m50II
    - Added new functions
      - Added PaperTakenSensor status in Printer Class.

  Version 2.23.0
    - Added Android version support
      - Android 13.0
    - Added TM printer support
      - TM-m30III
      - TM-m30III-H
    - Changed the maximum log capacity specified by the logSize of setLogSettings API from 50 MB to 1000 MB

  Version 2.22.1
    - Bug fix
      - Fixed crash when communicating with a bluetooth printer without request bluetooth permission
        on app API level is 31 or higher and Android 12 or higher terminal device.

  Version 2.22.0
    - Added Android version support
      - Android 12.0
    - Android 4.x was removed from support OS
    - Added TM printer support
      - TM-P20II
      - TM-P80II
    - Added new functions
      - Added verifyPassword API in Printer Class.
      - Added setPrinterSettingEx API with printer's password in Printer Class. SetPrinterSettingEx API without password is deprecated.
      - Added full-cut parameter on addCut API.

  Version 2.20.0
    - Added TM printer support
      - TM-L100

  Version 2.19.0
    - Added TM printer support
      - EU-m30
      - TM-L90 Liner-Free Label Printer Model
    - Added new functions
      - Added setPrinterSettingEx and getPrinterSettingEx API in Printer Class. 

  Version 2.18.0
    - Added TM printer support
      - TM-T88VII
    - Improved printer search speed.
    - Improved printer search function when the parameter bondedDevices of the start API of the Discovery class is TRUE.
    - Bug fix
      - Fixed a rare crash when using multiple classes at the same time with the printer turned off.

  Version 2.17.1
    - Added Android version support
      - Android 11.0
    - Improved printer search function on iOS 14
    - Bug fix
      - Fixed issue that the print layout is broken when page mode is used on TM-m30II(during ESC/POS control)

  Version 2.17.0
    - Added customer display support
      - DM-D70
    - Added new functions
      - Added GetPrinterInformation API in Printer Class. 
    - Improved performance of operate API in GermanyFiscalElement class. 

  Version 2.16.0
    - Added TM printer support
      - TM-m30II-S
      - TM-m30II-NT
      - TM-m50
    - Bug fix
      - Fixed issue that the ePOS SDK's reconnecting process doesn't end rarely　when the iOS device go into sleep mode during the ePOS SDK's reconnectiong process. 
    - Added new functions
      - Added "CODE128 auto" parameter to addBarcode API.
      - Added "usbDeviceName" parameter to filterOption in Discovery class. 

  Version 2.14.0
    - Added TM printer support
      - TM-m30II
      - TM-m30II-H
    - Added new functions
      - Batch rotate printing
      - UTF-8 printing

  Version 2.13.0
    - Added support Android version
      - Android 10.0
    - Added support Android Devices
      - armeabi-v7a-based Android devices
      - x86-based Android devices
    - Added new functions
      - GermanyFiscalElement class
      - FirmwareUpdate method in Printer class

  Version 2.12.2
    - Bug fix
      - Fixed issue where endTransaction API rarely causes an application to hang up. 

  Version 2.12.1
    - Bug fix
      - Fixed issue application hang up rarely when calling several API continuously.

  Version 2.12.0
    - Added TM printer support
      - TM-T20III
      - TM-T82III
    - Barcode scanner can now be connected to TM-T88VI.
    - Added filterOption in Discovery class.
      - bondedDevices : To search devices that were previously connected.
    - Bug fix
      - Fixed issue where wrong printing when connect/disconnect API is continuously in TM-U220.
      - Fixed rarely crash when multiple responses are received from the printer at the same time.
      - Fixed issue where tablet CPU usage will be 100% when returning from sleeping after executing connect API.
      - Fixed issue where connect API takes long time depending on environment.

  Version 2.11.0
    - Added Android version support
      - Android 8.1
      - Android 9.0
    - Added TM printer support
      - TM-T20IIIL
      - TM-T20X
      - TM-T81III
      - TM-T82X
      - TM-T82IIIL
      - TM-T83III
      - TM-T100
    - AddTextSize API and addLogo API can use in TM-U330.
    - GetAdmin API and getLocation API can use other than TM-DT series and TM-i series.
    - Bug fix
      - Fixed issue where callback of sendData API takes more than 30 seconds when printer is offline or printer become off-line during printing.
      - Fixed issue where order of printing is changed when sendData API is sent several times without waiting a callback of sendData API.
      - Fixed issue where parameter error is returned when addLayout API is used in TM-P80.
      - Fixed issue where connect API rarely causes an application to hang up.
      - Fixed crash when Discovery is running in the background.

  Version 2.10.0
    - Added AArch64 and x86-64 based Android devices support

  Version 2.9.2a
    - The library is the same as Ver.2.9.2.
    - Added TM printer support
      - TM-T70II-DT2
      - TM-T88VI-DT2

  Version 2.9.2
    - Bug fix
      - Fixed the TM-U330 cannot print second color at addTextStyle() and addImage().

  Version 2.9.0
    - Barcode scanner can now be connected to TM-m30.
    - Status update events other than Printer.EVENT_POWER_OFF are now notified immediately,
      regardless of the value of interval, only when the device ID is not added to
      the target parameter of connect () of the Printer class.
    - Bug fix
      - Fixed a phenomenon in which a TM printer that does not support LineDisplay connection
        executes connect when the printer is in the offline state and executes sendData after that,
        no offline factor error is returned and a TIMEOUT error is returned.
      - Fixed printing of Printer or display of LineDisplay might be delayed by the monitoring
        interval of status monitor.
      - Fixed the application may be forcibly terminated if a physical communication disconnection
        (disconnection of the LAN cable, etc.) occurs during execution of the connect API on
        TCP connection.
      - Fixed parseNFC() may fail in the network environment where UB-E03 exists.

  Version 2.8.1
    - Added peripheral devices support
      - POSKeyboard class
      - OtherPeripheral class
      - MSR class
    - Added the following to the package
      - Sample of device control programs
      - TM-DT Series Peripheral Device Control Guide
      - OPOS CCO installer package
    - Updated the OpenSSL embedded in the library from version 1.0.2k to version 1.0.2o.
    - Bug fix
      - Fixed the Printer class does not correctly notify errors during printing.

  Version 2.7.0
    - Added TM printer support
      - TM-H6000V
    - Added support device
      - DM-D210
    - Added devices class support
      - HybridPrinter class
    - Added Android version support
      - Android 7.1.2
      - Android 8.0
    - Keep the TCP connection even though no data transmitted.
    - Using TM-U330, QR Code, Code 39, Code 128 can be printed.
    - Bug fix
      - Fixed the stop API of Discovery class may take time to execute.
      - Fixed the application may be forcibly terminated when using ePOS-Print
        compatible API with USB connection.
      - Fixed a phenomenon that the application may be forcibly terminated when
        physical communication disconnection (disconnection of LAN cable, etc.)
        occurs while connecting with TCPS.
      - Fixed the application may be forcibly terminated when searching for a
        network-connected printer.

  Version 2.6.0
    - Added TM printer support
      - TM-T88VI Japanese model
    - Bug fix
      - Fixed printing of Printer or display of LineDisplay might be delayed by the monitoring
        interval of status monitor.

  Version 2.5.2
    - Bug Fixed
      - Fixed the application may hang up,
        when disconnect () is executed on another thread immediately after onPtrReceive () is called.

  Version 2.5.1a
    - Corrected the mistake of user's manual (The Japanese-language edition).

  Version 2.5.1
    - Added Android version support
      - Android 7.1.1
    - If the customer display is connected to the TM printer and communication is disconnected
      due to the printer's power OFF / ON, etc., communication will be restored by reconnecting
      with either the printer or the customer display.
    - Added "Printer.COVER_OPEN" to autoRecoverError of PrinterStatusInfo.
    - Added "CODE_ERR_REQUEST_ENTITY_TOO_LARGE" that returns when print job data with the size
      exceeding the capacity of the printer firmware was transmitted.
    - Updated the OpenSSL embedded in the library from version 1.0.2h to version 1.0.2k.
    - Bug Fixed
      - Fixed "ERR_NOT_FOUND" may be returned when executing the disconnect API of LineDisplay.
      - Fixed the initial value of the status monitor update interval may be 1 second.
      - Fixed the application may be terminated if status monitor is enabled for TM intelligent
        printer with SSL enabled.
      - Fixed in case of TCP/IP connection, the application may be terminated when search start
        and stop repeatedly.

  Version 2.5.0
    - Added TM printer support
      - TM-P80 Japanese model
    - Bug Fixed
      - Fixed the application may hang up when turning off the printer at disconnect API execution.
      - Fixed the sendData API callback may not be notified when offline occurs while printing with TM-P series.

  Version 2.4.2
    - Bug Fixed
      - Fixed printable width of addImage API, when the printer supports 42 columns mode.

  Version 2.4.1
    - Updated the OpenSSL embedded in the library to the new version.

  Version 2.4.0
    - Added TM printer support
      - TM-T88VI-iHUB
      - TM-T81II Vietnam model
      - TM-m30 South Asia model
    - Added Android version support
      - Android 6.0.1
      - Android 7.0
    - Added SSL communication support
    - Added multi-lingual keyboard support
    - Bug Fixed
      - Fixed a secondary connection I/F may fail printing.
      - Fixed printing notification may takes time when printing from a secondary connection I/F.
      - Fixed incorrect information may be returned by getPrefix API.
      - Fixed the search result for a peripheral device of intelligent printer.
         (The contents of the DeviceInfo structure might not be correct.)
      - Fixed the next sendData API may fail even though printer is online when previous sendData API
        was called while printer was offline.
      - Fixed in case of TCP/IP connection, application may hang up when you execute disconnect API,
        when the sendData API became the error of transmission failure.
    - Added "CODE_ERR_TOO_MANY_REQUESTS" that returns when the number of print jobs or data to be displayed
      on a display has exceeded the allowable limit of the printer's firmware.

  Version 2.3.0
    - Added TM printer support
      - TM-T60
      - TM-m30 Korean model
    - Bug Fixed
      - Fixed disconnect API may not returned when the API is called.
        * This situation only happen with TM Intelligent Printers.

  Version 2.1.0
    - Added TM printer support
      - TM-T88VI
    - Added Android version support
      - Android 6.0
    - Added the getSdkVersion API for getting SDK version in Log class
    - Improved TM-U220 functions.
      - Added double density of addImageAsync API in Printer class
      - Added second color of addTextStyle API in Printer class
      - Added addTextSize API in Printer class
      - Added addLogo API in Printer class
    - Bug Fixed
      - Fixed the rest of data is printed when OFFLINE condition happened
        while printing and then the OFFLINE condition is resolved.
      - Fixed printer status is "Connected" even though turn off printer
        while printing.
        * This situation only happen with TM-P20/TM-P60II/TM-m10/TM-m30/TM-T90II.
      - Fixed disconnection event may not occur when disconnect API is called.
      - Fixed the parseNFC API parses only the first data of the NFC tag that has multiple tag data.
      - Fixed the addFeedPosition API can not feed paper correctly.

  Version 2.0.0
    - New release



