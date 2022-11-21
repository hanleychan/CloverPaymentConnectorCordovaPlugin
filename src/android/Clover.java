package com.clover.cordova.plugin;

import android.content.Context;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import android.accounts.Account;

import com.clover.connector.sdk.v3.CardEntryMethods;
import com.clover.connector.sdk.v3.PaymentConnector;
import com.clover.sdk.v3.connector.IPaymentConnectorListener;
import com.clover.sdk.v3.payments.TipMode;
import com.clover.sdk.v3.remotepay.AuthResponse;
import com.clover.sdk.v3.remotepay.BaseResponse;
import com.clover.sdk.v3.remotepay.CapturePreAuthRequest;
import com.clover.sdk.v3.remotepay.CapturePreAuthResponse;
import com.clover.sdk.v3.remotepay.CloseoutRequest;
import com.clover.sdk.v3.remotepay.CloseoutResponse;
import com.clover.sdk.v3.remotepay.ConfirmPaymentRequest;
import com.clover.sdk.v3.remotepay.ManualRefundRequest;
import com.clover.sdk.v3.remotepay.ManualRefundResponse;
import com.clover.sdk.v3.remotepay.PaymentResponse;
import com.clover.sdk.v3.remotepay.PreAuthRequest;
import com.clover.sdk.v3.remotepay.PreAuthResponse;
import com.clover.sdk.v3.remotepay.ReadCardDataResponse;
import com.clover.sdk.v3.remotepay.RefundPaymentRequest;
import com.clover.sdk.v3.remotepay.RefundPaymentResponse;
import com.clover.sdk.v3.remotepay.RetrievePaymentRequest;
import com.clover.sdk.v3.remotepay.RetrievePaymentResponse;
import com.clover.sdk.v3.remotepay.RetrievePendingPaymentsResponse;
import com.clover.sdk.v3.remotepay.SaleRequest;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v3.remotepay.SaleResponse;
import com.clover.sdk.v3.remotepay.TipAdded;
import com.clover.sdk.v3.remotepay.TipAdjustAuthResponse;
import com.clover.sdk.v3.remotepay.VaultCardResponse;
import com.clover.sdk.v3.remotepay.VerifySignatureRequest;
import com.clover.sdk.v3.remotepay.VoidPaymentRefundResponse;
import com.clover.sdk.v3.remotepay.VoidPaymentRequest;
import com.clover.sdk.v3.remotepay.VoidPaymentResponse;

public class Clover extends CordovaPlugin {
    private static final String TAG = "CloverPlugin";
    private static final String SALES_REQUEST = "sale";
    private static final String PRE_AUTH_REQUEST = "preAuth";
    private static final String MANUAL_REFUND_REQUEST = "manualRefund"; // ind refund
    private static final String VOID_PAYMENT_REQUEST = "voidPayment";
    private static final String REFUND_PAYMENT_REQUEST = "refundPayment";
    private static final String CAPTURE_PRE_AUTH_REQUEST = "capturePreAuth";
    private static final String RETRIEVE_PAYMENT_REQUEST = "retrievePayment";
    private static final String CLOSEOUT_REQUEST = "closeout";

    private CallbackContext callbackContext; // The callback context from which we were invoked.
    private boolean deviceConnected = false;
    private PaymentConnector paymentConnector;

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        String[] validRequests = {
                SALES_REQUEST,
                PRE_AUTH_REQUEST,
                VOID_PAYMENT_REQUEST,
                CAPTURE_PRE_AUTH_REQUEST,
                REFUND_PAYMENT_REQUEST,
                MANUAL_REFUND_REQUEST,
                RETRIEVE_PAYMENT_REQUEST,
                CLOSEOUT_REQUEST,
        };

        boolean isValidRequest = false;
        for (String request : validRequests) {
            if ( request.equals(action) ) {
                isValidRequest = true;
                break;
            }
        }

