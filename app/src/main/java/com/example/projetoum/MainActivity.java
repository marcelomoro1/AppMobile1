package com.example.projetoum;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
private byte[] fotoBytesDigitada = null;


//Configuracoes para a camera
    private ImageView imageView;

    // Código de identificação para a solicitação de PERMISSÃO da câmera.
    // É usado no método 'tirarFoto' para pedir autorização e
    // verificado no 'onRequestPermissionsResult' para saber se o usuário aceitou.
    private static final int CAMERA_PERMISSION_CODE = 100;

    // Código de identificação para a CAPTURA DA IMAGEM (a foto em si).
    // É usado no método 'startCamera' ao iniciar a intenção da câmera e
    // verificado no 'onActivityResult' para garantir que os dados recebidos são da foto.
    private static final int REQUEST_IMAGE_CAPTURE = 200;

    public void tirarFoto(View view) {
        // Verifica se a permissão de CAMERA já foi concedida anteriormente
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Se NÃO tem permissão, abre a janelinha do sistema pedindo a autorização.
            // O código CAMERA_PERMISSION_CODE (100) serve para identificarmos esta resposta depois.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            // Se JÁ tem permissão, chama o método para abrir a câmera de fato.
            startCamera();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Pega a miniatura da foto enviada pela câmera
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // 1. Mostra a foto na tela (isso resolve seu problema de não aparecer)
            imageView.setImageBitmap(imageBitmap);

            // 2. Converte para byte[] para que o banco de dados possa salvar
            java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            fotoBytesDigitada = stream.toByteArray();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Verifica se a resposta que chegou é referente ao nosso pedido de câmera (código 100)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            // Verifica se o array de resultados não está vazio e se o usuário clicou em "Permitir"
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("CAMERA_DEBUG", "Usuário permitiu, abrindo câmera...");
                startCamera();
            } else {
                // Se o usuário negou, avisamos que ele não conseguirá tirar fotos.
                Toast.makeText(this, "A permissão é necessária para usar a câmera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        try {
            // Cria uma Intent (intenção) para capturar uma imagem.
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Inicia a atividade da câmera esperando um resultado (a foto).
            // O código REQUEST_IMAGE_CAPTURE (200) serve para identificarmos esta foto quando ela voltar.
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            Log.e("CAMERA_DEBUG", "Erro ao abrir a câmera: " + e.getMessage());
            Toast.makeText(this, "Erro ao abrir a câmera no seu dispositivo.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // CAMERA - Vincula a ImageView no layout para mostrar a foto
        imageView = findViewById(R.id.imageView);


        // Vinculando os componentes do XML com as variáveis Java (ID deve ser igual ao do XML)
        nome = findViewById(R.id.texto1);
        cpf = findViewById(R.id.texto2);
        telefone = findViewById(R.id.texto3);
        endereco = findViewById(R.id.texto4);
        curso = findViewById(R.id.texto5);
        // Vincula o botão do layout com a variável 'btnTakePhoto'.
        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);

        Intent it = getIntent(); //pega intenção
        if(it.hasExtra("aluno")) {
            aluno = (Aluno) it.getSerializableExtra("aluno");
            nome.setText(aluno.getNome().toString());
            cpf.setText(aluno.getCpf());
            telefone.setText(aluno.getTelefone());

            // Carregar a foto no ImageView no momento que carregar os dados para atualizar
            byte[] fotoBytes = aluno.getFotoBytes();
            if (fotoBytes != null && fotoBytes.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.length);
                imageView.setImageBitmap(bitmap);


            }
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



        if(aluno==null || aluno.getId() == null) { //SE NAO RECEBEU DADOS DO ATUALIZAR É ESTA COMO 'NULL', CADASTRA O ALUNO
            Aluno a = new Aluno();


            // 2. Coletamos os dados digitados nos EditText e convertemos para String
            a.setNome(nome.getText().toString());
            a.setCpf(cpf.getText().toString());
            a.setTelefone(telefone.getText().toString());
            a.setEndereco(endereco.getText().toString());
            a.setCurso(curso.getText().toString());
            a.setFotoBytes(fotoBytesDigitada);

            long id = dao.Inserir(a);

            if (id == -1) {
                // O método Inserir retorna -1 quando o CPF já existe no banco
                Toast.makeText(this, "Erro: Este CPF já está cadastrado!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Aluno cadastrado com sucesso! ID: " + id, Toast.LENGTH_SHORT).show();
            }
        }
        else {
            //RECEBENDO DADOS DO ATUALIZAR NA VARIAVEL 'aluno'
            //seta os valores de novo e atualiza
            aluno.setNome(nome.getText().toString());
            aluno.setCpf(cpf.getText().toString());
            aluno.setTelefone(telefone.getText().toString());
            if(fotoBytesDigitada != null) {
                aluno.setFotoBytes(fotoBytesDigitada);
            }

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