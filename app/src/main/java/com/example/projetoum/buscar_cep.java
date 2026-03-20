package com.example.projetoum;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class buscar_cep extends AppCompatActivity {

    private EditText textoCep;
    private EditText textoLogradouro;
    private EditText textoBairro;
    private EditText textoCidade;
    private EditText textoEstado;

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buscar_cep);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textoCep = findViewById(R.id.textoCep);
        textoLogradouro = findViewById(R.id.textoLogradouro);
        textoBairro = findViewById(R.id.textoBairro);
        textoCidade = findViewById(R.id.textoCidade);
        textoEstado = findViewById(R.id.textoEstado);

        executorService = Executors.newSingleThreadExecutor();
    }

    public void buscarCep(View view) {
        String cepString = textoCep.getText().toString().replaceAll("[^0-9]", "");

        if (cepString.length() != 8) {
            Toast.makeText(this, "CEP inválido! Digite 8 números.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Buscando CEP...", Toast.LENGTH_SHORT).show();

        executorService.execute(() -> {
            try {
                URL apiEnd = new URL("https://viacep.com.br/ws/" + cepString + "/json/");
                HttpURLConnection conexao = (HttpURLConnection) apiEnd.openConnection();
                conexao.setRequestMethod("GET");
                conexao.setReadTimeout(10000);
                conexao.setConnectTimeout(15000);
                conexao.connect();

                int resposta = conexao.getResponseCode();
                if (resposta == HttpURLConnection.HTTP_OK) {
                    BufferedReader dados = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
                    StringBuilder jsonRetorno = new StringBuilder();
                    String linha;
                    while ((linha = dados.readLine()) != null) {
                        jsonRetorno.append(linha);
                    }

                    // Parse JSON
                    JSONObject jsonObject = new JSONObject(jsonRetorno.toString());

                    // Verifica se o CEP não existe
                    if (jsonObject.has("erro") && jsonObject.getBoolean("erro")) {
                        runOnUiThread(() -> Toast.makeText(buscar_cep.this, "CEP não encontrado!", Toast.LENGTH_SHORT).show());
                    } else {
                        // Extrai os dados
                        String logradouro = jsonObject.optString("logradouro", "");
                        String bairro = jsonObject.optString("bairro", "");
                        String cidade = jsonObject.optString("localidade", ""); // ViaCEP usa "localidade" para cidade
                        String estado = jsonObject.optString("uf", "");

                        // Atualiza a UI na thread principal
                        runOnUiThread(() -> {
                            textoLogradouro.setText(logradouro);
                            textoBairro.setText(bairro);
                            textoCidade.setText(cidade);
                            textoEstado.setText(estado);
                            Toast.makeText(buscar_cep.this, "Endereço encontrado!", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(buscar_cep.this, "Erro na conexão: " + resposta, Toast.LENGTH_SHORT).show());
                }
                conexao.disconnect();

            } catch (Exception e) {
                Log.e("CEP_ERROR", "Erro ao buscar CEP", e);
                runOnUiThread(() -> Toast.makeText(buscar_cep.this, "Erro ao buscar os dados.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void voltarMain(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}