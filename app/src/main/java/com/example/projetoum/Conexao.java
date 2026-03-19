package com.example.projetoum;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class Conexao extends SQLiteOpenHelper{

    private static final String name = "banco.db";
    private static final int version = 2;


    public Conexao(Context context) {
        super(context, name, null, version);
    }


    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table aluno(id integer primary key autoincrement, " +
                "nome varchar(50), cpf varchar(50), telefone varchar(50), " +
                "endereco varchar(100), curso varchar(50), fotoBytes BLOB)");
    }



    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Recria a tabela com todas as colunas corretas
        db.execSQL("DROP TABLE IF EXISTS aluno");
        onCreate(db);
    }
}
