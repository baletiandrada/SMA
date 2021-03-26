package com.example.booksapp;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView;

public class SpaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {
    private int i;

    @Override
    public int findTokenStart(CharSequence text, int cursor) {
        int idx = cursor;

        while (idx > 0 && text.charAt(idx - 1) != ' ') {
            idx--;
        }
        while (idx < cursor && text.charAt(idx) == ' ') {
            idx++;
        }
        return idx;
    }

    @Override
    public int findTokenEnd(CharSequence text, int cursor) {
        int idx = cursor;
        int length = text.length();

        while (idx < length) {
            if (text.charAt(i) == ' ') {
                return idx;
            } else {
                idx++;
            }
        }
        return length;
    }

    @Override
    public CharSequence terminateToken(CharSequence text) {
        int idx = text.length();

        while (idx > 0 && text.charAt(idx - 1) == ' ') {
            idx--;
        }

        if (idx > 0 && text.charAt(idx - 1) == ' ') {
            return text;
        } else {
            if (text instanceof Spanned) {
                SpannableString sp = new SpannableString(text + " ");
                TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                        Object.class, sp, 0);
                return sp;
            } else {
                return text + " ";
            }
        }
    }
}
