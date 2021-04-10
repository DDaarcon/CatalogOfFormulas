package com.dwiktorowski.catalogofnotes;

import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.OutputStreamWriter;

import io.github.kexanie.library.MathView;

public class NoteWithFormulaInsert extends AppCompatActivity {

    EditText mainEditText;
    MathView mainViewText;

    private Character[] allowedCharactersInName = {
            'a', 'b', 'c' ,'d', 'e', 'f' ,'g', 'h', 'i' ,'j', 'k', 'l' ,'m', 'n', 'o' ,'p', 'q', 'r', 's' ,'t', 'u', 'w' ,'v', 'x', 'y', 'z',
            'A', 'B', 'C' ,'C', 'E', 'F' ,'G', 'H', 'I' ,'J', 'K', 'L' ,'M', 'N', 'O' ,'P', 'Q', 'R', 'S' ,'T', 'U', 'W' ,'V', 'X', 'Y', 'Z',
            '0', '1', '2' ,'3', '4', '5' ,'6', '7', '8' ,'9', '_', '-'};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_with_formula_insert);
        mainEditText = findViewById(R.id.editNoteWithForm);
        mainViewText = findViewById(R.id.showNoteWithForm);

        Toast.makeText(this, "Eksperymentalny sposób wprowadzania danych", Toast.LENGTH_SHORT).show();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            mainEditText.setText(bundle.getCharSequence("text_formula").toString());
            mainViewText.setText(mainEditText.getText().toString());
        }

        mainEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mainViewText.setText(s.toString());
            }
        });
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

    @Nullable
    private Character allowedName(String text_){
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

        alertDialogBuilder.setTitle("Wprowadź nazwę:");

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        final View view1 = layoutInflater.inflate(R.layout.message_edit_text, null);
        //final EditText nameEnter = findViewById(R.id.messageEditText);

        alertDialogBuilder.setView(view1)
                .setPositiveButton("Zapisz", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText nameEnter = view1.findViewById(R.id.messageEditText);
                        if (!TextUtils.isEmpty(nameEnter.getText().toString()) && allowedName(nameEnter.getText().toString()) == null) {
                            Save(nameEnter.getText().toString());
                            dialog.cancel();
                        }else{
                            Toast.makeText(getApplicationContext(), "Only a-z, A-Z, 0-9, '-', '_'", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Odrzuć", new DialogInterface.OnClickListener() {
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
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(filename_ + ".mxt", 0));
            outputStreamWriter.write(mainEditText.getText().toString());
            outputStreamWriter.close();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }catch (Throwable t){
            Toast.makeText(this, "Error at saving: " + t.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
