package com.example.lemoncream.myapplication.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lemoncream.myapplication.Model.Deserializers.PriceDeserializer;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceFull;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;
import com.example.lemoncream.myapplication.Model.RealmModels.Exchange;
import com.example.lemoncream.myapplication.Model.RealmModels.Pair;
import com.example.lemoncream.myapplication.Model.RealmModels.TxHistory;
import com.example.lemoncream.myapplication.Network.GsonHelper;
import com.example.lemoncream.myapplication.Network.PriceService;
import com.example.lemoncream.myapplication.Network.RetrofitHelper;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Utils.Database.RealmIdAutoIncrementHelper;
import com.example.lemoncream.myapplication.Utils.Formatters.NumberFormatter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Retrofit;

public class NewCoinActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private static String TAG = NewCoinActivity.class.getSimpleName();
    public static String EXTRA_PAIR_KEY = "extra_pair_key";
    public static String EXTRA_TX_KEY = "extra_bag_key";
    public static String EXTRA_RESULT_EXCHANGE_KEY = "extra_exchange_key";
    public static String EXTRA_RESULT_NOTE_KEY = "extra_note_key";

    private static int REQUEST_CODE_EXCHANGE = 1;
    private static int REQUEST_CODE_NOTE = 2;

    @BindView(R.id.new_coin_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.new_coin_radio_group)
    RadioGroup mRadioGroupOrderType;

    @BindView(R.id.new_coin_main_linearLayout_1)
    LinearLayout mExchangeDetailLl;
    @BindView(R.id.new_coin_exchange_text)
    TextView mExchangeText;

    @BindView(R.id.new_coin_trade_price_edit)
    EditText mTradePriceEdit;
    @BindView(R.id.new_coin_current_price_text)
    TextView mCurrentPriceText;

    @BindView(R.id.new_coin_amount_edit)
    EditText mNewCoinAmountEdit;

    @BindView(R.id.new_coin_total_value_text)
    TextView mTotalValueText;

    @BindView(R.id.new_coin_main_linearLayout_5)
    LinearLayout mDateDetailLl;
    @BindView(R.id.new_coin_date_text)
    TextView mDateText;

    @BindView(R.id.new_coin_deduct_text)
    TextView mDeductFromText;
    @BindView(R.id.new_coin_deduct_switch)
    Switch mDeductSwitch;

    @BindView(R.id.new_coin_misc_linearLayout_2)
    LinearLayout mNoteDetailLl;
    @BindView(R.id.new_coin_note_preview_text)
    TextView mNotePreviewText;

    private Realm mRealm;
    private boolean editMode = false;
    private Bag mCurrentBag;
    private TxHistory mCurrentTx;
    private Pair mCurrentPair;
    private Exchange mCurrentExchange;
    private Date mSelectedDate;
    private String mNote;


    private static final DecimalFormat mDecimalFormat = new DecimalFormat("#.########");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_coin);
        ButterKnife.bind(this);

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mRealm = Realm.getDefaultInstance();

        configViews();
        parseInitialData();
        requestCurrentPrice();
    }


    private void configViews() {
        // ========== onClickListners =============
        mExchangeDetailLl.setOnClickListener(this);
        mDateDetailLl.setOnClickListener(this);
        mNoteDetailLl.setOnClickListener(this);

        // ========== TextWatchers & FocusListeners =============
        mTradePriceEdit.setOnFocusChangeListener(this);
        mTradePriceEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalValue(s.toString(), mNewCoinAmountEdit.getText().toString());
            }
        });
        mNewCoinAmountEdit.setOnFocusChangeListener(this);
        mNewCoinAmountEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalValue(s.toString(), mTradePriceEdit.getText().toString());
            }
        });

        // ========== Switch =============

    }


    private void parseInitialData() {
        try {
            unpackPairDetails();
            setActivityTitle();
            setCurrentExchangeName();
            setDate();

            if (editMode) {
                setOrderTypeOption();
                setPriceOptionDetails();
                setAdditionalDetails();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.error_message), Toast.LENGTH_SHORT).show();
        }
    }

    private void unpackPairDetails() {
        int receivedBagId = getIntent().getIntExtra(EXTRA_TX_KEY, -1);

        if (receivedBagId == -1) {
            // new coin mode
            editMode = false;
            String receivedPairKey = getIntent().getStringExtra(EXTRA_PAIR_KEY);
            mCurrentPair = mRealm.where(Pair.class).equalTo("pairName", receivedPairKey).findFirst();
            if (mCurrentPair.getExchanges() != null && mCurrentPair.getExchanges().size() > 0) {
                mCurrentExchange = mCurrentPair.getExchanges().get(0);
            }
        } else {
            // edit mode
            editMode = true;
            mCurrentTx = mRealm.where(TxHistory.class).equalTo("_id", receivedBagId).findFirst();
            mCurrentBag = mCurrentTx.getTxHolder();
            mCurrentPair = mCurrentBag.getTradePair();
            mCurrentExchange = mCurrentTx.getExchange();
        }
    }

    private void setOrderTypeOption() {
        String orderType = mCurrentTx.getOrderType();
        if (orderType.equals(TxHistory.ORDER_TYPE_BUY))
            ((RadioButton) findViewById(R.id.new_coin_radio_buy)).setChecked(true);
        if (orderType.equals(TxHistory.ORDER_TYPE_SELL))
            ((RadioButton) findViewById(R.id.new_coin_radio_sell)).setChecked(true);
        if (orderType.equals(TxHistory.ORDER_TYPE_WATCH))
            ((RadioButton) findViewById(R.id.new_coin_radio_watch)).setChecked(true);
    }

    private void setPriceOptionDetails() {
        String tradePriceStr = NumberFormatter.formatDecimals(mCurrentTx.getTradePrice());
        String amountStr = NumberFormatter.formatDecimals(mCurrentTx.getAmount());
        mTradePriceEdit.setText(tradePriceStr);
        mNewCoinAmountEdit.setText(amountStr);
        calculateTotalValue(tradePriceStr, amountStr);
    }

    private void setAdditionalDetails() {
        mDeductSwitch.setChecked(mCurrentTx.isDeductFromAnotherBag());
        mNotePreviewText.setText(mCurrentTx.getNote());
        mNote = mCurrentTx.getNote();
    }

    private void setActivityTitle() {
        String title = "-";
        if (mCurrentPair != null) {
            String[] pairData = mCurrentPair.getPairName().split("_");
            title = pairData[0] + "/" + pairData[1] + " transaction";
        }
        mToolbar.setTitle(title);
    }

    private void setCurrentExchangeName() {
        String exchange = "No exchange data found";
        if (mCurrentPair != null)
            exchange = mCurrentExchange != null ? mCurrentExchange.getName() : "No exchange data found";
        mExchangeText.setText(exchange);
    }

    private void setDate() {
        mSelectedDate = editMode ? mCurrentTx.getDate() : Calendar.getInstance().getTime();
        String formattedDate = DateFormat.getDateInstance().format(mSelectedDate);
        mDateText.setText(formattedDate);
    }


    private void calculateTotalValue(String firstParam, String secondParam) {
        float firstParamFloat = NumberFormatter.convertUserInputIntoFloat(firstParam);
        if (firstParamFloat < 0) {
            mTradePriceEdit.setText(0);
            mNewCoinAmountEdit.setText(0);
            mTotalValueText.setText(0);
        } else {
            float SecondParamFloat = NumberFormatter.convertUserInputIntoFloat(secondParam);
            mTotalValueText.setText(String.valueOf(mDecimalFormat.format(SecondParamFloat * firstParamFloat)));
        }
    }

    private void requestCurrentPrice() {
        Retrofit retrofit = RetrofitHelper
                .createRetrofitWithRxConverter(getResources().getString(R.string.base_url),
                        GsonHelper.createGsonBuilder(PriceFull.class, new PriceDeserializer()).create());
        PriceService coinListService = retrofit.create(PriceService.class);
        Single<PriceFull> priceRequest = coinListService.getSingleCurrentPrice(mCurrentPair.getfCoin().getSymbol(),
                mCurrentPair.gettCoin().getSymbol(),
                mCurrentExchange.getName());

        priceRequest.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::parsePriceData);
    }

    private void parsePriceData(PriceFull priceFull) {
        if (priceFull != null) {
            Float currentPrice = priceFull.getTsymPriceDetail().getPrice();
            mCurrentPriceText.setText(String.valueOf(mDecimalFormat.format(currentPrice)));
            mTradePriceEdit.setHint(String.valueOf(NumberFormatter.formatDecimals(currentPrice)));
        }
    }

    private void launchDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            mSelectedDate = newDate.getTime();
            mDateText.setText(DateFormat.getDateInstance().format(mSelectedDate));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.new_coin_main_linearLayout_1:
                // Change exchange
                Intent intent = new Intent(this, ChangeExchangeActivity.class);
                intent.putExtra(ChangeExchangeActivity.EXTRA_ACTIVITY_KEY, "new_coin");
                intent.putExtra(ChangeExchangeActivity.EXTRA_PAIR_NAME_KEY, mCurrentPair.getPairName());
                startActivityForResult(intent, REQUEST_CODE_EXCHANGE);
                break;
            case R.id.new_coin_main_linearLayout_5:
                // Launch DatePicker
                launchDatePicker();
                break;
            case R.id.new_coin_misc_linearLayout_2:
                // Add note;
                Intent noteIntent = new Intent(this, AddNoteActivity.class);
                noteIntent.putExtra(AddNoteActivity.EXTRA_NOTE_KEY, mNote);
                startActivityForResult(noteIntent, REQUEST_CODE_NOTE);
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        // This is for formatting the user input when the EditText loses focus (i.e. .3353 -> 0.3353)
        if (!hasFocus) {
            EditText v = (EditText) view;
            float inputFloat = NumberFormatter.convertUserInputIntoFloat(v.getText().toString());
            v.setText(mDecimalFormat.format(inputFloat));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EXCHANGE) {
            if (resultCode == RESULT_OK && data != null) {
                mCurrentExchange = mCurrentPair.getExchanges().where()
                        .equalTo("name", data.getStringExtra(EXTRA_RESULT_EXCHANGE_KEY))
                        .findFirst();
                if (mCurrentExchange != null) mExchangeText.setText(mCurrentExchange.getName());
                requestCurrentPrice();
            }
        } else if (requestCode == REQUEST_CODE_NOTE) {
            if (resultCode == RESULT_OK && data != null) {
                mNote = data.getStringExtra(EXTRA_RESULT_NOTE_KEY);
                mNotePreviewText.setText(mNote);
                Log.d(TAG, "onActivityResult: " + mNote);
            }
        }
    }

    private void saveThisTxHistory() {
        int id = mCurrentTx == null ? -1 : mCurrentTx.get_id();

        if (getOrderType().equals(TxHistory.ORDER_TYPE_WATCH)) {
            if (findBagForThisPair() == null) {
                mRealm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(createTxHistoryObject(id)));
            } else {
                Toast.makeText(this, "This pair is already on your watchlist or portfolio", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (!checkIfValidInput()) {
                Toast.makeText(this, "Please enter valid input", Toast.LENGTH_SHORT).show();
                return;
            }
            TxHistory txHistory = createTxHistoryObject(id);
            mRealm.executeTransaction(realm1 -> {
                try {
                    realm1.copyToRealmOrUpdate(txHistory);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        // Finish with the result intent.
        Intent intent = new Intent();
        intent.putExtra(SearchCoinActivity.EXTRA_SAVE_SUCCESSFUL_KEY, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean checkIfValidInput() {
        // Input is valid when both the price edit / amount edit are filled AND when the amount edit value is not 0.
        return !mTradePriceEdit.getText().toString().isEmpty() &&
                !mNewCoinAmountEdit.getText().toString().isEmpty() &&
                NumberFormatter.convertUserInputIntoFloat(mNewCoinAmountEdit.getText().toString()) != 0;
    }

    private TxHistory createTxHistoryObject(int id) {

        if (id == -1) id = RealmIdAutoIncrementHelper.generateItemId(TxHistory.class, "_id");

        TxHistory txHistory = new TxHistory();
        txHistory.set_id(id);
        txHistory.setOrderType(getOrderType());
        txHistory.setTxHolder(configBag());
        txHistory.setAmount(getOrderType().equals(TxHistory.ORDER_TYPE_WATCH) ? 0 :
                NumberFormatter.convertUserInputIntoFloat(mNewCoinAmountEdit.getText().toString()));
        txHistory.setTradePrice(getOrderType().equals(TxHistory.ORDER_TYPE_WATCH) ? 0 :
                NumberFormatter.convertUserInputIntoFloat(mTradePriceEdit.getText().toString()));
        txHistory.setExchange(mCurrentExchange);
        txHistory.setDate(mSelectedDate);
        txHistory.setDeductFromAnotherBag(mDeductSwitch.isChecked());
        txHistory.setDecutedPair(null); // TODO FIX HERE LATER
        txHistory.setNote(mNote);

        return txHistory;
    }

    private Bag findBagForThisPair() {
        return mCurrentBag != null ? mCurrentBag :
                mRealm.where(Bag.class)
                        .equalTo("tradePair.pairName", mCurrentPair.getPairName())
                        .findFirst();
    }

    private Bag configBag() {
        String orderType = getOrderType();
        float inputAmount = orderType.equals(TxHistory.ORDER_TYPE_WATCH) ? 0 : Math.abs(Float.valueOf(mNewCoinAmountEdit.getText().toString()));
        final float newBalance = orderType.equals(TxHistory.ORDER_TYPE_BUY) ? inputAmount : inputAmount * -1;
        boolean watchOnly = orderType.equals(TxHistory.ORDER_TYPE_WATCH);

        Bag foundBag = findBagForThisPair(); // TODO Fix here so that it can query portfolio
        if (foundBag == null) {
            Bag newBag = new Bag();
            newBag.set_id(RealmIdAutoIncrementHelper.generateItemId(Bag.class, "_id"));
            newBag.setTradePair(mCurrentPair);
            newBag.setBalance(newBalance);
            newBag.setDateAdded(mSelectedDate);
            newBag.setPortfolio(null);
            newBag.setWatchOnly(watchOnly);
            return newBag;
        } else {
            mRealm.executeTransaction(realm -> {
                // If this is in the edit mode, delete the initial balance from the total bag holdings
                // and add the new balance to it.
                float startingBalance = foundBag.getBalance();
                if (editMode) startingBalance += (mCurrentTx.getAmount() * -1);
                foundBag.setBalance(startingBalance + newBalance);
                if (foundBag.isWatchOnly()) foundBag.setWatchOnly(watchOnly);
            });
            return foundBag;
        }
    }

    private String getOrderType() {
        RadioButton selectedRadioBtn = findViewById(mRadioGroupOrderType.getCheckedRadioButtonId());
        String orderType = selectedRadioBtn.getText().toString();
        if (orderType.equals("BUY")) return TxHistory.ORDER_TYPE_BUY;
        if (orderType.equals("SELL")) return TxHistory.ORDER_TYPE_SELL;
        if (orderType.equals("WATCH")) return TxHistory.ORDER_TYPE_WATCH;
        return TxHistory.ORDER_TYPE_WATCH;
    }

    private void deleteThisTxHistory() {
        mRealm.executeTransaction(realm -> {
            mCurrentBag.setBalance(mCurrentBag.getBalance() + (mCurrentTx.getAmount() * -1));
            mCurrentTx.deleteFromRealm();
            finish();
        });
    }


    private void showDiscardWarningDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(NewCoinActivity.this).create();
        alertDialog.setTitle("Exit?");
        alertDialog.setMessage("The current edit will be discarded");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            finish();
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog.show();
    }

    private void showDeletionWarningDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(NewCoinActivity.this).create();
        alertDialog.setTitle("Delete?");
        alertDialog.setMessage("This transaction history will be deleted");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            deleteThisTxHistory();
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_coin, menu);
        if (!editMode) menu.findItem(R.id.menu_new_coin_delete).setVisible(false);
        return true;
    }

    @Override
    public void onBackPressed() {
        showDiscardWarningDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_new_coin_done:
                //TODO Save new tx history
                saveThisTxHistory();
                return true;
            case R.id.menu_new_coin_delete:
                showDeletionWarningDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
