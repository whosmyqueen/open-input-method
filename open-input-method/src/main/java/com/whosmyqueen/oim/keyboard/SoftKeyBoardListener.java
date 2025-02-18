package com.whosmyqueen.oim.keyboard;

public interface SoftKeyBoardListener {
    void onCommitResultText(String text);

    void onCommitText(SoftKey key);

    void onSubmit();

    void onDelete();

    void onBack();
}
