package com.wyverno.ngrok;

public enum NgrokTypeError {
    NotHasAuthToken("Your authtoken is available on your dashboard"),
    NotHasApiKey("The API authentication you specified does not look like a valid credential."),
    NotCorrectPort("port");

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
