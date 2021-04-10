package com.dwiktorowski.catalogofnotes;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AppComponentFactory;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import io.github.kexanie.library.MathView;

public class SavesAdapter extends ArrayAdapter<SavesBuilder>{

    Context context;
    int layoutResourceId;
    SavesBuilder data[];
    private final MainActivity mainActivity;

    public SavesAdapter(MainActivity mainActivity_, Context context_, int layoutResourceId_, SavesBuilder[] data_){
        super(context_, layoutResourceId_, data_);
        layoutResourceId = layoutResourceId_;
        context = context_;
        data = data_;
        mainActivity = mainActivity_;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable final View convertView, @NonNull ViewGroup parent) {
        View save = convertView;
        final SavesHolder holder;

        if (save == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            save = inflater.inflate(layoutResourceId, parent, false);

            holder = new SavesHolder();
            holder.text = (TextView)save.findViewById(R.id.itemText);
            holder.typeImg = (ImageView)save.findViewById(R.id.itemIcon);
            holder.delete = (Button)save.findViewById(R.id.saveDeleteBtn);
            holder.openArea = (Button)save.findViewById(R.id.openArea);
            holder.formulaPreview = (MathView)save.findViewById(R.id.formulaPreview);
            holder.formulaPreviewBtn = (Button)save.findViewById(R.id.formulaPreviewBtn);
            holder.almostWholeButton = (RelativeLayout)save.findViewById(R.id.almostWholeButton);

            save.setTag(holder);
        } else {
            holder = (SavesHolder)save.getTag();
        }


        SavesBuilder object = data[position];
        holder.text.setText(object.getTitle());
        final String textFinal = holder.text.getText().toString();
        final SavesBuilder.Types typeFinal = object.getType();
        switch (object.getType()){
            case NOTE:
                holder.typeImg.setImageResource(R.drawable.docicon);

                break;
            case FORMULA:
                holder.typeImg.setImageResource(R.drawable.alpha);
                if (mainActivity.switchL.isChecked()) {
                    holder.formulaPreview.setVisibility(View.VISIBLE);
                    holder.formulaPreviewBtn.setVisibility(View.VISIBLE);
                    final String formulaPreviewContext = Open(textFinal + ".mth");
                    // POWIĘKSZANIE POLA ZE WZOREM

                    final SavesHolder holderFinal = holder;
                    final int formulaPreviewHeightNormal = (int) context.getResources().getDimension(R.dimen.formulaPreviewHeightNormal);
                    final int formulaPreviewHeightBigger = (int) context.getResources().getDimension(R.dimen.formulaPreviewHeightBigger);

                    String formulaPreviewContextEdited;
                    if (holder.formulaPreview.getLayoutParams().height == formulaPreviewHeightNormal)
                        formulaPreviewContextEdited = formulaPreviewContext.substring(0, 2) + " \\tiny " +
                                formulaPreviewContext.substring(2);
                    else
                        formulaPreviewContextEdited = formulaPreviewContext.substring(0, 2) + " \\small " +
                                formulaPreviewContext.substring(2);
                    holder.formulaPreview.setText(formulaPreviewContextEdited);

                    holder.formulaPreviewBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i("formulaPreviewBtn", "clicked");

                            final RelativeLayout almostWholeButton = holderFinal.almostWholeButton;
                            final Button delete = holderFinal.delete;
                            final Button formulaPreviewBtn = holderFinal.formulaPreviewBtn;
                            final MathView formulaPreview = holderFinal.formulaPreview;
                            String formulaPreviewContextEdited = formulaPreview.getText();

                            ValueAnimator translate = ValueAnimator.ofFloat(formulaPreviewHeightNormal, formulaPreviewHeightBigger);
                            if (formulaPreview.getLayoutParams().height == formulaPreviewHeightNormal) {
                                formulaPreviewContextEdited = formulaPreviewContext.substring(0, 2) + " \\small " +
                                        formulaPreviewContext.substring(2);
                                translate.setValues(PropertyValuesHolder.ofInt("formulaPreviewHeight", formulaPreviewHeightNormal, formulaPreviewHeightBigger));

                            } else if (formulaPreview.getLayoutParams().height == formulaPreviewHeightBigger) {
                                formulaPreviewContextEdited = formulaPreviewContext.substring(0, 2) + " \\tiny " +
                                        formulaPreviewContext.substring(2);
                                formulaPreview.setText(formulaPreviewContextEdited);
                                translate.setValues(PropertyValuesHolder.ofInt("formulaPreviewHeight", formulaPreviewHeightBigger, formulaPreviewHeightNormal));
                            } else
                                return;
                            formulaPreview.setText(formulaPreviewContextEdited);
                            translate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {

                                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) formulaPreview.getLayoutParams();
                                    params.addRule(RelativeLayout.BELOW, almostWholeButton.getId());
                                    params.height = (int) animation.getAnimatedValue("formulaPreviewHeight");
                                    formulaPreview.setLayoutParams(params);
                                    formulaPreviewBtn.setLayoutParams(params);

                                }
                            });
                            translate.setDuration(200);
                            translate.start();
                        }
                    });
                }
                break;
            case NOTE_WITH_FORMULA:
                holder.typeImg.setImageResource(R.drawable.docformicon);

                break;
            case CATALOG:
                holder.typeImg.setImageResource(R.drawable.folder_icon);

                break;
        }

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                alertDialogBuilder.setTitle(context.getResources().getString(R.string.deleteQMsg));

                alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.deleteMsg), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Usuwanie
                                String suffix;
                                switch (typeFinal){
                                    case NOTE:
                                        suffix = ".txt";
                                        break;
                                    case FORMULA:
                                        suffix = ".mth";
                                        break;
                                    case NOTE_WITH_FORMULA:
                                        suffix = ".mxt";
                                        break;
                                    case CATALOG:
                                        suffix = "";
                                        break;
                                    default:
                                        suffix = ".txt";
                                }
                                String path = mainActivity.getCurrentDirectory() + "/" + textFinal + suffix;
                                Log.i("PATH", path);
                                File file = new File(path);
                                if (!(file.delete())) {
                                    Toast.makeText(context, context.getResources().getString(R.string.canNotDeleteMsg), Toast.LENGTH_SHORT).show();
                                    if (typeFinal == SavesBuilder.Types.CATALOG)
                                        Toast.makeText(context, context.getResources().getString(R.string.catalogIsEmptyMsg), Toast.LENGTH_SHORT).show();
                                }
                                mainActivity.refreshFiles();
                            }
                        })
                        .setNegativeButton(context.getResources().getString(R.string.backMsg), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();
            }
        });

        holder.openArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (typeFinal) {
                    case FORMULA:
                        try {
                            Intent intent = new Intent(context, formula_insert.class);
                            //Bundle bundle = intent.getExtras();
                            String fileContext = Open(textFinal + ".mth");
                            String key = "formula";
                            intent.putExtra(key, fileContext);
                            context.startActivity(intent);
                        } catch (NullPointerException e){
                            Log.i("Error", e.toString());
                        }
                        break;
                    case NOTE:
                        try {
                            Intent intent = new Intent(context, NoteInsert.class);
                            String fileContext = Open(textFinal + ".txt");
                            String key = "text";
                            intent.putExtra(key, fileContext);
                            context.startActivity(intent);
                        } catch (NullPointerException e){
                            Log.i("Error", e.toString());
                        }
                        break;
                    case NOTE_WITH_FORMULA:
                        try {
                            Intent intent = new Intent(context, NoteWithFormulaInsert.class);
                            String fileContext = Open(textFinal + ".mxt");
                            String key = "text_formula";
                            intent.putExtra(key, fileContext);
                            context.startActivity(intent);
                        } catch (NullPointerException e){
                            Log.i("Error", e.toString());
                        }
                        break;
                    case CATALOG:
                        File newDir = new File(mainActivity.getCurrentDirectory().getPath() + '/' + holder.text.getText().toString());
                        Log.i("newPath", newDir.toString());
                        mainActivity.setCurrentDirectory(newDir);
                        mainActivity.refreshFiles();
                        break;
                    default:

                }
            }
        });

        return save;
    }

    static class SavesHolder{
        ImageView typeImg;
        TextView text;
        Button delete;
        Button openArea;
        MathView formulaPreview;
        Button formulaPreviewBtn;
        RelativeLayout almostWholeButton;
    }

    private boolean fileExists(String filename_){
        File file = context.getFileStreamPath(filename_);
        return file.exists();
    }

    @Nullable
    private String Open(String filename_){
        String context_ = "";
        //if (fileExists(filename_)) {
            try {
                FileInputStream in = new FileInputStream(new File(mainActivity.getCurrentDirectory(), filename_));
                if (in != null){
                    InputStreamReader tmp = new InputStreamReader(in);
                    BufferedReader reader = new BufferedReader(tmp);
                    String tempStr;
                    StringBuilder buf = new StringBuilder();

                    while ((tempStr = reader.readLine()) != null) {
                        buf.append(tempStr + '\n');
                    }
                    in.close();
                    context_ = buf.toString();
                }
            }catch (java.io.FileNotFoundException e){
                Log.i("exception", "FileNotFoundException");
            }catch (Throwable t){
                Toast.makeText(context, context.getResources().getString(R.string.openingErrorMsg) + t.toString(), Toast.LENGTH_SHORT).show();
            }
        //}
        Log.i("no context?", "con: " + context_);
        return context_;
    }
}
