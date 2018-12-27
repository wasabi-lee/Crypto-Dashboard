package com.example.lemoncream.myapplication.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;

import com.example.lemoncream.myapplication.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddNoteActivity extends AppCompatActivity {

    private static final String TAG = AddNoteActivity.class.getSimpleName();
    public static final String EXTRA_NOTE_KEY = "extra_note_key";

    @BindView(R.id.add_note_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.add_note_edit)
    EditText mNoteEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        ButterKnife.bind(this);

        mToolbar.setTitle("Add a new note");
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        String note = getIntent().getStringExtra(EXTRA_NOTE_KEY);
        if (note != null) mNoteEdit.setText(note);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        String note = mNoteEdit.getText().toString();
        returnIntent.putExtra(NewCoinActivity.EXTRA_RESULT_NOTE_KEY, note.isEmpty() ? null : note);
        setResult(RESULT_OK, returnIntent);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
