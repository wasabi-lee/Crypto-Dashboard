package com.example.lemoncream.myapplication.Utils.Formatters;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Wasabi on 3/20/2018.
 */

public class AlertTextWatcher implements TextWatcher {

    private static final String TAG = AlertTextWatcher.class.getSimpleName();

    private EditText mMoreEdit, mLessEdit;
    private TextView mSummaryText;

    private String iWillInformYouWhen = "I will inform you when ";
    private String pairName = "";
    private String is = "is ";
    private String moreThan = "more than ";
    private String or = "or ";
    private String lessThan = "less than ";
    private String in = "in ";
    private String exchangeName = "";


    public AlertTextWatcher(EditText mMoreEdit, EditText mLessEdit, TextView mSummaryText) {
        this.mMoreEdit = mMoreEdit;
        this.mLessEdit = mLessEdit;
        this.mSummaryText = mSummaryText;
    }

    public AlertTextWatcher(EditText mMoreEdit, EditText mLessEdit, TextView mSummaryText, String pairName, String exchangeName) {
        this.mMoreEdit = mMoreEdit;
        this.mLessEdit = mLessEdit;
        this.mSummaryText = mSummaryText;
        this.pairName = pairName;
        this.exchangeName = exchangeName;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        Log.d(TAG, "onTextChanged: ");
        createSummaryText();
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    public void createSummaryText() {
        String newSummary = "";

        if (checkIfInputExists()) {

            newSummary += (iWillInformYouWhen + pairName + " " + is);

            if (checkIfInputExists(mMoreEdit)) {
                String newMoreInput = mMoreEdit.getText().toString();
                newSummary += (moreThan + newMoreInput + " ");
            }

            if (checkIfInputExists(mMoreEdit) && checkIfInputExists(mLessEdit)) newSummary += or;

            if (checkIfInputExists(mLessEdit)) {
                String newLessInput = mLessEdit.getText().toString();
                newSummary += (lessThan + newLessInput + " ");
            }

            newSummary += (in + exchangeName + ".");

        } else {
            newSummary = "Please enter the alert conditions.";
        }
        mSummaryText.setText(newSummary);
    }

    private boolean checkIfInputExists() {
        return !mMoreEdit.getText().toString().isEmpty() || !mLessEdit.getText().toString().isEmpty();
    }

    private boolean checkIfInputExists(EditText edit) {
        return !edit.getText().toString().isEmpty();
    }

    private boolean checkIfInputISValid(String input) {
        return Float.valueOf(input) >= 0;
    }

    public EditText getmMoreEdit() {
        return mMoreEdit;
    }

    public void setmMoreEdit(EditText mMoreEdit) {
        this.mMoreEdit = mMoreEdit;
    }

    public EditText getmLessEdit() {
        return mLessEdit;
    }

    public void setmLessEdit(EditText mLessEdit) {
        this.mLessEdit = mLessEdit;
    }

    public TextView getmSummaryText() {
        return mSummaryText;
    }

    public void setmSummaryText(TextView mSummaryText) {
        this.mSummaryText = mSummaryText;
    }

    public String getPairName() {
        return pairName;
    }

    public void setPairName(String pairName) {
        this.pairName = pairName;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }
}
