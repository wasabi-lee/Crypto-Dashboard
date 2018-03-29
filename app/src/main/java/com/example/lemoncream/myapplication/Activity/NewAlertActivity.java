package com.example.lemoncream.myapplication.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lemoncream.myapplication.Model.Deserializers.PriceDeserializer;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceFull;
import com.example.lemoncream.myapplication.Model.RealmModels.Alert;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;
import com.example.lemoncream.myapplication.Model.RealmModels.Exchange;
import com.example.lemoncream.myapplication.Model.RealmModels.Pair;
import com.example.lemoncream.myapplication.Model.RealmModels.TxHistory;
import com.example.lemoncream.myapplication.Network.PriceService;
import com.example.lemoncream.myapplication.Network.RetrofitHelper;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Utils.Database.RealmIdAutoIncrementHelper;
import com.example.lemoncream.myapplication.Utils.Formatters.AlertTextWatcher;
import com.example.lemoncream.myapplication.Utils.Formatters.NumberFormatter;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.Sort;
import retrofit2.Retrofit;

import static com.example.lemoncream.myapplication.Network.GsonHelper.*;

public class NewAlertActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private static final String TAG = NewAlertActivity.class.getSimpleName();
    public static final String EXTRA_BAG_ID_KEY = "extra_bag_id_key";
    public static final String EXTRA_ALERT_ID_KEY = "extra_alert_id_key";
    public static final String EXTRA_RESULT_EXCHANGE_KEY = "extra_result_exchange_key";
    public static final int REQUEST_CODE_NEW_ALERT_EXCHANGE = 553;

    @BindView(R.id.new_alert_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.new_alert_radio_group)
    RadioGroup mOneTimeRadioGroup;
    @BindView(R.id.new_alert_linearlayout_1_1)
    LinearLayout mExchangeLayout;
    @BindView(R.id.new_alert_more_exchanges_image_view)
    ImageView mExchangeArrow;
    @BindView(R.id.new_alert_exchange_text)
    TextView mExchangeText;
    @BindView(R.id.new_alert_trade_pair_text)
    TextView mTradePairText;
    @BindView(R.id.new_alert_more_than_edit_text)
    EditText mMoreThanText;
    @BindView(R.id.new_alert_less_than_edit_text)
    EditText mLessThanText;
    @BindView(R.id.new_alert_summary_text)
    TextView mSummaryText;

    private boolean editMode = false;

    private int mBagId;
    private int mAlertId;
    private Realm mRealm;
    private Alert mCurrentAlert;
    private Bag mCurrentBag;
    private Exchange mExchange;
    private String mPairName;
    private String mExchangeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_alert);
        ButterKnife.bind(this);

        mToolbar.setTitle("Add a new alert");
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mRealm = Realm.getDefaultInstance();

        setListeners();
        loadInitialData();
        parseInitialData();
        configEdittext();

    }

    private void setListeners() {
        mExchangeLayout.setOnClickListener(this);
        mExchangeArrow.setOnClickListener(this);
    }


    private void loadInitialData() {
        try {
            mAlertId = getIntent().getIntExtra(EXTRA_ALERT_ID_KEY, -1);
            if (mAlertId != -1) {
                editMode = true;
                mCurrentAlert = mRealm.where(Alert.class).equalTo("_id", mAlertId).findFirst();
                mCurrentBag = mCurrentAlert.getBag();
                mBagId = mCurrentBag.get_id();
            } else {
                editMode = false;
                mBagId = getIntent().getIntExtra(EXTRA_BAG_ID_KEY, -1);
                mCurrentBag = mRealm.where(Bag.class).equalTo("_id", mBagId).findFirst();
            }
        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.error_message), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void parseInitialData() {
        try {
            if (mCurrentBag == null) return;
            Pair tradePair = mCurrentBag.getTradePair();
            mPairName = findPairName(tradePair);
            mExchange = getExchange();
            mExchangeName = mExchange == null ? "" : mExchange.getName();
            requestCurrentPrice(tradePair, mExchange);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mExchangeText.setText(mExchangeName);
        mTradePairText.setText(mPairName);
        if (editMode) {
            if (mCurrentAlert.isOneTime()) {
                ((RadioButton) findViewById(R.id.new_alert_one_time_radio_button)).setChecked(true);
            } else {
                ((RadioButton) findViewById(R.id.new_alert_persistant_radio_button)).setChecked(true);
            }
            if (mCurrentAlert.getMoreThan() != -1)
                mMoreThanText.setText(NumberFormatter.formatDecimals(mCurrentAlert.getMoreThan()));
            if (mCurrentAlert.getLessThan() != -1)
                mLessThanText.setText(NumberFormatter.formatDecimals(mCurrentAlert.getLessThan()));
        }
    }

    private void configEdittext() {
        AlertTextWatcher alertTextWatcher = new AlertTextWatcher(mMoreThanText, mLessThanText, mSummaryText, mPairName, mExchangeName);
        mMoreThanText.setOnFocusChangeListener(this);
        mMoreThanText.addTextChangedListener(alertTextWatcher);
        mLessThanText.setOnFocusChangeListener(this);
        mLessThanText.addTextChangedListener(alertTextWatcher);

        if (editMode) alertTextWatcher.createSummaryText();
    }


    private String findPairName(Pair tradePair) {
        if (tradePair == null) return "";
        String fsym = "", tsym = "";
        try {
            fsym = tradePair.getfCoin().getSymbol();
            tsym = tradePair.gettCoin().getSymbol();
            if (fsym == null) fsym = "";
            if (tsym == null) tsym = "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fsym + "/" + tsym;
    }

    private Exchange getExchange() {
        return editMode ?
                (mCurrentAlert == null ? findLatestExchange() : mCurrentAlert.getExchange()) :
                findLatestExchange();
    }

    private Exchange findLatestExchange() {
        TxHistory latestTx = mRealm.where(TxHistory.class)
                .equalTo("txHolder._id", mBagId)
                .sort("date", Sort.DESCENDING)
                .findFirst();
        if (latestTx == null || latestTx.getExchange() == null) return null;
        return latestTx.getExchange();
    }

    private void requestCurrentPrice(Pair tradePair, Exchange exchange) {
        if (tradePair == null || exchange == null) return;
        Retrofit retrofit = RetrofitHelper
                .createRetrofitWithRxConverter(getResources().getString(R.string.base_url),
                        createGsonBuilder(PriceFull.class, new PriceDeserializer()).create());
        PriceService coinListService = retrofit.create(PriceService.class);
        Single<PriceFull> priceRequest = coinListService.getSingleCurrentPrice(tradePair.getfCoin().getSymbol(),
                tradePair.gettCoin().getSymbol(),
                exchange.getName());

        priceRequest.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(priceFull -> {
                    if (priceFull == null || priceFull.getTsymPriceDetail() == null) return;
                    String currentPriceStr = NumberFormatter.formatDecimals(priceFull.getTsymPriceDetail().getPrice());
                    mMoreThanText.setHint(currentPriceStr);
                    mLessThanText.setHint(currentPriceStr);
                });
    }


    private void launchExchangeSettingActivity() {
        if (mCurrentBag != null && mCurrentBag.getTradePair() != null) {
            Intent intent = new Intent(this, ChangeExchangeActivity.class);
            intent.putExtra(ChangeExchangeActivity.EXTRA_ACTIVITY_KEY, "new_alert");
            intent.putExtra(ChangeExchangeActivity.EXTRA_PAIR_NAME_KEY, mCurrentBag.getTradePair().getPairName());
            startActivityForResult(intent, REQUEST_CODE_NEW_ALERT_EXCHANGE);
        }
    }

    private void enableEditting(View view, boolean b) {
        if (editMode) return;
        EditText editText = (EditText) view;
        if (b) {
            editText.setText(editText.getHint());
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        } else {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    private void saveThisAlert() {

        if (!checkIfInputValid()) {
            Toast.makeText(this, "Please enter a valid input", Toast.LENGTH_SHORT).show();
            return;
        }

        int id = mCurrentAlert != null ? mCurrentAlert.get_id() :
                RealmIdAutoIncrementHelper.generateItemId(Alert.class, "_id");
        Alert newAlert = createNewAlert(id);

        mRealm.executeTransaction(realm -> {
            realm.copyToRealmOrUpdate(newAlert);
            Intent returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);
            finish();
        });
    }

    private Alert createNewAlert(int id) {
        String moreInput = mMoreThanText.getText().toString();
        String lessInput = mLessThanText.getText().toString();
        float more = moreInput.isEmpty() ? -1 : NumberFormatter.convertUserInputIntoFloat(moreInput);
        float less = lessInput.isEmpty() ? -1 : NumberFormatter.convertUserInputIntoFloat(lessInput);

        Alert newAlert = new Alert();
        newAlert.set_id(id);
        newAlert.setBag(mCurrentBag);
        newAlert.setExchange(mExchange);
        newAlert.setActive(true);
        newAlert.setOneTime(isOneTime());
        newAlert.setMoreThan(more);
        newAlert.setLessThan(less);

        return newAlert;
    }

    private boolean checkIfInputValid() {
        String moreInput = mMoreThanText.getText().toString();
        String lessInput = mLessThanText.getText().toString();
        if (!moreInput.isEmpty() && !lessInput.isEmpty()) return true;
        if (!moreInput.isEmpty()) return NumberFormatter.convertUserInputIntoFloat(moreInput) > 0;
        if (!lessInput.isEmpty()) return NumberFormatter.convertUserInputIntoFloat(lessInput) > 0;
        return false;
    }

    private boolean isOneTime() {
        RadioButton selectedRadioButton = findViewById(mOneTimeRadioGroup.getCheckedRadioButtonId());
        return selectedRadioButton.getText().toString().equals("ONE-TIME");
    }

    private void deleteThisAlert() {
        mRealm.executeTransaction(realm -> {
            mCurrentAlert.deleteFromRealm();
            finish();
        });
    }

    private void showDiscardWarningDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(NewAlertActivity.this).create();
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
        AlertDialog alertDialog = new AlertDialog.Builder(NewAlertActivity.this).create();
        alertDialog.setTitle("Delete?");
        alertDialog.setMessage("This alert will be deleted");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            deleteThisAlert();
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NEW_ALERT_EXCHANGE) {
            if (resultCode == RESULT_OK && data != null) {
                String exchangeName = data.getStringExtra(EXTRA_RESULT_EXCHANGE_KEY);
                mExchange = mRealm.where(Exchange.class).equalTo("name", exchangeName).findFirst();
                mExchangeText.setText(exchangeName == null ? "" : exchangeName);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.new_alert_linearlayout_1_1:
                launchExchangeSettingActivity();
                break;
            case R.id.new_alert_more_exchanges_image_view:
                launchExchangeSettingActivity();
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        switch (view.getId()) {
            case R.id.new_alert_more_than_edit_text:
                Log.d(TAG, "onFocusChange: more than");
                enableEditting(view, b);
                break;
            case R.id.new_alert_less_than_edit_text:
                Log.d(TAG, "onFocusChange: less than");
                enableEditting(view, b);
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mRealm.isClosed()) mRealm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_alert, menu);
        if (!editMode) menu.findItem(R.id.menu_new_alert_delete).setVisible(false);
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
            case R.id.menu_new_alert_done:
                // Save new alert
                saveThisAlert();
                return true;
            case R.id.menu_new_alert_delete:
                showDeletionWarningDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
