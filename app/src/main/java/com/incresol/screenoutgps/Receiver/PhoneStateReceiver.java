package com.incresol.screenoutgps.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PhoneStateReceiver extends BroadcastReceiver {
    public static String TAG = "PhoneStateReceiver";
    public static boolean kioskmode;
    SmsManager smsManager;
    SharedPreferences sharedPreferences;
    static List<String> inboundList;
    static List<String> outboundList;


    @Override
    public void onReceive(Context context, Intent intent) {
        sharedPreferences = context.getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
        System.out.println("PhoneStateReceiver onReceive() action -> " + intent.getAction() + "\t state ->" + intent.getStringExtra(TelephonyManager.EXTRA_STATE));
        smsManager = SmsManager.getDefault();
        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d(TAG, "PhoneStateReceiver* kiosk *Call State=" + state);
            loadArrayInbound(context);
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Log.d(TAG, "PhoneStateReceiver* kiosk *Idle");
            } else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if (incomingNumber.length() > 10) {
                    incomingNumber = incomingNumber.substring(incomingNumber.length() - 10);
                    Log.d(TAG, "PhoneStateReceiver* kiosk *Incoming call " + incomingNumber + " kioskmode => " + kioskmode);
                }
                processIncomingCall(incomingNumber, context);
            } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                Log.d(TAG, "PhoneStateReceiver * kiosk *Offhook");
            }
        } else if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            // Outgoing call
            loadArrayOutbound(context);
            String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            processOutgoingCall(outgoingNumber, context);
            Log.d(TAG, "PhoneStateReceiver * kiosk *Outgoing call " + outgoingNumber);
        } else {
            Log.d(TAG, "PhoneStateReceiver * kiosk *unexpected intent.action=" + intent.getAction());
        }
    }

    public boolean killCall(Context context) {
        try {
            // Get the boring old TelephonyManager
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            // Get the getITelephony() method
            Class classTelephony = Class.forName(telephonyManager.getClass().getName());
            Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");

            // Ignore that the method is supposed to be private
            methodGetITelephony.setAccessible(true);


            // Invoke getITelephony() to get the ITelephony interface
            Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);

            // Get the endCall method from ITelephony
            Class telephonyInterfaceClass = Class.forName(telephonyInterface.getClass().getName());
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");

            // Invoke endCall()
            methodEndCall.invoke(telephonyInterface);

        } catch (Exception ex) { // Many things can go wrong with reflection
            // calls
            Log.d(TAG, "PhoneStateReceiver * kiosk *" + ex.toString());
            return false;
        }
        return true;
    }

    public void processIncomingCall(String incomingNumber, Context context) {
        System.out.println("processIncomingCall -> " + incomingNumber);
        if (kioskmode) {
            if (inboundList != null && inboundList.size() > 0) {
                for (int i = 0; i < inboundList.size(); i++) {
                    if (inboundList.get(i) != null) {
                        System.out.println("CALLS -> incoming -> " + inboundList.get(i));
                        if (!inboundList.get(i).contains(incomingNumber)) {
                            if (i + 1 == inboundList.size()) {
                                boolean cutCall = killCall(context);
                                if (cutCall) {
                                    System.out.println("Incoming Call  kiosk Disconnected !!!!");
                                } else {
                                    System.out.println("Incoming Call kiosk  Disconnection encountered a problem!!");
                                }
                            }
                        } else {
                            System.out.println("incoming number is valid -> " + incomingNumber);
                            return;
                        }
                    } else {
                        boolean cutCall = killCall(context);
                        if (cutCall) {
                            System.out.println("Incoming Call  kiosk Disconnected !!!!");
                        } else {
                            System.out.println("Incoming Call kiosk  Disconnection encountered a problem!!");
                        }
                    }
                }
            } else {
                boolean cutCall = killCall(context);
                if (cutCall) {
                    System.out.println("Incoming Call  kiosk Disconnected !!!!");
                } else {
                    System.out.println("Incoming Call kiosk  Disconnection encountered a problem!!");
                }
            }
        } else {
            System.out.println("incoming kiosk mode is not activated yet");
        }
    }

    public void processOutgoingCall(String outgoingNumber, Context context) {
        System.out.println("processOutgoingCall -> " + outgoingNumber);
        if (kioskmode) {
            if (outboundList != null && outboundList.size() > 0) {
                for (int i = 0; i < outboundList.size(); i++) {
                    if (outboundList.get(i) != null) {

                        if (!outboundList.get(i).contains(outgoingNumber)) {
                            if (i + 1 == outboundList.size()) {
                                boolean cutCall = killCall(context);
                                if (cutCall) {
                                    System.out.println("Outgoing Call  kiosk Disconnected !!!!");
                                } else {
                                    System.out.println("Outgoing Call kiosk  Disconnection encountered a problem!!");
                                }
                            }
                        } else {
                            return;
                        }
                    } else {
                        boolean cutCall = killCall(context);
                        if (cutCall) {
                            System.out.println("Outgoing Call  kiosk Disconnected !!!!");
                        } else {
                            System.out.println("Outgoing Call kiosk  Disconnection encountered a problem!!");
                        }
                    }
                }
            } else {
                boolean cutCall = killCall(context);
                if (cutCall) {
                    System.out.println("Outgoing Call  kiosk Disconnected !!!!");
                } else {
                    System.out.println("Outgoing Call kiosk  Disconnection encountered a problem!!");
                }
            }
        }
    }

    public static void loadArrayOutbound(Context mContext) {
        SharedPreferences mSharedPreference = mContext.getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
        outboundList = new ArrayList<>();
        if (outboundList != null && outboundList.size() > 0) {
            outboundList.clear();
        }
        int size = mSharedPreference.getInt("outboundCallList_Count", -1);
        if (size != -1) {
            for (int i = 0; i < size; i++) {
                String number = mSharedPreference.getString("outbound_" + i, null);
                outboundList.add(number);
            }
        }
        System.out.println("loadArrayOutbound -> " + outboundList);
    }

    public static void loadArrayInbound(Context mContext) {
        SharedPreferences mSharedPreference = mContext.getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
        inboundList = new ArrayList<>();
        if (inboundList != null && inboundList.size() > 0) {
            inboundList.clear();
        }
        int size = mSharedPreference.getInt("inboundCallList_Count", -1);
        if (size != -1) {
            for (int i = 0; i < size; i++) {
                String number = mSharedPreference.getString("inbound_" + i, null);
                inboundList.add(number);
            }
        }
        System.out.println("loadArrayInbound -> " + inboundList);
    }
}
