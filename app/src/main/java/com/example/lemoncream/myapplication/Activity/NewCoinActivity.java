package com.example.lemoncream.myapplication.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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

import com.example.lemoncream.myapplication.Model.GsonModels.Price;
import com.example.lemoncream.myapplication.Model.Deserializers.PriceDeserializer;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;
import com.example.lemoncream.myapplication.Model.RealmModels.Exchange;
import com.example.lemoncream.myapplication.Model.RealmModels.Pair;
import com.example.lemoncream.myapplication.Model.RealmModels.TxHistory;
import com.example.lemoncream.myapplication.Network.GsonHelper;
import com.example.lemoncream.myapplication.Network.PriceService;
import com.example.lemoncream.myapplication.Network.RetrofitHelper;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Utils.Database.RealmIdAutoIncrementHelper;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Retrofit;

public class NewCoinActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private static String TAG = NewCoinActivity.class.getSimpleName();
    public static String EXTRA_PAIR_KEY = "extra_pair_key";
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
                onUserInputChanged(s.toString(), mNewCoinAmountEdit.getText().toString());
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
                onUserInputChanged(s.toString(), mTradePriceEdit.getText().toString());
            }
        });

        // ========== Switch =============

    }


    private void parseInitialData() {
        unpackPairDetails();
        setActivityTitle();
        setCurrentExchangeName();
        setTodaysDate();
    }

    private void unpackPairDetails() {
        String receivedPairKey = getIntent().getStringExtra(EXTRA_PAIR_KEY);
        mCurrentPair = mRealm.where(Pair.class).equalTo("pairName", receivedPairKey).findFirst();
        if (mCurrentPair.getExchanges() != null && mCurrentPair.getExchanges().size() > 0) {
            mCurrentExchange = mCurrentPair.getExchanges().get(0);
        }
    }

    private void setActivityTitle() {
        String title = "-";
        if (mCurrentPair != null) {
            String[] pairData = getIntent().getStringExtra(EXTRA_PAIR_KEY).split("_");
            title = "Add a " + pairData[0] + "/" + pairData[1] + " transaction";
        }
        mToolbar.setTitle(title);
    }

    private void setCurrentExchangeName() {
        String exchange = "No exchange data found";
        if (mCurrentPair != null)
            exchange = mCurrentExchange != null ? mCurrentExchange.getName() : "No exchange data found";
        mExchangeText.setText(exchange);
    }

    private void setTodaysDate() {
        Date todaysDate = Calendar.getInstance().getTime();
        mSelectedDate = todaysDate;
        String formattedDate = DateFormat.getDateInstance().format(todaysDate);
        mDateText.setText(formattedDate);
    }

    private static float convertUserInputIntoFloat(String inputString) {
        float inputFloat;
        try {
            inputFloat = Float.valueOf(inputString);
        } catch (NumberFormatException e) {
            inputFloat = 0;
        }
        return inputFloat >= 0 ? inputFloat : 0;
    }

    private void onUserInputChanged(String firstParam, String secondParam) {
        float firstParamFloat = convertUserInputIntoFloat(firstParam);
        if (firstParamFloat < 0) {
            mTradePriceEdit.setText(0);
            mNewCoinAmountEdit.setText(0);
            mTotalValueText.setText(0);
        } else {
            float SecondParamFloat = convertUserInputIntoFloat(secondParam);
            mTotalValueText.setText(String.valueOf(mDecimalFormat.format(SecondParamFloat * firstParamFloat)));
        }
    }

    private void requestCurrentPrice() {
        Retrofit retrofit = RetrofitHelper
                .createRetrofitWithRxConverter(getResources().getString(R.string.base_url),
                        GsonHelper.createGsonBuilder(Price.class, new PriceDeserializer()).create());
        PriceService coinListService = retrofit.create(PriceService.class);
        Observable<Price> priceRequest = coinListService.getCurrentPrice(mCurrentPair.getfCoin().getSymbol(),
                mCurrentPair.gettCoin().getSymbol(),
                mCurrentExchange.getName());

        priceRequest.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Price>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe: ");
                    }

                    @Override
                    public void onNext(Price price) {
                        Log.d(TAG, "onNext: ");
                        parsePriceData(price);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: ");
                        //TODO Handle error
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    private void parsePriceData(Price priceData) {
        //TODO Handle null error
        if (priceData != null) {
            String currentTsym = mCurrentPair.gettCoin().getSymbol();
            Float currentPrice = priceData.getPrices().get(currentTsym);
            mCurrentPriceText.setText(String.valueOf(mDecimalFormat.format(currentPrice)));
            mTradePriceEdit.setHint(String.valueOf(mDecimalFormat.format(currentPrice)));
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
                intent.putExtra(ChangeExchangeActivity.EXTRA_EXCHANGE_KEY, mCurrentPair.getPairName());
                startActivityForResult(intent, REQUEST_CODE_EXCHANGE);
                break;
            case R.id.new_coin_main_linearLayout_5:
                // Launch DatePicker
                launchDatePicker();
                break;
            case R.id.new_coin_misc_linearLayout_2:
                // Add note;
                startActivityForResult(new Intent(this, AddNoteActivity.class), REQUEST_CODE_NOTE);
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        // This is for formatting the user input when the EditText loses focus (i.e. .3353 -> 0.3353)
        if (!hasFocus) {
            EditText v = (EditText) view;
            float inputFloat = convertUserInputIntoFloat(v.getText().toString());
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
        TxHistory txHistory = createTxHistoryObject();
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            try {
                realm1.copyToRealmOrUpdate(txHistory);
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Error e) {
                e.printStackTrace();
            }
        });
        // Finish with the result intent.
        Intent intent = new Intent();
        intent.putExtra(SearchCoinActivity.EXTRA_SAVE_SUCCESSFUL_KEY, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    private TxHistory createTxHistoryObject() {
        TxHistory txHistory = new TxHistory();
        txHistory.set_id(RealmIdAutoIncrementHelper.generateItemId(TxHistory.class, "_id"));
        txHistory.setOrderType(getOrderType());
        txHistory.setTxHolder(findBagForThisTx()); // TODO FIX HERE LATER
        txHistory.setAmount(convertUserInputIntoFloat(mNewCoinAmountEdit.getText().toString()));
        txHistory.setTradePrice(convertUserInputIntoFloat(mTradePriceEdit.getText().toString()));
        txHistory.setExchange(mCurrentExchange);
        txHistory.setDate(mSelectedDate);
        txHistory.setDeductFromAnotherBag(mDeductSwitch.isChecked());
        txHistory.setDecutedPair(null); // TODO FIX HERE LATER
        return txHistory;
    }

    private Bag findBagForThisTx() {
        Bag foundBag = mRealm.where(Bag.class)
                .equalTo("tradePair.pairName", mCurrentPair.getPairName())
                .findFirst(); // TODO Fix here so that it can query portfolio
        if (foundBag == null) {
            foundBag = new Bag();
            foundBag.set_id(RealmIdAutoIncrementHelper.generateItemId(Bag.class, "_id"));
            foundBag.setTradePair(mCurrentPair);
            foundBag.setBalance(foundBag.getBalance() + convertUserInputIntoFloat(mNewCoinAmountEdit.getText().toString()));
            foundBag.setDateAdded(mSelectedDate);
            foundBag.setPortfolio(null); // TODO Null for now
        }
        return foundBag;
    }

    private String getOrderType() {
        RadioButton selectedRadioBtn = findViewById(mRadioGroupOrderType.getCheckedRadioButtonId());
        String orderType = selectedRadioBtn.getText().toString();
        if (orderType.equals("BUY")) return "order_buy";
        if (orderType.equals("SELL")) return "order_buy";
        if (orderType.equals("WATCH")) return "order_watch";
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_coin, menu);
        return true;
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
