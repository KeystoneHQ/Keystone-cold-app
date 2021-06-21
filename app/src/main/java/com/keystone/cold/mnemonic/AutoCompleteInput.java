/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.mnemonic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.collection.ArraySet;

import com.keystone.coinlib.WordList;
import com.keystone.cold.R;

import java.util.Arrays;
import java.util.Set;

public class AutoCompleteInput extends AppCompatAutoCompleteTextView {

    private static Set<String> sWordsSet = new ArraySet<>(Arrays.asList(WordList.words));
    @ColorInt
    private static int sNormalTextColor;
    @ColorInt
    private static int sWrongTextColor;
    @Nullable
    private FilterCompleteCallback mCallback;

    private ScrollView scrollView;

    public AutoCompleteInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        sNormalTextColor = context.getColor(R.color.text);
        sWrongTextColor = context.getColor(R.color.text_wrong);
        InputServant mInputServant = new InputServant(this);
        setAdapter(new ArrayAdapter<>(context, R.layout.mnemonic_dropdowm_item, WordList.words));
        setFilterCompleteCallback(mInputServant);
        setOnFocusChangeListener(mInputServant);
        setOnItemClickListener(mInputServant);
        setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        setTransformationMethod(null);
    }

    public void setWordList(String[] wordList) {
        setAdapter(new ArrayAdapter<>(getContext(), R.layout.mnemonic_dropdowm_item, wordList));
        sWordsSet = new ArraySet<>(Arrays.asList(wordList));
    }

    @Override
    public void showDropDown() {
        super.showDropDown();
    }

    private void adjustScroll() {
        Rect displayFrame = new Rect();
        getWindowVisibleDisplayFrame(displayFrame);

        int[] locationOnScreen = new int[2];
        getLocationOnScreen(locationOnScreen);

        int bottom = locationOnScreen[1] + getHeight();
        int availableHeightBelow = displayFrame.bottom - bottom;
        if (availableHeightBelow < 200) {
            if (scrollView != null) {
                scrollView.smoothScrollBy(0, 200 - availableHeightBelow);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        scrollView = (ScrollView) getParent().getParent().getParent().getParent();
    }

    @Override
    public void onFilterComplete(int count) {
        super.onFilterComplete(count);

        if (mCallback != null) {
            mCallback.onFilterComplete(this, count);
        }
    }

    public void setFilterCompleteCallback(FilterCompleteCallback callback) {
        mCallback = callback;
    }

    @Nullable
    public FilterCompleteCallback getFilterCompleteCallback() {
        return mCallback;
    }

    public interface FilterCompleteCallback {
        void onFilterComplete(AutoCompleteInput view, int count);
    }

    @Override
    public View focusSearch(int direction) {
        return super.focusSearch(direction);
    }

    public void onInputComplete() {
        if (scrollView != null) {
            Button button = scrollView.findViewById(R.id.import_mnemonic);
            if (button != null) {
                button.requestFocus();
            } else {
                button = scrollView.findViewById(R.id.verify_mnemonic);
                if (button != null) {
                    button.requestFocus();
                }
            }
            scrollView.smoothScrollTo(0, 0);
        }
    }

    private static class InputServant implements AdapterView.OnItemClickListener,
            AutoCompleteInput.FilterCompleteCallback, View.OnFocusChangeListener {

        private final AutoCompleteInput mHost;

        final boolean focusNextOnClick = true;
        final boolean autoCompleteOnMatch = true;

        private InputServant(@NonNull AutoCompleteInput host) {
            mHost = host;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (focusNextOnClick) {
                @SuppressLint("WrongConstant") final View v = mHost.focusSearch(View.FOCUS_FORWARD);
                if (isNextFocusableView(v)) {
                    v.requestFocus();
                } else {
                    mHost.onInputComplete();
                    try {
                        ViewGroup vg = (ViewGroup) mHost.getParent().getParent().getParent();
                        vg.findViewById(R.id.t).requestFocus();
                    } catch (Exception ignore){}

                    final InputMethodManager imm = (InputMethodManager) mHost.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(mHost.getWindowToken(), 0);
                    }
                }
            }
        }


        private boolean isNextFocusableView(View view) {
            if (view == null || view.getTag() == null) {
                return false;
            }
            int oldIndex = (int) mHost.getTag();
            int newIndex = (int) view.getTag();
            return view.isFocusableInTouchMode() && oldIndex < newIndex;
        }


        @Override
        public void onFilterComplete(AutoCompleteInput view, int count) {
            mHost.setTextColor(count > 0 ? sNormalTextColor : sWrongTextColor);

            if (autoCompleteOnMatch && count == 1) {
                mHost.onCommitCompletion(new CompletionInfo(0, 0, null));
                mHost.performCompletion();
            }
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                final String text = mHost.getText().toString();
                final boolean valid = sWordsSet.contains(text);
                mHost.setTextColor(valid ? sNormalTextColor : sWrongTextColor);
            } else {
                mHost.adjustScroll();
            }
        }


    }

}
