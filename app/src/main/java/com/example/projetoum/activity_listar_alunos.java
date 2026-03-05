package com.example.projetoum;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class activity_listar_alunos extends AppCompatActivity {


    private ListView listView;
    private AlunoDAO dao;
    private List<Aluno> alunos;

    private List<Aluno> alunosFiltrados = new ArrayList<Aluno>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Define qual arquivo XML será o layout desta tela
        setContentView(R.layout.activity_listar_alunos);

        // Vinculando os componentes do XML com as variáveis Java (ID deve ser igual ao do XML)
        listView = findViewById(R.id.lista_alunos);
        dao = new AlunoDAO(this);
        alunos = dao.obterTodos();
        alunosFiltrados.addAll(alunos);

        //registrar o menu de contexto (excluir e atualizar) na listview
        registerForContextMenu(listView);

        ArrayAdapter<Aluno> adaptador = new ArrayAdapter<Aluno>(this, android.R.layout.simple_list_item_1, alunos);
        listView.setAdapter(adaptador);

    }

    //METODO MENU_CONTEXTO PARA INFLAR O MENU QUANDO ITEM PRESSIONADO
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        // Chama o método da superclasse (neste caso, o método onCreateContextMenu da classe pai).
        // Isso é importante para garantir que qualquer comportamento padrão do método na superclasse
        // (por exemplo, qualquer configuração padrão de menu que a superclasse realiza) seja executado antes
        // de você adicionar suas próprias ações ao menu.
        super.onCreateContextMenu(menu, v, menuInfo);

        // Cria um objeto MenuInflater, que é responsável por inflar (converter um arquivo XML de menu em um objeto Menu)
        // o menu de contexto a partir de um arquivo XML de menu que você criou anteriormente.
        MenuInflater i = getMenuInflater();

        // O método inflate do MenuInflater é usado para inflar o menu de contexto.
        // Aqui, você está especificando o recurso XML (R.menu.menu_contexto) que define as opções de menu
        // que aparecerão quando um item da lista for pressionado.
        i.inflate(R.menu.menu_contexto, menu); //Aqui coloca o nome do menu que havia sido configurado
    }

    public void irCadastro(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void excluir(MenuItem item){
        // Obtém informações do item selecionado no menu de contexto.
        // O objeto `menuInfo` contém a posição do item na lista.
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // Obtém o aluno que será excluído a partir da lista filtrada.
        final Aluno alunoExcluir = alunosFiltrados.get(menuInfo.position);
        // Exibe um alerta de confirmação antes de excluir o aluno
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Atenção") // Título do alerta
                .setMessage("Realmente deseja excluir o aluno?") // Mensagem de confirmação
                .setNegativeButton("NÃO",null) // Caso o usuário clique em "NÃO", fecha o alerta sem fazer nada.
                .setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Remove o aluno da lista filtrada
                        alunosFiltrados.remove(alunoExcluir);
                        // Remove o aluno da lista principal
                        alunos.remove(alunoExcluir);
                        // Exclui o aluno do banco de dados
                        dao.excluir(alunoExcluir);
                        // Atualiza a ListView para refletir a exclusão
                        listView.invalidateViews();
                    }
                } ).create(); // Cria a caixa de diálogo
        dialog.show(); // Exibe o alerta na tela
    }


    public void atualizar(MenuItem item) {
        // Obtém informações do item selecionado no menu de contexto.
        // O objeto `menuInfo` contém a posição do item na lista.
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // Obtém o aluno que será atualizado a partir da lista filtrada.
        final Aluno alunoAtualizar = alunosFiltrados.get(menuInfo.position);
        // Cria uma Intent para abrir a tela de cadastro (MainActivity).
        // Isso permite reutilizar a mesma tela para edição.
        Intent it = new Intent(this, MainActivity.class);
        // Adiciona o objeto `alunoAtualizar` à Intent, para que os dados sejam
        // carregados na tela de cadastro e possam ser editados.
        it.putExtra("aluno", alunoAtualizar);
        // Inicia a Activity de cadastro (MainActivity) com os dados do aluno selecionado.
        startActivity(it);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega todos os alunos do banco de dados
        alunos = dao.obterTodos();
        // Limpa a lista filtrada e adiciona os novos alunos
        alunosFiltrados.clear();
        alunosFiltrados.addAll(alunos);
        // Atualiza o adapter da ListView para refletir os novos dados
        ArrayAdapter<Aluno> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, alunosFiltrados);
        listView.setAdapter(adaptador);
    }
}