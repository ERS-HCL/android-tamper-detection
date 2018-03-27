package com.hcltech.tamperresistance.securitylib;

/**
 * Custom exception to send a meaningful error message for the failure scenarios.
 */
public class TamperCheckException extends Exception {


    public static final int EXCEPTION_CODE_DEBUG_MODE = 1;
    public static final int EXCEPTION_CODE_EMULATOR = 2;
    public static final int EXCEPTION_CODE_UNKNOWN_INSTALLER = 5;

    private int exceptionCode;

    public TamperCheckException(int exceptionCode, String message) {
        super(message);
        this.exceptionCode = exceptionCode;
    }

    public TamperCheckException(int exceptionCode, String message, Throwable throwable) {
        super(message, throwable);
        this.exceptionCode = exceptionCode;
    }

    /**
     * Get the exception code for the {@link TamperCheckException}
     * @return exception code
     * Possible exception codes:
     * {@link #EXCEPTION_CODE_DEBUG_MODE}, {@link #EXCEPTION_CODE_EMULATOR},
     * {@link #EXCEPTION_CODE_UNKNOWN_INSTALLER}
     */
    public int getExceptionCode(){
        return this.exceptionCode;
    }
}
