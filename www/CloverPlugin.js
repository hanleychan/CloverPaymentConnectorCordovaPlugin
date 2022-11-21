var Clover = {
	sale : function(remoteApplicationId, externalId, amount, tipMode, cardEntryMethod, autoAcceptSignature, disableDuplicateChecking, disableReceiptSelection, autoAcceptPaymentConfirmations, allowOfflinePayment, successCallback, errorCallback) {
		cordova.exec( successCallback, errorCallback,
			'Clover',
			'sale',
			[{
				'remoteApplicationId' : remoteApplicationId,
				'externalId' : externalId,
				'amount' : amount,
				'tipMode' : tipMode,
				'cardEntryMethod' : cardEntryMethod,
				'autoAcceptSignature' : autoAcceptSignature,
				'disableDuplicateChecking' : disableDuplicateChecking,
                'disableReceiptSelection' : disableReceiptSelection,
				'autoAcceptPaymentConfirmations' : autoAcceptPaymentConfirmations,
				'allowOfflinePayment' : allowOfflinePayment,
			}]
		);
	},
	preAuth : function( remoteApplicationId, externalId, amount, cardEntryMethod, disableDuplicateChecking, disableReceiptSelection, autoAcceptPaymentConfirmations, successCallback, errorCallback) {
		cordova.exec( successCallback, errorCallback,
			'Clover',
			'preAuth',
			[{
				'remoteApplicationId' : remoteApplicationId,
				'externalId' : externalId,
				'amount' : amount,
				'cardEntryMethod' : cardEntryMethod,
				'disableDuplicateChecking' : disableDuplicateChecking,
				'disableReceiptSelection' : disableReceiptSelection,
				'autoAcceptPaymentConfirmations' : autoAcceptPaymentConfirmations
			}]
		);
	},
	manualRefund : function(remoteApplicationId, externalId, amount, cardEntryMethod, disableDuplicateChecking, autoAcceptPaymentConfirmations, disablePrinting, disableReceiptSelection, successCallback, errorCallback) {
		cordova.exec( successCallback, errorCallback,
	        'Clover',
	        'manualRefund',
	        [{
	            'remoteApplicationId' : remoteApplicationId,
	            'externalId' : externalId,
	            'amount' : amount,
	            'cardEntryMethod' : cardEntryMethod,
	            'disableDuplicateChecking' : disableDuplicateChecking,
	            'autoAcceptPaymentConfirmations' : autoAcceptPaymentConfirmations,
	            'disablePrinting' : disablePrinting,
	            'disableReceiptSelection' : disableReceiptSelection
	        }]
	    );
	},

	refundPayment : function( remoteApplicationId, orderId, paymentId, amount, disableReceiptSelection, disablePrinting, successCallback, errorCallback) {
		cordova.exec(successCallback, errorCallback,
			'Clover',
			'refundPayment',
			[{
				'remoteApplicationId' : remoteApplicationId,
				'orderId' : orderId,
				'paymentId' : paymentId,
				'amount' : amount,
				'disableReceiptSelection' : disableReceiptSelection,
				'disablePrinting' : disablePrinting
			}]
		);
	},

	voidPayment : function(remoteApplicationId, orderId, paymentId, voidReason, disableReceiptSelection, disablePrinting, successCallback, errorCallback) {
	    cordova.exec(successCallback, errorCallback,
            'Clover',
            'voidPayment',
            [{
                'remoteApplicationId' : remoteApplicationId,
                'orderId' : orderId,
                'paymentId' : paymentId,
                'voidReason' : voidReason,
                'disableReceiptSelection' : disableReceiptSelection,
                'disablePrinting' : disablePrinting
            }]
	    );
	},

	capturePreAuth : function(remoteApplicationId, paymentId, amount, tipAmount, successCallback, errorCallback ) {
	    cordova.exec(successCallback, errorCallback,
	        'Clover',
	        'capturePreAuth',
	        [{
	            'remoteApplicationId' : remoteApplicationId,
	            'paymentId' : paymentId,
	            'amount' : amount,
				'tipAmount' : tipAmount
	        }]
	    );
	},

    retrievePayment : function(remoteApplicationId, externalId, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback,
            'Clover',
            'retrievePayment',
            [{
                'remoteApplicationId' : remoteApplicationId,
                'externalId' : externalId
            }]
        );
    },

	closeout : function(remoteApplicationId, successCallback, errorCallback) {
	    cordova.exec(successCallback, errorCallback,
	        'Clover',
	        'closeout',
	        [{
	            'remoteApplicationId' : remoteApplicationId
	        }]
	    );
	}
};

module.exports = Clover;