        if ( isValidRequest ) {
            JSONObject arg_object = args.getJSONObject(0);
            String remoteApplicationId = arg_object.getString("remoteApplicationId");

            if ( !this.deviceConnected ) {
                this.paymentConnector = initializePaymentConnector(remoteApplicationId);

                while ( !this.deviceConnected ) { // Wait for device to be connected
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch(InterruptedException e) {
                        this.callbackContext.error("Error: " + e);
                        return false;
                    }

                    if ( this.deviceConnected ) {
                        break;
                    }
                }
            } else {
                // payment connector already initialized
            }

            switch (action) {
                case SALES_REQUEST: {
                    String externalId = arg_object.getString("externalId");
                    double amount = arg_object.getDouble("amount") * 100;
                    long formattedAmount = (long) amount;
                    boolean autoAcceptSignature = arg_object.getBoolean("autoAcceptSignature");
                    boolean disableDuplicateChecking = arg_object.getBoolean("disableDuplicateChecking");
                    boolean autoAcceptPaymentConfirmations = arg_object.getBoolean("autoAcceptPaymentConfirmations");
                    boolean disableReceiptSelection = arg_object.getBoolean("disableReceiptSelection");
                    boolean allowOfflinePayment = arg_object.getBoolean("allowOfflinePayment");

                    TipMode tipMode = this.tipMode(arg_object.getString("tipMode"));
                    int cardEntryMethod = this.cardEntryMethod(arg_object.getString("cardEntryMethod"));

                    SaleRequest saleRequest = new SaleRequest();

                    saleRequest.setExternalId(externalId);
                    saleRequest.setAmount(formattedAmount);
                    saleRequest.setTipMode(tipMode);
                    saleRequest.setCardEntryMethods(cardEntryMethod);
                    saleRequest.setAutoAcceptSignature(autoAcceptSignature);
                    saleRequest.setDisableDuplicateChecking(disableDuplicateChecking);
                    saleRequest.setAutoAcceptPaymentConfirmations(autoAcceptPaymentConfirmations);
                    saleRequest.setAllowOfflinePayment(allowOfflinePayment);
                    saleRequest.setDisableReceiptSelection(disableReceiptSelection);

                    Log.d(TAG, saleRequest.toString());

                    this.paymentConnector.sale(saleRequest);
                    break;
                }
                case PRE_AUTH_REQUEST: {
                    String externalId = arg_object.getString("externalId");
                    double amount = arg_object.getDouble("amount") * 100;
                    long formattedAmount = (long) amount;
                    boolean disableDuplicateChecking = arg_object.getBoolean("disableDuplicateChecking");
                    boolean autoAcceptPaymentConfirmations = arg_object.getBoolean("autoAcceptPaymentConfirmations");
                    boolean disableReceiptSelection = arg_object.getBoolean("disableReceiptSelection");
                    int cardEntryMethod = this.cardEntryMethod(arg_object.getString("cardEntryMethod"));

                    PreAuthRequest preAuthRequest = new PreAuthRequest();
                    preAuthRequest.setExternalId(externalId);
                    preAuthRequest.setAmount(formattedAmount);
                    preAuthRequest.setCardEntryMethods(cardEntryMethod);
                    preAuthRequest.setDisableDuplicateChecking(disableDuplicateChecking);
                    preAuthRequest.setAutoAcceptPaymentConfirmations(autoAcceptPaymentConfirmations);
                    preAuthRequest.setDisableReceiptSelection(disableReceiptSelection);

                    Log.d(TAG, preAuthRequest.toString());

                    this.paymentConnector.preAuth(preAuthRequest);
                    break;
                }
                case CAPTURE_PRE_AUTH_REQUEST: {
                    double amount = arg_object.getDouble("amount") * 100;
                    long formattedAmount = (long) amount;
                    String paymentId = arg_object.getString("paymentId");

                    double tipAmount = arg_object.getDouble("tipAmount") * 100;
                    long formattedTipAmount = (long) tipAmount;

                    CapturePreAuthRequest capturePreAuthRequest = new CapturePreAuthRequest();
                    capturePreAuthRequest.setPaymentId(paymentId);
                    capturePreAuthRequest.setAmount(formattedAmount);

                    capturePreAuthRequest.setTipAmount(formattedTipAmount);

                    Log.d(TAG, capturePreAuthRequest.toString());

                    this.paymentConnector.capturePreAuth(capturePreAuthRequest);
                    break;
                }
                case MANUAL_REFUND_REQUEST: {
                    String externalId = arg_object.getString("externalId");
                    double amount = arg_object.getDouble("amount") * 100;
                    long formattedAmount = (long) amount;
                    int cardEntryMethod = this.cardEntryMethod(arg_object.getString("cardEntryMethod"));
                    boolean autoAcceptPaymentConfirmations = arg_object.getBoolean("autoAcceptPaymentConfirmations");
                    boolean disableDuplicateChecking = arg_object.getBoolean("disableDuplicateChecking");
                    boolean disablePrinting = arg_object.getBoolean("disablePrinting");
                    boolean disableReceiptSelection = arg_object.getBoolean("disableReceiptSelection");

                    ManualRefundRequest manualRefundRequest = new ManualRefundRequest();

                    manualRefundRequest.setExternalId(externalId);
                    manualRefundRequest.setAmount( formattedAmount );
                    manualRefundRequest.setCardEntryMethods(cardEntryMethod);
                    manualRefundRequest.setAutoAcceptPaymentConfirmations(autoAcceptPaymentConfirmations);
                    manualRefundRequest.setDisableDuplicateChecking(disableDuplicateChecking);
                    manualRefundRequest.setDisablePrinting(disablePrinting);
                    manualRefundRequest.setDisableReceiptSelection(disableReceiptSelection);

                    Log.d(TAG, manualRefundRequest.toString());

                    this.paymentConnector.manualRefund(manualRefundRequest);
                    break;
                }
                case REFUND_PAYMENT_REQUEST: {
                    String orderId = arg_object.getString("orderId");
                    String paymentId = arg_object.getString("paymentId");
                    boolean disableReceiptSelection = arg_object.getBoolean("disableReceiptSelection");
                    boolean disablePrinting = arg_object.getBoolean("disablePrinting");

                    RefundPaymentRequest refundPaymentRequest = new RefundPaymentRequest();
                    if ( arg_object.isNull("amount") ) { // refund full amount
                        refundPaymentRequest.setFullRefund(true);
                    } else { // partial refund
                        double amount = arg_object.getDouble("amount") * 100;
                        long formattedAmount = (long) amount;

                        refundPaymentRequest.setAmount(formattedAmount);
                        refundPaymentRequest.setFullRefund(false);
                    }

                    refundPaymentRequest.setPaymentId(paymentId);
                    refundPaymentRequest.setOrderId(orderId);
                    refundPaymentRequest.setDisableReceiptSelection(disableReceiptSelection);
                    refundPaymentRequest.setDisablePrinting(disablePrinting);

                    Log.d(TAG, refundPaymentRequest.toString());

                    this.paymentConnector.refundPayment(refundPaymentRequest);
                    break;
                }
                case VOID_PAYMENT_REQUEST: {
                    String orderId = arg_object.getString("orderId");
                    String paymentId = arg_object.getString("paymentId");
                    String voidReason = arg_object.getString("voidReason");
                    boolean disableReceiptSelection = arg_object.getBoolean("disableReceiptSelection");
                    boolean disablePrinting = arg_object.getBoolean("disablePrinting");

                    VoidPaymentRequest voidPaymentRequest = new VoidPaymentRequest();
                    voidPaymentRequest.setOrderId(orderId);
                    voidPaymentRequest.setPaymentId(paymentId);
                    voidPaymentRequest.setVoidReason(voidReason);
                    voidPaymentRequest.setDisableReceiptSelection(disableReceiptSelection);
                    voidPaymentRequest.setDisablePrinting(disablePrinting);

                    Log.d(TAG, voidPaymentRequest.toString());

                    this.paymentConnector.voidPayment(voidPaymentRequest);
                    break;
                }
                case RETRIEVE_PAYMENT_REQUEST: {
                    String externalId = arg_object.getString("externalId");

                    RetrievePaymentRequest retrievePaymentRequest = new RetrievePaymentRequest();
                    retrievePaymentRequest.setExternalPaymentId(externalId);

                    Log.d(TAG, retrievePaymentRequest.toString());

                    this.paymentConnector.retrievePayment(retrievePaymentRequest);
                    break;
                }
                case CLOSEOUT_REQUEST: {
                    CloseoutRequest closeoutRequest = new CloseoutRequest();

                    Log.d(TAG, closeoutRequest.toString());

                    this.paymentConnector.closeout(closeoutRequest);
                    break;
                }
            }

            return true;
        }

