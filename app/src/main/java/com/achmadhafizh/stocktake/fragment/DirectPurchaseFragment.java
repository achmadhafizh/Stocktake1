package com.achmadhafizh.stocktake.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.achmadhafizh.stocktake.R;
import com.achmadhafizh.stocktake.activity.MainActivity;
import com.achmadhafizh.stocktake.adapter.InquiryAdapter;
import com.achmadhafizh.stocktake.adapter.RecyclerItemTouchHelper;
import com.achmadhafizh.stocktake.helper.DatabaseManager;
import com.achmadhafizh.stocktake.helper.SettingsManager;
import com.achmadhafizh.stocktake.holder.InquiryViewHolder;
import com.achmadhafizh.stocktake.model.Stocktake;
import com.achmadhafizh.stocktake.utilities.CommonConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.achmadhafizh.stocktake.utilities.CommonConstant.ARG_ITEM;

public class DirectPurchaseFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private static final String TAG = DirectPurchaseFragment.class.getSimpleName();
    private SettingsManager settingsManager;
    public static DatabaseManager db;
    public static List<Stocktake> stocktakeList;
    private HashMap<String, String> settingPrefs;
    public static InquiryAdapter mAdapter;
    private String item, scannerID, storeNo;

    @BindView(R.id.rl_outer)
    RelativeLayout relativeLayout;

    @BindView(R.id.input_layout_nik)
    TextInputLayout inputLayoutNik;

    @BindView(R.id.input_nik)
    EditText inputNik;

    @BindView(R.id.input_layout_fixture)
    TextInputLayout inputLayoutFixture;

    @BindView(R.id.input_fixture)
    EditText inputFixture;

    @BindView(R.id.btn_new_fixture)
    Button btnNewFixture;

    @BindView(R.id.input_layout_bc1)
    TextInputLayout inputLayoutBc1;

    @BindView(R.id.input_bc1)
    EditText inputBc1;

    @BindView(R.id.input_layout_bc2)
    TextInputLayout inputLayoutBc2;

    @BindView(R.id.input_bc2)
    EditText inputBc2;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    public static TextView tv_qty_footer;

    @BindView(R.id.tv_barcode_footer)
    TextView tv_barcode_footer;

    @BindView(R.id.tv_fixture_footer)
    TextView tv_fixture_footer;

    public DirectPurchaseFragment() {}

    public static DirectPurchaseFragment newInstance(String section) {
        DirectPurchaseFragment fragment = new DirectPurchaseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ITEM, section);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = getArguments().getString(ARG_ITEM);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_direct_purchase, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        tv_qty_footer = view.findViewById(R.id.tv_qty_footer);

        setUpActionBar();

        db = new DatabaseManager(getActivity());
        settingsManager = new SettingsManager(getActivity());

        settingPrefs = settingsManager.getSettings();

        scannerID = settingPrefs.get(CommonConstant.SCANNER_ID);
        storeNo = settingPrefs.get(CommonConstant.STORE_NO);

        inputNik.addTextChangedListener(new MyTextWatcher(inputNik));
        inputFixture.addTextChangedListener(new MyTextWatcher(inputFixture));
        inputBc1.addTextChangedListener(new MyTextWatcher(inputBc1));
        inputBc2.addTextChangedListener(new MyTextWatcher(inputBc2));

        inputNik.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_ENTER ) {
                    if( event.getAction() == KeyEvent.ACTION_UP ) {
                        if (validateNik()) {
                            requestFocus(inputFixture);
                            inputFixture.setSelectAllOnFocus(true);
                            inputFixture.selectAll();
                            hideKeyboard(inputFixture);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        inputFixture.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_ENTER ) {
                    if( event.getAction() == KeyEvent.ACTION_UP ) {
                        if (validateFixture()) {
                            requestFocus(inputBc1);
                            inputBc1.setSelectAllOnFocus(true);
                            inputBc1.selectAll();
                            hideKeyboard(inputBc1);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        inputBc1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_ENTER ) {
                    if( event.getAction() == KeyEvent.ACTION_UP ) {
                        if (validateBc1()) {
                            requestFocus(inputBc2);
                            inputBc2.setSelectAllOnFocus(true);
                            inputBc2.selectAll();
                            hideKeyboard(inputBc2);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        inputBc2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_ENTER ) {
                    if( event.getAction() == KeyEvent.ACTION_UP ) {
                        if (validateBc2()) {
                            submitForm();
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        stocktakeList = new ArrayList<>();
        mAdapter = new InquiryAdapter(stocktakeList, true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        prepareListData();

        requestFocus(inputNik);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("ConstantConditions")
    private void setUpActionBar() {
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(item);
        MainActivity.mTitleTextView.setText(item);
    }

    @OnClick(R.id.btn_new_fixture)
    public void clearInputText() {
        requestFocus(inputNik);
        inputNik.setSelectAllOnFocus(true);
        inputNik.selectAll();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof InquiryViewHolder) {
            // get the removed item name to display it in snack bar
            final Integer id = stocktakeList.get(viewHolder.getAdapterPosition()).getId();
            final String type = stocktakeList.get(viewHolder.getAdapterPosition()).getType();
            final String fixture = stocktakeList.get(viewHolder.getAdapterPosition()).getFixture();
            final String bc1 = stocktakeList.get(viewHolder.getAdapterPosition()).getBc1();
            final String bc2 = stocktakeList.get(viewHolder.getAdapterPosition()).getBc2();
            final Integer qty = stocktakeList.get(viewHolder.getAdapterPosition()).getQty();
            final String nik = stocktakeList.get(viewHolder.getAdapterPosition()).getNik();
            final String scannerID = stocktakeList.get(viewHolder.getAdapterPosition()).getScanner();
            final Integer flag = stocktakeList.get(viewHolder.getAdapterPosition()).getFlag();

            // backup of removed item for undo purpose
            final Stocktake deletedItem = stocktakeList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            mAdapter.removeItem(viewHolder.getAdapterPosition());

            // remove from database
            Log.d(TAG, "Deleting ..");
            db.deleteStocktake(new Stocktake(id));

            // resummarize qty
            Integer total = getNumberFromString(tv_qty_footer.getText().toString().trim()) - qty;
            tv_qty_footer.setText(total  + " Pcs");

            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar.make(relativeLayout, bc1 + " removed from cart!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // undo is selected, restore the deleted item
                    mAdapter.restoreItem(deletedItem, deletedIndex);
                    Log.d(TAG, "Inserting ..");
                    db.addStocktake(new Stocktake(type, fixture, bc1, bc2, qty, nik, scannerID, flag));

                    // resummarize qty
                    Integer total = getNumberFromString(tv_qty_footer.getText().toString().trim()) + qty;
                    tv_qty_footer.setText(total  + " Pcs");
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    private int getNumberFromString(String word) {
        String regex = "[^0-9]+";
        Integer number = Integer.parseInt(word.toString().trim().replaceAll(regex,""));
        return number;
    }

    private void prepareScanBarcode() {
        requestFocus(inputBc1);
        inputBc1.setSelectAllOnFocus(true);
        inputBc1.selectAll();
    }

    public static void prepareListData() {
        Log.d("Reading all ", "Stocktake DP");
        List<Stocktake> items = db.getAllStocktakeDP();
        int total = 0;

        for (Stocktake stk : items) {
            total = total + stk.getQty();
            String log = "Id: " + stk.getId() + ", Type: " + stk.getType() + ", Fixture: " + stk.getFixture() +
                    ", Barcode 1: " + stk.getBc1() + ", Barcode 2: " + stk.getBc2() + ", Qty: " + stk.getQty() +
                    ", Nik: " + stk.getNik() + " ,Scanner: " + stk.getScanner() + " ,Flag: " + stk.getFlag();
            Log.d("All DP Records ", log);
        }

        tv_qty_footer.setText(total + " Pcs");
        stocktakeList.clear();
        stocktakeList.addAll(items);

        mAdapter.notifyDataSetChanged();
    }

    private void submitForm() {
        if (!validateNik()) {
            return;
        }

        if (!validateFixture()) {
            return;
        }

        if (!validateBc1()) {
            return;
        }

        if (!validateBc2()) {
            return;
        }

        String type = "DP";
        String nik = inputNik.getText().toString().trim();
        String fixture = inputFixture.getText().toString().trim();
        String bc1 = inputBc1.getText().toString().trim();
        String bc2 = inputBc2.getText().toString().trim();
        Integer qty = 1;

        int cnt = db.getStocktakeCountDP(fixture, bc1, bc2);
        Log.d("Count Stocktake DP ", cnt + "");

        if(cnt > 0) {
            Stocktake st = db.getSpesificStocktake(type, fixture, bc1, bc2);

            String data = "Id: " + st.getId() + " ,Type: " + st.getType() + " ,Fixture: " + st.getFixture() +
                    " ,Barcode 1: " + st.getBc1() + " ,Barcode 2: " + st.getBc2() + " ,Qty: " + st.getQty() +
                    " ,Nik: " + st.getNik() + " ,Scanner: " + st.getScanner() + " ,Flag: " + st.getFlag();
            Log.d("Spesific Records ", data);

            int id = st.getId();
            int stock = st.getQty();
            int total_qty = stock + qty;

            db.updateStocktake(new Stocktake(id, total_qty));
            Log.d("Update ", "Updating ..");
        } else {
            db.addStocktake(new Stocktake(type, fixture, bc1, bc2, qty, nik, scannerID.trim(), 0));
            Log.d("Insert ", "Inserting ..");
        }

        prepareListData();
        prepareScanBarcode();
        hideKeyboard(inputBc1);

        Snackbar snackbar = Snackbar.make(relativeLayout, " Record successfully added!", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    private boolean validateNik() {
        if(scannerID == null || scannerID.isEmpty()) {
            inputLayoutNik.setError(getString(R.string.err_msg_settings));
            requestFocus(inputNik);
            return false;
        } else if(storeNo == null || storeNo.isEmpty()) {
            inputLayoutNik.setError(getString(R.string.err_msg_settings));
            requestFocus(inputNik);
            return false;
        } else if (inputNik.getText().toString().trim().isEmpty()) {
            inputLayoutNik.setError(getString(R.string.err_msg_nik1));
            requestFocus(inputNik);
            return false;
        } else if (inputNik.getText().toString().trim().length() < 6) {
            inputLayoutNik.setError(getString(R.string.err_msg_nik2));
            requestFocus(inputNik);
            return false;
        } else {
            inputLayoutNik.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateFixture() {
        if(scannerID == null || scannerID.isEmpty()) {
            inputLayoutFixture.setError(getString(R.string.err_msg_settings));
            requestFocus(inputFixture);
            return false;
        } else if(storeNo == null || storeNo.isEmpty()) {
            inputLayoutFixture.setError(getString(R.string.err_msg_settings));
            requestFocus(inputFixture);
            return false;
        } else if (inputFixture.getText().toString().trim().isEmpty()) {
            inputLayoutFixture.setError(getString(R.string.err_msg_fixture1));
            requestFocus(inputFixture);
            return false;
        } else if (inputFixture.getText().toString().trim().length() < 9) {
            inputLayoutFixture.setError(getString(R.string.err_msg_fixture2));
            requestFocus(inputFixture);
            return false;
        } else if (!inputFixture.getText().toString().trim().substring(0, 2).equals(storeNo.trim())) {
            inputLayoutFixture.setError(getString(R.string.err_msg_fixture3));
            requestFocus(inputFixture);
            return false;
        } else {
            inputLayoutFixture.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateBc1() {
        if(scannerID == null || scannerID.isEmpty()) {
            inputLayoutBc1.setError(getString(R.string.err_msg_settings));
            requestFocus(inputBc1);
            return false;
        } else if(storeNo == null || storeNo.isEmpty()) {
            inputLayoutBc1.setError(getString(R.string.err_msg_settings));
            requestFocus(inputBc1);
            return false;
        } else if (inputBc1.getText().toString().trim().isEmpty()) {
            inputLayoutBc1.setError(getString(R.string.err_msg_bc11));
            requestFocus(inputBc1);
            return false;
        } else if (inputBc1.getText().toString().trim().length() < 13) {
            inputLayoutBc1.setError(getString(R.string.err_msg_bc12));
            requestFocus(inputBc1);
            return false;
        } else if (Integer.parseInt(inputBc1.getText().toString().trim().substring(0, 1)) < 5 ||
                Integer.parseInt(inputBc1.getText().toString().trim().substring(0, 1)) > 5) {
            inputLayoutBc1.setError(getString(R.string.err_msg_bc13));
            requestFocus(inputBc1);
            inputBc1.setSelectAllOnFocus(true);
            inputBc1.selectAll();
            hideKeyboard(inputBc1);
            return false;
        } else {
            inputLayoutBc1.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateBc2() {
        if(scannerID == null || scannerID.isEmpty()) {
            inputLayoutBc2.setError(getString(R.string.err_msg_settings));
            requestFocus(inputBc2);
            return false;
        } else if(storeNo == null || storeNo.isEmpty()) {
            inputLayoutBc2.setError(getString(R.string.err_msg_settings));
            requestFocus(inputBc2);
            return false;
        } else if (inputBc2.getText().toString().trim().isEmpty()) {
            inputLayoutBc2.setError(getString(R.string.err_msg_bc21));
            requestFocus(inputBc2);
            return false;
        } else if (inputBc2.getText().toString().trim().length() < 13) {
            inputLayoutBc2.setError(getString(R.string.err_msg_bc22));
            requestFocus(inputBc2);
            return false;
        } else if (Integer.parseInt(inputBc2.getText().toString().trim().substring(0, 1)) < 7 ||
                Integer.parseInt(inputBc2.getText().toString().trim().substring(0, 1)) > 7) {
            inputLayoutBc2.setError(getString(R.string.err_msg_bc23));
            requestFocus(inputBc2);
            inputBc2.setSelectAllOnFocus(true);
            inputBc2.selectAll();
            hideKeyboard(inputBc2);
            return false;
        } else {
            inputLayoutBc2.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void hideKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_nik:
                    validateNik();
                    break;
                case R.id.input_fixture:
                    validateFixture();
                    break;
                case R.id.input_bc1:
                    validateBc1();
                    break;
                case R.id.input_bc2:
                    validateBc2();
                    break;
            }
        }
    }

    public static void update_qty(int id, int qty) {
        db.updateStocktake(new Stocktake(id, qty));
        Log.d("Update ", "Updating ..");

        prepareListData();
    }
}
