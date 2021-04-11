package com.dwiktorowski.catalogofnotes;


import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.FloatingActionButton;
import android.widget.ListView;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    RelativeLayout mainLayout;
    LinearLayout addingMenu;
    TextView titleOfMenu;
    public Switch switchL;

    private boolean menuIsHidden;
    private final float offsetFromRightSide = 32;
    private long animationDuration = 500;

    ListView savesList;

    private final static String PREFS_NAME = "com.example.ddada";

    private static File currentDirectory;

    public static File getCurrentDirectory(){
        return currentDirectory;
    }
    public static void setCurrentDirectory(File newDir_){
        currentDirectory = newDir_;
    }
    public void refreshFiles(){
        ArrayList<SavesBuilder> saves = new ArrayList<>();
        //File directory = getFilesDir();
        //Log.i("PATh", directory.toString());
        Log.i("Path", currentDirectory.toString());
        File[] files = currentDirectory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                String name = files[i].getName().substring(0, files[i].getName().length() - 4);
                String suffix = files[i].getName().substring(files[i].getName().length() - 3);
                Log.i("file", name + " " + suffix);
                if (suffix.equals("mth")) {
                    saves.add(new SavesBuilder(name, SavesBuilder.Types.FORMULA));
                } else if (suffix.equals("txt")) {
                    saves.add(new SavesBuilder(name, SavesBuilder.Types.NOTE));
                } else if (suffix.equals("mxt")) {
                    saves.add(new SavesBuilder(name, SavesBuilder.Types.NOTE_WITH_FORMULA));
                } else
                    Toast.makeText(this, getResources().getString(R.string.wrongExtensionMsg), Toast.LENGTH_LONG).show();
            } else if (files[i].isDirectory()) {
                String name = files[i].getName();
                Log.i("directory", name);

                saves.add(0, new SavesBuilder(name, SavesBuilder.Types.CATALOG));
            }
        }
        SavesBuilder[] savesArray = saves.toArray(new SavesBuilder[]{});
        SavesAdapter adapter = new SavesAdapter(this, this, R.layout.main_list_element, savesArray);

        savesList.setAdapter(adapter);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)titleOfMenu.getLayoutParams();
        ValueAnimator translate = ValueAnimator.ofInt(0, (int) getResources().getDimension(R.dimen.titleViewHeight));
        if (getFilesDir().equals(currentDirectory)) {
            translate.setValues(PropertyValuesHolder.ofInt("resize", params.height, 0));
        } else {
            titleOfMenu.setText(currentDirectory.getName());
            translate.setValues(PropertyValuesHolder.ofInt("resize", params.height, (int) getResources().getDimension(R.dimen.titleViewHeight)));
        }
        translate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)titleOfMenu.getLayoutParams();
                params.height = (int) animation.getAnimatedValue("resize");
                titleOfMenu.setLayoutParams(params);
            }
        });
        translate.setDuration(100);
        translate.start();

    }

    @Override
    protected void onResume() {
        refreshFiles();
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout = findViewById(R.id.mainLayout);
        addingMenu = findViewById(R.id.addingMenu);
        titleOfMenu = findViewById(R.id.titleOfMenu);
        menuIsHidden = true;
        savesList = findViewById(R.id.savesMenu);
        switchL = findViewById(R.id.OnOffPreviewSwitch);
        currentDirectory = getFilesDir();

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
        boolean savedSwitchLState = preferences.getBoolean("showPreview", true);
        switchL.setChecked(savedSwitchLState);
    }

    @Override
    protected void onPause() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showPreview", switchL.isChecked());
        editor.apply();
        //editor.commit();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (menuIsHidden) {
            if (getFilesDir().equals(currentDirectory)) {
                super.onBackPressed();
            } else {
                currentDirectory = currentDirectory.getParentFile();
                refreshFiles();
            }
        } else {
            floatButtonClicked((View) findViewById(R.id.floatingActionButton));
        }
    }

    public void floatButtonClicked(View view) {
        float rightPosOfFAB =  mainLayout.getWidth() - view.getWidth() - offsetFromRightSide;
        float startPosOfFAB = (mainLayout.getWidth() - view.getWidth())/2;
        int startPosOfBar = (int)(mainLayout.getHeight() - view.getY() - view.getHeight() / 1.4736); // Liczba [1.4736] stosowana by wrócić do pozycji zbliżonej do pozycji początkowej
        int topPosOfBar = (mainLayout.getHeight() * 50 / 100);

        Log.i("startPosOfBar", ((Integer) startPosOfBar).toString());
        Log.i("topPosOfBar", ((Integer) topPosOfBar).toString());

        if(menuIsHidden) {
            // OPENING
            ValueAnimator translate = ValueAnimator.ofFloat(view.getX(), rightPosOfFAB, addingMenu.getHeight(), topPosOfBar, 0, 255);
            translate.setValues(PropertyValuesHolder.ofFloat("x(t)", view.getX(), rightPosOfFAB),
                    PropertyValuesHolder.ofInt("y(t)", addingMenu.getHeight(), topPosOfBar),
                    PropertyValuesHolder.ofInt("alpha", 0, 255),
                    PropertyValuesHolder.ofInt("marginTop", 0, 30));
            translate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    // BUTTON
                    View view1 = findViewById(R.id.floatingActionButton);
                    view1.setX((float) animation.getAnimatedValue("x(t)"));

                    // MENU
                    LinearLayout linearLayout = findViewById(R.id.addingMenu);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) linearLayout.getLayoutParams();
                    params.height = (int) animation.getAnimatedValue("y(t)");
                    linearLayout.setLayoutParams(params);
                    linearLayout.setPadding(linearLayout.getPaddingLeft(), (int) animation.getAnimatedValue("marginTop"), linearLayout.getPaddingRight(),
                            linearLayout.getPaddingBottom());


                    // ICON
                    int alpha = (int) animation.getAnimatedValue("alpha");

                    /*if (alpha > 0){
                        ((FloatingActionButton) view1).setImageAlpha(alpha);
                    } else if (alpha < 0) {
                        ((FloatingActionButton) view1).setImageAlpha(-(alpha));
                    }
                    if (alpha < 4 && alpha > -4){
                        ((FloatingActionButton) view1).setImageResource(R.drawable.arrow);
                    }*/

                    if (alpha == 0) {
                        ((FloatingActionButton) view1).setImageAlpha(0);
                        ((FloatingActionButton) view1).setImageResource(R.drawable.arrow);
                    }else if (alpha > 0)
                        ((FloatingActionButton) view1).setImageAlpha(alpha);
                }
            });
            translate.setDuration(animationDuration);
            translate.start();

            // MENU RESIZING - UNUSED
            /*
            LinearLayout linearLayout = findViewById(R.id.addingMenu);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) linearLayout.getLayoutParams();
            Log.i("params.heightI", ((Integer)params.height).toString());
            //params.height = (mainLayout.getHeight() * 80 / 100);
            params.height = (int)(mainLayout.getHeight() - view.getY() - view.getHeight() / 1.4736);
            Log.i("params.heightII", ((Integer)params.height).toString());
            linearLayout.setLayoutParams(params);*/

            //((FloatingActionButton) view).setImageAlpha(2);
            //((FloatingActionButton) view).setImageResource(R.drawable.arrow);

            menuIsHidden = false;
        }else{
            // CLOSING

            ValueAnimator translate = ValueAnimator.ofFloat(view.getX(), startPosOfFAB, addingMenu.getHeight(), startPosOfBar, 0, 255);
            translate.setValues(PropertyValuesHolder.ofFloat("x(t)", view.getX(), startPosOfFAB),
                    PropertyValuesHolder.ofInt("y(t)", addingMenu.getHeight(), startPosOfBar),
                    PropertyValuesHolder.ofInt("alpha", 0, 255),
                    PropertyValuesHolder.ofInt("marginTop", 30, 0));
            translate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    View view1 = findViewById(R.id.floatingActionButton);
                    view1.setX((float) animation.getAnimatedValue("x(t)"));

                    LinearLayout linearLayout = findViewById(R.id.addingMenu);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) linearLayout.getLayoutParams();
                    params.height = (int) animation.getAnimatedValue("y(t)");
                    linearLayout.setLayoutParams(params);
                    linearLayout.setPadding(linearLayout.getPaddingLeft(), (int) animation.getAnimatedValue("marginTop"), linearLayout.getPaddingRight(),
                            linearLayout.getPaddingBottom());

                    // ICON
                    int alpha = (int) animation.getAnimatedValue("alpha");

                    if (alpha == 0) {
                        ((FloatingActionButton) view1).setImageAlpha(0);
                        ((FloatingActionButton) view1).setImageResource(R.drawable.plus_larger);
                    }else if (alpha > 0)
                        ((FloatingActionButton) view1).setImageAlpha(alpha);
                }
            });

            translate.setDuration(animationDuration);
            translate.start();

            //((FloatingActionButton) view).setImageResource(R.drawable.plus_larger);

            menuIsHidden = true;
        }
        /*
        if (view.getX() == rightXPos)
            menuIsHidden = false;
        else if (view.getX() == startPosOfFAB)
            menuIsHidden = true;*/
    }

    public void onOffSwitch(View view){
        if (view.getId() == R.id.OnOffPreviewText)
            switchL.setChecked(!(switchL.isChecked()));
        refreshFiles();
    }

    public void addListItemClicked(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.addNote:
                intent = new Intent(this, NoteInsert.class);
                startActivity(intent);
                if (!menuIsHidden)
                    floatButtonClicked((View) findViewById(R.id.floatingActionButton));
                //onResume();
                break;
            /*case R.id.addNoteWithFormula:
                intent = new Intent(this, NoteWithFormulaInsert.class);
                startActivity(intent);
                if (!menuIsHidden)
                    floatButtonClicked((View) findViewById(R.id.floatingActionButton));
                onResume();
                break;*/
            case R.id.addFormula:
                intent = new Intent(this, formula_insert.class);
                startActivity(intent);
                if (!menuIsHidden)
                    floatButtonClicked((View) findViewById(R.id.floatingActionButton));
                //onResume();
                break;
            case R.id.addCatalog:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(getResources().getString(R.string.insertNameMsg));

                LayoutInflater layoutInflater = LayoutInflater.from(this);

                final View view1 = layoutInflater.inflate(R.layout.message_edit_text, null);
                //final EditText nameEnter = findViewById(R.id.messageEditText);

                alertDialogBuilder.setView(view1)
                        .setPositiveButton(getResources().getString(R.string.addMsg), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final EditText nameEnter = view1.findViewById(R.id.messageEditText);
                                if (!TextUtils.isEmpty(nameEnter.getText().toString()) && NoteInsert.allowedName(nameEnter.getText().toString()) == null) {
                                    File newDir = new File(currentDirectory.getPath() + '/' + nameEnter.getText().toString());
                                    newDir.mkdir();

                                    currentDirectory = newDir;
                                    Log.i("Path after edit", newDir.toString());
                                    refreshFiles();
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
                break;
            default:
                Log.e("MainActivity", "wrong object in addListItemClicked()");
        }
    }
}
