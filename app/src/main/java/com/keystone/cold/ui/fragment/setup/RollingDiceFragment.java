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

package com.keystone.cold.ui.fragment.setup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.keystone.cold.R;
import com.keystone.cold.databinding.CommonModalBinding;
import com.keystone.cold.databinding.ModalWithTwoButtonBinding;
import com.keystone.cold.databinding.RollingDiceBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;

import java.util.Arrays;
import java.util.List;

public class RollingDiceFragment extends BaseFragment<RollingDiceBinding> {

    private DiceGridAdapter adapter;
    private final int INIT_ROLLS = 100;
    private byte[] rolls = new byte[INIT_ROLLS];
    private int currentPos;

    private final int numOfColumn = 10;
    private OnClickListener onClickListener = v -> {
        String tag = (String) v.getTag();
        if (tag.equals("X")) {
            if (currentPos != 0) {
                currentPos--;
                rolls[currentPos] = 0;
                adapter.notifyItemRangeChanged(currentPos, 2, 1);
            }
        } else {
            rolls[currentPos] = Byte.parseByte(tag);
            currentPos++;
            if (currentPos == rolls.length -1) {
                enlarge();
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemRangeChanged(currentPos - 1, 2, 1);
            }
        }
        int first = ((GridLayoutManager)mBinding.diceGrid.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        int last = ((GridLayoutManager)mBinding.diceGrid.getLayoutManager()).findLastCompletelyVisibleItemPosition();
        if (currentPos > last - numOfColumn) {
            mBinding.diceGrid.smoothScrollToPosition(Math.min(currentPos + INIT_ROLLS / 2, rolls.length));
        } else if(currentPos < first + numOfColumn) {
            int scrollTo =  (currentPos - INIT_ROLLS / 2);
            if (scrollTo < 0) return;
            mBinding.diceGrid.smoothScrollToPosition(currentPos - INIT_ROLLS / 2);
        }
    };

    private void enlarge() {
        byte[] enlarge = new byte[rolls.length + INIT_ROLLS];
        System.arraycopy(rolls, 0, enlarge, 0, rolls.length);
        rolls = enlarge;
    }


    @Override
    protected int setView() {
        return R.layout.rolling_dice;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.complete.setOnClickListener(v -> onCompleteClick());
        mBinding.setOnDiceRoll(onClickListener);
        setupDiceGrid();
    }

    private void onCompleteClick() {
        if (currentPos < 50) {
            ModalDialog dialog = new ModalDialog();
            CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),R.layout.common_modal,
                    null,false);
            binding.title.setText(R.string.rolling_not_enough);
            binding.subTitle.setText(mActivity.getString(R.string.rolling_hint_less_than_50,currentPos));
            binding.confirm.setText(R.string.know);
            binding.close.setVisibility(View.GONE);
            binding.confirm.setOnClickListener(v -> dialog.dismiss());
            dialog.setBinding(binding);
            dialog.show(mActivity.getSupportFragmentManager(),"");
        } else if(currentPos < 99) {
            ModalDialog dialog = new ModalDialog();
            ModalWithTwoButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),R.layout.modal_with_two_button,
                    null,false);
            binding.title.setText(R.string.keep_rolling);
            binding.subTitle.setText(mActivity.getString(R.string.rolling_hint_less_than_99,currentPos));
            binding.left.setText(R.string.confirm_rolling);
            binding.left.setOnClickListener(v -> {
                dialog.dismiss();
                navigateToGenerateMnemonic();
            });
            binding.right.setText(R.string.keep_rolling);
            binding.right.setOnClickListener(v -> dialog.dismiss());
            dialog.setBinding(binding);
            dialog.show(mActivity.getSupportFragmentManager(),"");
        } else {
            navigateToGenerateMnemonic();
        }
    }

    private void navigateToGenerateMnemonic() {
        Bundle data = new Bundle();
        data.putByteArray("dice_rolls", Arrays.copyOfRange(rolls,0, currentPos));
        data.putBoolean("use_dice", true);
        navigate(R.id.action_to_generateMnemonicFragment, data);
    }

    private void setupDiceGrid() {
        adapter = new DiceGridAdapter();
        GridLayoutManager layoutManager = new GridLayoutManager(mActivity,21);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position % numOfColumn == 0) {
                    return 3;
                }
                return 2;
            }
        });
        mBinding.diceGrid.setLayoutManager(layoutManager);
        mBinding.diceGrid.setAdapter(adapter);
        mBinding.diceGrid.addItemDecoration(new TableItemDecoration(mActivity) );
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    class DiceGridAdapter extends Adapter<DiceViewHolder> {

        @NonNull
        @Override
        public DiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mActivity).inflate(R.layout.dice_grid_item,parent,false);
            return new DiceViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull DiceViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position);
            } else {
                int value = (int) payloads.get(0);
                if (value != 0) {
                    if (rolls[position] != 0) {
                        holder.data.setText(rolls[position] + "");
                    } else {
                        holder.data.setText("");
                    }
                }
                if (currentPos == position && rolls[position] == 0) {
                    holder.data.setBackground(mActivity.getDrawable(R.drawable.dice_rect));
                } else {
                    holder.data.setBackground(null);
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull DiceViewHolder holder, int position) {
            holder.top.setText(String.valueOf(position + 1));
            holder.left.setText(String.valueOf(position / numOfColumn + 1));
            if (rolls[position] != 0) {
                holder.data.setText(String.valueOf(rolls[position]));
            } else {
                holder.data.setText("");
            }
            if (position == 0) {
                holder.top.setVisibility(View.VISIBLE);
                holder.left.setVisibility(View.VISIBLE);
            } else if(position < numOfColumn){
                holder.top.setVisibility(View.VISIBLE);
                holder.left.setVisibility(View.GONE);
            } else if(position % numOfColumn == 0) {
                holder.left.setVisibility(View.VISIBLE);
                holder.top.setVisibility(View.GONE);
            } else {
                holder.left.setVisibility(View.GONE);
                holder.top.setVisibility(View.GONE);
            }
            if (currentPos == position && rolls[position] == 0) {
                holder.data.setBackground(mActivity.getDrawable(R.drawable.dice_rect));
            } else {
                holder.data.setBackground(null);
            }
        }

        @Override
        public int getItemCount() {
            return rolls.length;
        }
    }

    class DiceViewHolder extends RecyclerView.ViewHolder {

        public TextView left;
        public TextView top;
        public TextView data;

        DiceViewHolder(@NonNull View itemView) {
            super(itemView);
            left = itemView.findViewById(R.id.left);
            top = itemView.findViewById(R.id.top);
            data = itemView.findViewById(R.id.data);
        }
    }

    class TableItemDecoration extends RecyclerView.ItemDecoration {

        private final Paint mPaint;
        private final int dividerWidth;

        TableItemDecoration(Context context) {
            mPaint = new Paint();
            mPaint.setColor(context.getColor(R.color.white40));
            dividerWidth = dp2px(context, 1);
            mPaint.setStrokeWidth(dividerWidth);
        }

        @Override
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            drawHorizontal(c, parent);
            drawVertical(c, parent);
        }

        void drawHorizontal(Canvas c, RecyclerView parent) {

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);

                int left = child.getLeft();
                final int right = child.getRight();
                final int top = child.getBottom();

                if ( i % numOfColumn == 0) {
                    left += dp2px(mActivity,14);
                }

                if (i + numOfColumn >= childCount) {
                    c.drawLine(left, top - dividerWidth, right, top - dividerWidth, mPaint);
                } else {
                    c.drawLine(left, top, right, top, mPaint);
                }

                //first row
                if (i < numOfColumn) {
                    c.drawLine(left, child.getTop() + (dividerWidth >> 1) + dp2px(mActivity,14), right,
                            child.getTop() + (dividerWidth >> 1) + dp2px(mActivity,14), mPaint);
                }
            }
        }

        void drawVertical(Canvas c, RecyclerView parent) {

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);

                int top = child.getTop();
                final int bottom = child.getBottom();
                final int left = child.getRight();

                if (i < numOfColumn) {
                    top += dp2px(mActivity,14);
                }

                //last column
                if ((i + 1) % numOfColumn == 0) {
                    c.drawLine(left - dividerWidth, top,
                            left - dividerWidth, bottom, mPaint);
                } else {
                    c.drawLine(left, top, left, bottom, mPaint);
                }

                //first column
                if (i % numOfColumn == 0) {
                    c.drawLine(child.getLeft() + (dividerWidth >> 1) + dp2px(mActivity,14), top,
                            child.getLeft() + (dividerWidth >> 1) + dp2px(mActivity,14), bottom, mPaint);
                }
            }
        }


        private int dp2px(Context context, int dp) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    dp, context.getResources().getDisplayMetrics());
        }
    }
}