        this.callbackContext.error("Invalid request: " + action);
        return false;
    }

    private void handleResponse(Object obj) {
        String responseMsg;
        boolean success = false;

        if ( obj instanceof PaymentResponse ) {
            PaymentResponse paymentResponse = (PaymentResponse) obj;
            responseMsg = paymentResponse.getJSONObject().toString();
            success = paymentResponse.getSuccess();
        } else if ( obj instanceof BaseResponse) {
            BaseResponse baseResponse = (BaseResponse) obj;

            responseMsg = baseResponse.getJSONObject().toString();
            success = baseResponse.getSuccess();
        } else {
            responseMsg = "Error processing response";
        }

        Log.d(TAG, responseMsg);

        if ( success ) {
            this.callbackContext.success(responseMsg);
        } else {
            this.callbackContext.error(responseMsg);
        }

        /*
        this.paymentConnector.resetDevice();
        this.paymentConnector.dispose();
        this.setDeviceConnected(false);
         */
    }

    private PaymentConnector initializePaymentConnector(String remoteApplicationId) {
        Context context = this.cordova.getActivity().getApplicationContext();
        Account cloverAccount = CloverAccount.getAccount(context);

        IPaymentConnectorListener paymentConnectorListener = new IPaymentConnectorListener() {

            @Override
            public void onVoidPaymentResponse(VoidPaymentResponse response) {
                Log.d(TAG, "onVoidPaymentResponse");
                handleResponse(response);
            }

            @Override
            public void onPreAuthResponse(PreAuthResponse response) {
                Log.d(TAG, "onPreAuthResponse");
                handleResponse(response);
            }

            @Override
            public void onAuthResponse(AuthResponse response) {
                Log.d(TAG, "onAuthResponse");
                handleResponse(response);
            }

            @Override
            public void onTipAdjustAuthResponse(TipAdjustAuthResponse response) {
                Log.d(TAG, "AUTH RESPONSE");
                handleResponse(response);
            }

            @Override
            public void onSaleResponse(SaleResponse response) {
                Log.d(TAG, "onSaleResponse:");
                handleResponse(response);
            }

            @Override
            public void onManualRefundResponse(ManualRefundResponse response) {
                Log.d(TAG, "onManualRefundResponse");
                handleResponse(response);
            }

            @Override
            public void onRefundPaymentResponse(RefundPaymentResponse response) {
                Log.d(TAG, "onRefundPaymentResponse");
                handleResponse(response);
            }

            @Override
            public void onVaultCardResponse(VaultCardResponse response) {
                Log.d(TAG, "onVaultCardResponse");
                handleResponse(response);
            }

            @Override
            public void onRetrievePendingPaymentsResponse(RetrievePendingPaymentsResponse retrievePendingPaymentResponse) {
                Log.d(TAG, "onRetrievePendingPaymentsResponse");
                handleResponse(retrievePendingPaymentResponse);
            }

            @Override
            public void onReadCardDataResponse(ReadCardDataResponse response) {
                Log.d(TAG, "onReadCardDataResponse");
                handleResponse(response);
            }

            @Override
            public void onCloseoutResponse(CloseoutResponse response) {
                Log.d(TAG, "onCloseoutResponse");
                handleResponse(response);
            }

            @Override
            public void onRetrievePaymentResponse(RetrievePaymentResponse response) {
                Log.d(TAG, "onRetrievePaymentResponse");
                handleResponse(response);
            }

            @Override
            public void onVoidPaymentRefundResponse(VoidPaymentRefundResponse response) {
                Log.d(TAG, "onVoidPaymentRefundResponse");
                handleResponse(response);
            }

            @Override
            public void onCapturePreAuthResponse(CapturePreAuthResponse response) {
                Log.d(TAG, "onCapturePreAuthResponse");
                handleResponse(response);
            }

            @Override
            public void onTipAdded(TipAdded tipAdded) {
                Log.d(TAG, "onTipAdded");
            }

            @Override
            public void onVerifySignatureRequest(VerifySignatureRequest request) {
                Log.d(TAG, "onVerifySignatureRequest");
            }

            @Override
            public void onConfirmPaymentRequest(ConfirmPaymentRequest request) {
                Log.d(TAG, "onConfirmPaymentRequest");
            }

            @Override
            public void onDeviceDisconnected() {
                Log.d(TAG, "onDeviceDisconnected");
                setDeviceConnected(false);
            }

            @Override
            public void onDeviceConnected() {
                Log.d(TAG, "onDeviceConnected");
                setDeviceConnected(true);
            }
        };

        return new PaymentConnector(context, cloverAccount, paymentConnectorListener, remoteApplicationId );
    }

    private void setDeviceConnected(boolean connected) {
        this.deviceConnected = connected;
    }

    private int cardEntryMethod(String cardEntryMethod) {
        switch (cardEntryMethod) {
            case "ICC_CONTACT":
                return CardEntryMethods.CARD_ENTRY_METHOD_ICC_CONTACT;
            case "MAG_STRIP":
                return CardEntryMethods.CARD_ENTRY_METHOD_MAG_STRIPE;
            case "MANUAL":
                return CardEntryMethods.CARD_ENTRY_METHOD_MANUAL;
            case "NFC_CONTACTLESS":
                return CardEntryMethods.CARD_ENTRY_METHOD_NFC_CONTACTLESS;
            case "ALL":
                return CardEntryMethods.ALL;
            default:
                return CardEntryMethods.DEFAULT;
        }
    }

    private TipMode tipMode(String tipMode) {
        switch (tipMode) {
            case "ON_SCREEN_BEFORE_PAYMENT":
                return TipMode.ON_SCREEN_BEFORE_PAYMENT;
            case "NO_TIP":
            default:
                return TipMode.NO_TIP;
        }
    }
}
