package com.dwiktorowski.catalogofnotes;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import io.github.kexanie.library.MathView;

public class formula_insert extends AppCompatActivity {

    MathView mainMathView;

    private Character[] allowedCharactersInTextInsert = {
            'a', 'b', 'c' ,'d', 'e', 'f' ,'g', 'h', 'i' ,'j', 'k', 'l' ,'m', 'n', 'o' ,'p', 'q', 'r', 's' ,'t', 'u', 'w' ,'v', 'x', 'y', 'z',
            'A', 'B', 'C' ,'C', 'E', 'F' ,'G', 'H', 'I' ,'J', 'K', 'L' ,'M', 'N', 'O' ,'P', 'Q', 'R', 'S' ,'T', 'U', 'W' ,'V', 'X', 'Y', 'Z',
            '0', '1', '2' ,'3', '4', '5' ,'6', '7', '8' ,'9', '.', ','};

    private Character[] extraAllowedCharactersInName = {
            '_', '-'
    };

    private String lastSaved;

    private Animation effect;

    boolean infoWithCursor = true;

    public class Formula{
        public String text;
        public int cursPosition;

        private final int savesAll = 30;
        private int savesNow = 0;
        List<String> saveList = new ArrayList<String>(savesAll);
        List<Integer> cursList = new ArrayList<Integer>(savesAll);

        String cursorSign = "\\star";

        Formula(){
            text = "$$ $$";
            cursPosition = 3;
        }

        public String applyCursor(){
            return text.substring(0, cursPosition) + " " + cursorSign + " " + text.substring(cursPosition);
        }

        public String editText(String newText_, int newCursPosition_){
            if (saveList.size() >= savesAll) {
                Log.i("deleting", (String)saveList.get(0));
                savesNow--;
                saveList.remove(0);
                cursList.remove(0);
                Log.i("what is left at 0pos", saveList.get(0) + "   " + cursList.get(0).toString());
            }
            savesNow++;
            cursList.add(savesNow - 1, cursPosition);
            saveList.add(savesNow - 1, text);
            cursPosition = newCursPosition_;
            text = newText_;
            for (int i = 0; i < savesNow; i++){
                Log.i("Saves", saveList.get(i) + "   " + cursList.get(i).toString());
            }

            return newText_;
        }

