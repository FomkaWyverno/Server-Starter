package com.wyverno.ngrok;

public enum NgrokTypeError {
    NotHasAuthToken("ERR_NGROK_105","ERR_NGROK_302"),
    NotHasApiKey("API key is missing"),
    NotCorrectPort();

    public final String[] errorMessages;

    NgrokTypeError(String... errorMessages) {
        this.errorMessages = errorMessages;
    }

    public static NgrokTypeError getTypeError(String message) {
        if (NotHasAuthToken.containsAll(message)) {
            return NotHasAuthToken;
        } else if (NotHasApiKey.containsAll(message)) {
            return NotHasApiKey;
        } else {
            return null;
        }
    }

    private boolean containsAll(String message) {
        for (String errorMessage : errorMessages) {
            if (message.contains(errorMessage)) return true;
        }
        return false;
    }
}
