package com.wyverno.ngrok;

public enum NgrokTypeError {
    NotHasAuthToken("Your authtoken is available on your dashboard"), NotHasApiKey("skip"), NotCorrectPort("port");

    public final String errorMessage;

    NgrokTypeError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static NgrokTypeError getTypeError(String errorMessage) {
        if (errorMessage.contains(NotHasAuthToken.errorMessage)) {
            return NotHasAuthToken;
        } else if (errorMessage.contains(NotHasApiKey.errorMessage)) {
            return NotHasApiKey;
        } else {
            return null;
        }
    }
}