        public String restoreText(){
            if (savesNow > 0){
                String newText = saveList.get(savesNow - 1);
                Integer newCursPosition = cursList.get(savesNow - 1);
                saveList.remove(savesNow - 1);
                cursList.remove(savesNow - 1);
                savesNow--;
                text = newText;
                cursPosition = newCursPosition;
                return newText;
            }
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.canNotRestoreMoreMsg), Toast.LENGTH_SHORT).show();
            return null;
        }

        public String applyDotCursor(){
            return text.substring(0, cursPosition) + " . " + text.substring(cursPosition);
        }

        public int moveCursorLeft(int pos_){
            int tempPos = pos_;

            /* Błędy:
            * -- \sqrt[ ]{ } - w pozycji przed znakiem '[' po wywołaniu funkcji przechodzi między 't' a '[' co wywołuje błąd
            * ^{ } - w pozycji przed znakiem '{' po wywołaniu funkcji pomija element stojący przed '^'
            * -- ^{ } - w pozycji, gdy nie ma na lewo żadnego obiekttu, wychodzi poza zakres tablicy i powoduje błąd
            *
            *
             */
            while (text.charAt(tempPos - 1) == ' '){
                tempPos--;
            }
            // spacje usunięte

            if (text.charAt(tempPos - 1) == '$' && text.charAt(tempPos - 2) == '$'){
                Log.i("moveCursorLeft", "Edge of formula");
                return pos_;
            }
            // na pewno nie koniec wzoru

            if (text.charAt(tempPos - 1) == '}'){
                /*if (text.charAt(tempPos - 2) == '}'){
                    if (text.charAt(tempPos - 3) == '}')
                        return tempPos - 3;
                    return tempPos - 2;
                }*/
                pos_ = tempPos - 1;
                return pos_;
            }
            // na pewno nie '}'

            if (isNumber(tempPos - 1)){
                pos_ = tempPos - 1;
                return pos_;
            }
            // na pewno nie liczba

            if (text.charAt(tempPos - 1) == '{'){
                // klamra otwarta (możliwe: znaki indeksu [" ^"][" _"] OK, klamra zamknięta [" }"] OK, funkcje specjalne [" \frac "]
                tempPos--;

                while (text.charAt(tempPos - 1) == ' '){
                    tempPos--;
                }
                // spacje usunięte

                if (text.charAt(tempPos - 1) == '^' ||
                        text.charAt(tempPos - 1) == '_'){
                    // indeks górny/dolny (możliwe: znak specjalny [" \alpha "], cyfra [" 1 "], znak zwykły [" a "][" abc "], nawias [" ) "][" ] "]
                    tempPos--;

                    while (text.charAt(tempPos - 1) == ' '){
                        tempPos--;
                    }
                    // spacje usunięte

                    if (isNumber(tempPos - 1)){
                        return tempPos - 1;
                    }
                    // na pewno nie liczba

                    if (text.charAt(tempPos - 1) == ')' ||
                            text.charAt(tempPos - 1) == ']'){
                        return tempPos - 1;
                    }
                    // na pewno nie nawias

                    if (text.charAt(tempPos - 1) == '$' && text.charAt(tempPos - 2) == '$'){
                        Log.i("moveCursorLeft", "Edge of formula");
                        return tempPos;
                    }
                    // na pewno nie koniec wzoru

                    // zapewne znak z klawiatury lub lub znak specjalny
                    return ifLetterL(tempPos);

                }

                if (text.charAt(tempPos - 1) == '}'){
                    return tempPos - 1;
                }
                // na pewno nie klamra zamknięta

                // zapewne znak z klawiatury lub znak specjalny
                return ifLetterL(tempPos);
            }
            // na pewno nie '{'

            if (text.charAt(tempPos - 1) == '=' ||
                    text.charAt(tempPos - 1) == '+' ||
                    text.charAt(tempPos - 1) == '-' ||
                    text.charAt(tempPos - 1) == '*' ||
                    text.charAt(tempPos - 1) == '/' ||
                    text.charAt(tempPos - 1) == '(' ||
                    text.charAt(tempPos - 1) == ')' ||
                    text.charAt(tempPos - 1) == '[' ||
                    text.charAt(tempPos - 1) == ']'){
                return tempPos - 1;
            }
            // na pewno nie '=', '+', '-', '*', '/'

            // zapewne znak z klawiatury lub znak specjalny
            return ifLetterL(tempPos);
        }

        public int moveCursorRight(int pos_){
            int tempPos = pos_;

            /* Błędy:
             * ?? jeśli jest funkcja do potęgi, nie pomija pozycji przed potęgą, tzn. "\sin @ ^{ }" pozostaje w pozycji @
             *
             *
             */

            while (text.charAt(tempPos) == ' '){
                tempPos++;
            }
            // spacje usunięte

            if (text.charAt(tempPos) == '$' && text.charAt(tempPos + 1) == '$'){
                Log.i("moveCursorRight", "Edge of formula");
                return pos_;
            }
            // na pewno nie koniec wzoru
            //Log.i("na pewno", "nie koniec");

            //Log.i("special sign", ((Character)text.charAt(tempPos)).toString());
            if (text.charAt(tempPos) == '\\'){
                return ifSpecialSignR(tempPos);
            }
            // na pewno nie specjalny znak
            //Log.i("na pewno", "nie specjalny znak");

            if (text.charAt(tempPos) == '}')
                if (text.charAt(tempPos + 1) == '{')
                    return tempPos + 2;
            // na pewno nie zamknięta klamra
            //Log.i("na pewno", "nie klamra");

            if (text.charAt(tempPos) == '^' ||
                    text.charAt(tempPos) == '_')
                if (text.charAt(tempPos + 1) == '{')
                    return tempPos + 2;
            // na pewno nie indeks górny/dolny
            //Log.i("na pewno", "nie indeksy");

            if (text.charAt(tempPos) == ']' &&
                    text.charAt(tempPos + 1) == '{')
                return tempPos + 2;

            //Log.i("czy aby", "na pewno?");
            // zapewne znak z klawiatury, liczba, '.', ',', '+', '-', '=', '(', ')', '[', ']'
            return tempPos + 1;
        }

        private boolean isNumber(int pos_){
            // podawać do pos- aktualną pozycje bez -1
            if (text.charAt(pos_) == '0' ||
                    text.charAt(pos_) == '1' ||
                    text.charAt(pos_) == '2' ||
                    text.charAt(pos_) == '3' ||
                    text.charAt(pos_) == '4' ||
                    text.charAt(pos_) == '5' ||
                    text.charAt(pos_) == '6' ||
                    text.charAt(pos_) == '7' ||
                    text.charAt(pos_) == '8' ||
                    text.charAt(pos_) == '9' ||
                    text.charAt(pos_) == '.' ||
                    text.charAt(pos_) == ','){
                return true;
            }
            else return false;
        }

        private int ifSpecialSignR(int pos_){
            // podawać do pos_ aktualną pozycję (pozycję znaku '\')

            int tempTempPos = pos_;
            while (true){
                if (text.charAt(tempTempPos) == ' ' ||
                        text.charAt(tempTempPos) == '{' ||
                        text.charAt(tempTempPos) == '[' ||
                        text.charAt(tempTempPos) == '^' ||
                        text.charAt(tempTempPos) == '_'){
                    break;
                }
                tempTempPos++;
            }

            if (text.charAt(tempTempPos) == ' ')
                return tempTempPos;

            if (text.charAt(tempTempPos) == '{' ||
                    text.charAt(tempTempPos) == '[')
                return tempTempPos + 1;

            if (text.charAt(tempTempPos) == '^' ||
                    text.charAt(tempTempPos) == '_')
                if (text.charAt(tempTempPos + 1) == '{')
                    return tempTempPos + 2;

            return pos_;
        }

        private int ifLetterL(int pos_){
            // podawać do pos_ aktualną pozycje bez -1

            int tempTempPos = pos_;
            while (true){
                if (text.charAt(tempTempPos - 1) == ' ' ||
                        text.charAt(tempTempPos - 1) == '\\')
                    break;
                //Log.i("test", ((Character)text.charAt(tempTempPos - 1)).toString());
                tempTempPos--;
            }
            if (text.charAt(tempTempPos - 1) == ' '){
                return --pos_;
            }
            if (text.charAt(tempTempPos - 1) == '\\'){
                pos_ = tempTempPos - 1;
                return pos_;
            }
            return pos_;
        }
    }

    Formula formula = new Formula();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formula_insert);
        ((Button)findViewById(R.id.insertBtn)).getBackground().setColorFilter(0xFFffeb3b, PorterDuff.Mode.MULTIPLY);
        mainMathView = findViewById(R.id.insertMath);
        formula.cursPosition = 2;
        try {
            effect = AnimationUtils.loadAnimation(this, R.anim.many_btns_animation);
        } catch (Throwable t){
            Log.i("Error", t.toString());
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            formula.text = bundle.getCharSequence("formula").toString();
            refresh();
        }
        lastSaved = formula.text;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putCharSequence("formula_text", formula.text);
        outState.putInt("formula_cursor_position", formula.cursPosition);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        formula.text = savedInstanceState.getCharSequence("formula_text").toString();
        formula.cursPosition = savedInstanceState.getInt("formula_cursor_position");

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if ((!lastSaved.equals(formula.text))) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setTitle(getResources().getString(R.string.exitQMsg));

            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.yesMsg), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    formula_insert.super.onBackPressed();
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

    public void refresh(){
        mainMathView.setText(formula.applyCursor());
        //((TextView) findViewById(R.id.testerView)).setText(formula.applyDotCursor());
        //((TextView) findViewById(R.id.testerViewCursor)).setText(((Integer) formula.cursPosition).toString());
        Log.i("wholeFormula", mainMathView.getText());
    }

    @Nullable
    private Character allowedText(String text_){
        boolean found;
        for (int i = 0; i < text_.length(); i++) {
            found = false;
            for (int j = 0; j < allowedCharactersInTextInsert.length; j++){
                if (text_.charAt(i) == allowedCharactersInTextInsert[j]) {
                    found = true;
                    break;
                }
            }
            if (!found)
                return text_.charAt(i);
        }
        return null;
    }
    @Nullable
    private Character allowedName(String text_){
        boolean found;
        for (int i = 0; i < text_.length(); i++) {
            found = false;
            for (int j = 0; j < allowedCharactersInTextInsert.length; j++){
                if (text_.charAt(i) == allowedCharactersInTextInsert[j]) {
                    found = true;
                    break;
                }
            }
            for (int j = 0; j < extraAllowedCharactersInName.length; j++){
                if (text_.charAt(i) == extraAllowedCharactersInName[j]) {
                    found = true;
                    break;
                }
            }
            if (text_.charAt(i) == '.' || text_.charAt(i) == ',')
                found = false;
            if (!found)
                return text_.charAt(i);
        }
        return null;
    }

    public void insertBtn(View view){
        EditText editText = findViewById(R.id.insertMathText);
        String text = editText.getText().toString();
        Character unsupportedCharacter = allowedText(text);
        if (unsupportedCharacter == null) {
            if (!(text.equals(""))){
                compileTextAndCursorPosition( text, text.length() + 1);
                refresh();
                editText.onEditorAction(EditorInfo.IME_ACTION_DONE);
                editText.setText("");
            }
        }else {
            Toast.makeText(this, getResources().getString(R.string.unsupportedCharacterMsg) + "\'" + unsupportedCharacter + "\'!", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, getResources().getString(R.string.unexpectedCharactersFormulaMsg), Toast.LENGTH_SHORT).show();
        }
        //Toast.makeText(this, mainMathView.getText(),Toast.LENGTH_LONG).show();
    }

    public void clearBtn(View view){
        //formula.text = "$$ $$";
        formula.editText("$$ $$", 3);
        //formula.cursPosition = 3;
        refresh();

    }

    public void restoreBtn(View view){
        String tryRestore = formula.restoreText();
        if (tryRestore != null){
            refresh();
        }
    }

    private void Save(String filename_){
        try {
            //OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(filename_ + ".mth", 0));
            //OutputStreamWriter outputStreamWriter =
            //        new OutputStreamWriter(this.openFileOutput(MainActivity.getCurrentDirectory().toString() + '/' + filename_ + ".mth", 0));
            File parentPath = new File(MainActivity.getCurrentDirectory().getPath());
            FileOutputStream outputStreamWriter = new FileOutputStream(new File(parentPath, filename_ + ".mth"));
            outputStreamWriter.write(formula.text.getBytes());
            outputStreamWriter.close();
            lastSaved = formula.text;
            Toast.makeText(this, getResources().getString(R.string.savedMsg), Toast.LENGTH_SHORT).show();
        }catch (Throwable t){
            Toast.makeText(this, getResources().getString(R.string.savingErrorMsg) + t.toString(), Toast.LENGTH_SHORT).show();
        }
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

    public void infoBtn(View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(getResources().getString(R.string.TeXMsg));

        String textToDisplay;

        if (infoWithCursor)
            textToDisplay = formula.applyDotCursor();
        else
            textToDisplay = formula.text;
        alertDialogBuilder.setMessage(textToDisplay)
                .setNegativeButton(getResources().getString(R.string.backMsg), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    public void moveCursorBtnClicked(View view){
        if (view.getId() == R.id.moveCursorLeftBtn){
            formula.cursPosition = formula.moveCursorLeft(formula.cursPosition);
            refresh();
            //Toast.makeText(this, ((Integer) formula.cursPosition).toString(), Toast.LENGTH_SHORT).show();
            //if (formula.text.charAt(2) == ' ')
            //   Log.i("space detector", "detected");
        }
        else if (view.getId() == R.id.moveCursorRightBtn){
            formula.cursPosition = formula.moveCursorRight(formula.cursPosition);
            refresh();
            //Toast.makeText(this, ((Integer) formula.cursPosition).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void compileTextAndCursorPosition(String text_, Integer cursMoveRightBy_){
        formula.editText(formula.text.substring(0, formula.cursPosition) + " " + text_ + " " + formula.text.substring(formula.cursPosition),
                formula.cursPosition + cursMoveRightBy_);
    }

    public void manyInsertBtns(View view){
        int id = view.getId();
        try {
            Animation effect = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.many_btns_animation);
            view.startAnimation(effect);
        } catch (Throwable t) {
            Log.i("is animation errorring", "yes it it");
        }

        switch (id){
            // row 1
            case R.id.fracBtn:
                compileTextAndCursorPosition("\\frac{ }{ }", 7);
                refresh();
                break;
            case R.id.powBtn:
                compileTextAndCursorPosition("^{ }", 3);
                refresh();
                break;
            case R.id.indexBtn:
                compileTextAndCursorPosition("_{ }", 3);
                refresh();
                break;
            case R.id.sqrtBtn:
                compileTextAndCursorPosition("\\sqrt{ }", 7);
                refresh();
                break;
            case R.id.rootBtn:
                compileTextAndCursorPosition("\\sqrt[ ]{ }", 7);
                refresh();
                break;
            case R.id.percentBtn:
                compileTextAndCursorPosition("\\%", 4);
                refresh();
                break;
            case R.id.strongBtn:
                compileTextAndCursorPosition("!", 2);
                refresh();
                break;
            case R.id.degreeBtn:
                compileTextAndCursorPosition("^{\\circ}", 10);
                refresh();
                break;
            case R.id.minutesBtn:
                compileTextAndCursorPosition("'", 2);
                refresh();
                break;
            case R.id.secondsBtn:
                compileTextAndCursorPosition("''", 3);
                refresh();
                break;

            // row 2
            case R.id.equalBtn:
                compileTextAndCursorPosition("=", 2);
                refresh();
                break;
            case R.id.plusBtn:
                compileTextAndCursorPosition("+", 2);
                refresh();
                break;
            case R.id.minusBtn:
                compileTextAndCursorPosition("-", 2);
                refresh();
                break;
            case R.id.cdotBtn:
                compileTextAndCursorPosition("\\cdot", 6);
                refresh();
                break;
            case R.id.timesBtn:
                compileTextAndCursorPosition("\\times", 7);
                refresh();
                break;
            case R.id.colonBtn:
                compileTextAndCursorPosition(":", 2);
                refresh();
                break;
            case R.id.divBtn:
                compileTextAndCursorPosition("\\div", 5);
                refresh();
                break;
            case R.id.ltBtn:
                compileTextAndCursorPosition("<", 2);
                refresh();
                break;
            case R.id.leBtn:
                compileTextAndCursorPosition("\\le", 4);
                refresh();
                break;
            case R.id.geBtn:
                compileTextAndCursorPosition("\\ge", 4);
                refresh();
                break;
            case R.id.gtBtn:
                compileTextAndCursorPosition(">", 2);
                refresh();
                break;
            case R.id.neqBtn:
                compileTextAndCursorPosition("\\neq", 5);
                refresh();
                break;

            // row 3
            case R.id.lcircBtn:
                compileTextAndCursorPosition("(", 2);
                refresh();
                break;
            case R.id.lsquBtn:
                compileTextAndCursorPosition("[", 2);
                refresh();
                break;
            case R.id.langleBtn:
                compileTextAndCursorPosition("\\langle", 9);
                refresh();
                break;
            case R.id.vertBtn:
                compileTextAndCursorPosition("\\vert", 7);
                refresh();
                break;
            case R.id.rangleBtn:
                compileTextAndCursorPosition("\\rangle", 9);
                refresh();
                break;
            case R.id.rsquBtn:
                compileTextAndCursorPosition("]", 2);
                refresh();
                break;
            case R.id.rcircBtn:
                compileTextAndCursorPosition(")", 2);
                refresh();
                break;
            case R.id.inBtn:
                compileTextAndCursorPosition("\\in", 5);
                refresh();
                break;
            case R.id.notinBtn:
                compileTextAndCursorPosition("\\notin", 8);
                refresh();
                break;
            case R.id.RightarrowBtn:
                compileTextAndCursorPosition("\\Rightarrow", 13);
                refresh();
                break;
            case R.id.approxBtn:
                compileTextAndCursorPosition("\\approx", 9);
                refresh();
                break;
            case R.id.simBtn:
                compileTextAndCursorPosition("\\sim", 6);
                refresh();
                break;
            case R.id.inftyBtn:
                compileTextAndCursorPosition("\\infty", 8);
                refresh();
                break;
            case R.id.barBtn:
                compileTextAndCursorPosition("\\bar{ }", 7);
                refresh();
                break;
            case R.id.vecBtn:
                compileTextAndCursorPosition("\\vec{ }", 7);
                refresh();
                break;
            case R.id.ldotsBtn:
                compileTextAndCursorPosition("\\ldots", 8);
                refresh();
                break;

            // row 4
            case R.id.sinBtn:
                compileTextAndCursorPosition("\\sin{ }", 6);
                refresh();
                break;
            case R.id.cosBtn:
                compileTextAndCursorPosition("\\cos{ }", 6);
                refresh();
                break;
            case R.id.tanBtn:
                compileTextAndCursorPosition("\\tan{ }", 6);
                refresh();
                break;
            case R.id.sinupBtn:
                compileTextAndCursorPosition("\\sin^{ }{ }", 8);
                refresh();
                break;
            case R.id.cosupBtn:
                compileTextAndCursorPosition("\\cos^{ }{ }", 8);
                refresh();
                break;
            case R.id.tanupBtn:
                compileTextAndCursorPosition("\\tan^{ }{ }", 8);
                refresh();
                break;
            case R.id.logBtn:
                compileTextAndCursorPosition("\\log_{ }{ }", 8);
                refresh();
                break;
            case R.id.lnBtn:
                compileTextAndCursorPosition("\\ln{ }", 6);
                refresh();
                break;


            // row 5
            case R.id.piBtn:
                compileTextAndCursorPosition("\\pi", 5);
                refresh();
                break;
            case R.id.alphaBtn:
                compileTextAndCursorPosition("\\alpha", 8);
                refresh();
                break;
            case R.id.betaBtn:
                compileTextAndCursorPosition("\\beta", 7);
                refresh();
                break;
            case R.id.gammaBtn:
                compileTextAndCursorPosition("\\gamma", 8);
                refresh();
                break;
            case R.id.DeltaBtn:
                compileTextAndCursorPosition("\\Delta", 8);
                refresh();
                break;
            case R.id.deltaBtn:
                compileTextAndCursorPosition("\\delta", 8);
                refresh();
                break;
            case R.id.omegaBtn:
                compileTextAndCursorPosition("\\omega", 8);
                refresh();
                break;
            case R.id.phiBtn:
                compileTextAndCursorPosition("\\phi", 6);
                refresh();
                break;
            case R.id.rhoBtn:
                compileTextAndCursorPosition("\\rho", 6);
                refresh();
                break;
            case R.id.epsilonBtn:
                compileTextAndCursorPosition("\\varepsilon", 13);
                refresh();
                break;
            case R.id.OmegaBtn:
                compileTextAndCursorPosition("\\Omega", 8);
                refresh();
                break;
            default:
                //Toast.makeText(this, "Wrong ID! This button is not finished", Toast.LENGTH_SHORT).show();
        }
    }
}
