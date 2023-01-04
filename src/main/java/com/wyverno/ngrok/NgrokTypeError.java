package com.wyverno.ngrok;

public enum NgrokTypeError {
    NotHasAuthToken("ERR_NGROK_105"),
    NotHasApiKey("ERR_NGROK_202"),
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
