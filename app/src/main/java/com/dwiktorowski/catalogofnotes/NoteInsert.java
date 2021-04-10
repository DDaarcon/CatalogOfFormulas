package com.dwiktorowski.catalogofnotes;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class NoteInsert extends AppCompatActivity {

    EditText mainEditText;

    private static Character[] allowedCharactersInName = {
            'a', 'b', 'c' ,'d', 'e', 'f' ,'g', 'h', 'i' ,'j', 'k', 'l' ,'m', 'n', 'o' ,'p', 'q', 'r', 's' ,'t', 'u', 'w' ,'v', 'x', 'y', 'z',
            'A', 'B', 'C' ,'C', 'E', 'F' ,'G', 'H', 'I' ,'J', 'K', 'L' ,'M', 'N', 'O' ,'P', 'Q', 'R', 'S' ,'T', 'U', 'W' ,'V', 'X', 'Y', 'Z',
            '0', '1', '2' ,'3', '4', '5' ,'6', '7', '8' ,'9', '_', '-'};

    private String lastSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_insert);
        mainEditText = findViewById(R.id.editNote);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            mainEditText.setText(bundle.getCharSequence("text").toString());
        }
        lastSaved = mainEditText.getText().toString();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putCharSequence("main_text", mainEditText.getText().toString());

        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mainEditText.setText(savedInstanceState.getCharSequence("main_text"));

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if ((!lastSaved.equals(mainEditText.getText().toString()))) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setTitle(getResources().getString(R.string.exitQMsg));

            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.yesMsg), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    NoteInsert.super.onBackPressed();
                }
            })
                    .setNegativeButton(getResources().getString(R.string.backMsg), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();

            alertDialog.show();
        }else
            super.onBackPressed();
    }

    @Nullable
    public static Character allowedName(String text_){
        boolean found;
        for (int i = 0; i < text_.length(); i++) {
            found = false;
            for (int j = 0; j < allowedCharactersInName.length; j++){
                if (text_.charAt(i) == allowedCharactersInName[j]) {
                    found = true;
                    break;
                }
            }
            if (!found)
                return text_.charAt(i);
        }
        return null;
    }
    public void saveBtn(View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(getResources().getString(R.string.insertNameMsg));

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        final View view1 = layoutInflater.inflate(R.layout.message_edit_text, null);
        //final EditText nameEnter = findViewById(R.id.messageEditText);

        alertDialogBuilder.setView(view1)
                .setPositiveButton(getResources().getString(R.string.saveMsg), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText nameEnter = view1.findViewById(R.id.messageEditText);
                        if (!TextUtils.isEmpty(nameEnter.getText().toString()) && allowedName(nameEnter.getText().toString()) == null) {
                            Save(nameEnter.getText().toString());
                            dialog.cancel();
                        }else{
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.unexpectedCharactersNameMsg), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(getResources().getString(R.string.back2Msg), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void Save(String filename_){
        try {
            File parentPath = new File(MainActivity.getCurrentDirectory().getPath());
            FileOutputStream outputStreamWriter = new FileOutputStream(new File(parentPath, filename_ + ".txt"));
            outputStreamWriter.write(mainEditText.getText().toString().getBytes());
            outputStreamWriter.close();
            lastSaved = mainEditText.getText().toString();
            Toast.makeText(this, getResources().getString(R.string.savedMsg), Toast.LENGTH_SHORT).show();
        }catch (Throwable t){
            Toast.makeText(this, getResources().getString(R.string.savingErrorMsg) + t.toString(), Toast.LENGTH_SHORT).show();
        }
    }

}
