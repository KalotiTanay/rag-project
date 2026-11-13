package com.ragqa.generation;

public interface TokenHandler {
    void onToken(String tokenText);
    void onComplete();
    void onError(Throwable t);
}
