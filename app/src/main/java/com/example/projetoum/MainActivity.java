package com.example.projetoum;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

private EditText nome;
private EditText cpf;
private EditText telefone;
private EditText endereco;
private EditText curso;

private AlunoDAO dao;

private Aluno aluno = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Vinculando os componentes do XML com as variáveis Java (ID deve ser igual ao do XML)
        nome = findViewById(R.id.texto1);
        cpf = findViewById(R.id.texto2);
        telefone = findViewById(R.id.texto3);
        endereco = findViewById(R.id.texto4);
        curso = findViewById(R.id.texto5);

        Intent it = getIntent(); //pega intenção
        if(it.hasExtra("aluno")){
            aluno = (Aluno) it.getSerializableExtra("aluno");
            nome.setText(aluno.getNome().toString());
            cpf.setText(aluno.getCpf());
            telefone.setText(aluno.getTelefone());
        }

        // Instanciando o DAO. Passamos 'this' (a própria Activity) como Contexto.
        // O Contexto é necessário para o SQLite saber em que pasta do sistema salvar o arquivo.
        dao = new AlunoDAO(this);

    }


    public void salvar(View view){

        String cpfDigitado = cpf.getText().toString();
        String telefoneDigitado = telefone.getText().toString();

        if(!dao.validaCpf(cpfDigitado)){
            Toast.makeText(this, "CPF inválido!", Toast.LENGTH_LONG).show();
            cpf.requestFocus(); // Opcional: foca no campo de CPF para correção
            return; // Interrompe a execução do método salvar
        }

        if (!dao.validaTelefone(telefoneDigitado)) {
            Toast.makeText(this, "Telefone inválido! Use o formato (XX) 9XXXX-XXXX.", Toast.LENGTH_LONG).show();
            telefone.requestFocus(); // Opcional: foca no campo de telefone para correção
            return; // Interrompe a execução do método salvar
        }





        if(aluno==null) { //SE NAO RECEBEU DADOS DO ATUALIZAR É ESTA COMO 'NULL', CADASTRA O ALUNO
            Aluno a = new Aluno();


            // 2. Coletamos os dados digitados nos EditText e convertemos para String
            a.setNome(nome.getText().toString());
            a.setCpf(cpf.getText().toString());
            a.setTelefone(telefone.getText().toString());
            a.setEndereco(endereco.getText().toString());
            a.setCurso(curso.getText().toString());
            // 3. Chamamos o método de persistência do DAO
            // O retorno 'long' indica o ID gerado pelo banco para este novo registro.
            long id = dao.Inserir(a);

            if (id == -1) {
                // O método Inserir retorna -1 quando o CPF já existe
                Toast.makeText(this, "Erro: CPF já cadastrado!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Aluno inserido com id: " + id, Toast.LENGTH_SHORT).show();
                // Opcional: Limpar os campos após o sucesso
            }
        }
        else {
            //RECEBENDO DADOS DO ATUALIZAR NA VARIAVEL 'aluno'
            //seta os valores de novo e atualiza
            aluno.setNome(nome.getText().toString());
            aluno.setCpf(cpf.getText().toString());
            aluno.setTelefone(telefone.getText().toString());
            dao.atualizar(aluno); //inserir o aluno

            Toast.makeText(this,"Aluno Atualizado!! com id: ", Toast.LENGTH_SHORT).show();

        }

        // 1. Criamos um objeto de modelo (POJO)

    }


    public void irLista(View view){
        // O nome deve ser exatamente o nome da classe Java que você criou
        Intent intent = new Intent(this, activity_listar_alunos.class);
        startActivity(intent);
    }


}