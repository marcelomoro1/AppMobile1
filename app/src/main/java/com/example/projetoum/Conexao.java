package com.example.projetoum;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class Conexao extends SQLiteOpenHelper{

    private static final String name = "banco.db";
    private static final int version = 1;


    public Conexao(Context context) {
        super(context, name, null, version);
    }


    public void onCreate(SQLiteDatabase db) {
        // execSQL: Executa um comando SQL puro de DDL (Data Definition Language).
        // Aqui definimos a estrutura da tabela "aluno".
        db.execSQL("create table aluno(" +
                "id integer primary key autoincrement, " + // ID automático e único
                "nome varchar(50), " +                    // Limite de 50 caracteres para o nome
                "cpf varchar(50), " +
                "telefone varchar(50), " +
                "endereco varchar(50), " +// Armazenamos CPF como String/Varchar
                "curso varchar(50))");                 // Armazenamos telefone como String/Varchar
    }



    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Exemplo: Se o senhor mudar a versão para 2, poderia colocar aqui um:
        // db.execSQL("alter table aluno add column email varchar(50)");
    }
}
