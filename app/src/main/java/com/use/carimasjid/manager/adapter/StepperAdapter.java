package com.use.carimasjid.manager.adapter;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;
import com.use.carimasjid.manager.fragment.StepOneFragment;
import com.use.carimasjid.manager.fragment.StepThreeFragment;
import com.use.carimasjid.manager.fragment.StepTwoFragment;

public class StepperAdapter extends AbstractFragmentStepAdapter {

    private static final String CURRENT_STEP_POSITION_KEY = "messageResourceId";

    public StepperAdapter(FragmentManager fm, Context context) {
        super(fm, context);
    }

    @Override
    public Step createStep(int position) {
        switch (position){
            case 0:
                final StepOneFragment step1 = new StepOneFragment();
                Bundle b1 = new Bundle();
                b1.putInt(CURRENT_STEP_POSITION_KEY, position);
                step1.setArguments(b1);
                return step1;
            case 1:
                final StepTwoFragment step2 = new StepTwoFragment();
                Bundle b2 = new Bundle();
                b2.putInt(CURRENT_STEP_POSITION_KEY, position);
                step2.setArguments(b2);
                return step2;
            case 2:
                final StepThreeFragment step3 = new StepThreeFragment();
                Bundle b3 = new Bundle();
                b3.putInt(CURRENT_STEP_POSITION_KEY, position);
                step3.setArguments(b3);
                return step3;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
    @NonNull
    @Override
    public StepViewModel getViewModel(@IntRange(from = 0) int position) {
        //Override this method to set Step title for the Tabs, not necessary for other stepper types
        switch (position){
            case 0:
                return new StepViewModel.Builder(context)
                        .setTitle("Step 1") //can be a CharSequence instead
                        .create();
            case 1:
                return new StepViewModel.Builder(context)
                        .setTitle("Step 2") //can be a CharSequence instead
                        .create();
            case 2:
                return new StepViewModel.Builder(context)
                        .setTitle("Step 3") //can be a CharSequence instead
                        .create();
        }
        return null;
    }
}
