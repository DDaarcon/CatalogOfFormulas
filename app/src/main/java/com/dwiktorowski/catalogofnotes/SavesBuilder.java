package com.dwiktorowski.catalogofnotes;

import android.widget.ImageView;

public class SavesBuilder {

    public enum Types {NOTE, FORMULA, NOTE_WITH_FORMULA, CATALOG}

    private String title;
    private Types type;

    public SavesBuilder(){

    }

    public SavesBuilder(String title_, Types type_ ){
        title = title_;
        type = type_;
    }

    public String getTitle(){
        return title;
    }

    public Types getType(){
        return type;
    }
}
