package com.example.projetoum;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AlunoDAO {
    private Conexao conexao;
    private SQLiteDatabase banco;

    private static final Pattern TELEFONE_PATTERN = Pattern.compile("^\\(?([1-9]{2})\\)?\\s?9[0-9]{4}-?[0-9]{4}$");
    public AlunoDAO(Context context){
        // Instancia a classe auxiliar de conexão que criamos (OpenHelper)
        conexao = new Conexao(context);

        // Solicita ao sistema permissão de ESCRITA no arquivo do banco de dados.
        // Se o banco não existir, o método onCreate da classe Conexao será disparado.
        banco = conexao.getWritableDatabase();
    }


    public long Inserir(Aluno aluno){
        if (!cpfExistente(aluno.getCpf())){
            ContentValues values = new ContentValues();
            values.put("nome",aluno.getNome());
            values.put("cpf",aluno.getCpf());
            values.put("telefone",aluno.getTelefone());
            values.put("endereco",aluno.getEndereco());
            values.put("curso",aluno.getCurso());
            values.put("fotoBytes",aluno.getFotoBytes());//FOTO
            return banco.insert("aluno", null, values);
        }
        else{
            return -1;
        }

    }

    public List<Aluno> obterTodos(){
        List<Aluno> alunos = new ArrayList<>();
        //cursor aponta para as linhas retornadas
        Cursor cursor = banco.query("aluno", new String[]{"id", "nome", "cpf", "telefone", "fotoBytes"},
                null, null,null,null,null); //nome da tabela, nome das colunas, completa com null o método
        //que por padrão pede esse número de colunas obrigatórias
        while(cursor.moveToNext()){ //verifica se consegue mover para o próximo ponteiro ou linha
            Aluno a = new Aluno();
            a.setId(cursor.getInt(0)); // new String[]{"id", "nome", "cpf", "telefone"}, id é coluna '0'
            a.setNome(cursor.getString(1)); // new String[]{"id", "nome", "cpf", "telefone"}, nome é coluna '1'
            a.setCpf(cursor.getString(2)); // new String[]{"id", "nome", "cpf", "telefone"}, cpf é coluna '2'
            a.setTelefone(cursor.getString(3)); // new String[]{"id", "nome", "cpf", "telefone"}, telefone é coluna '3'
            a.setFotoBytes(cursor.getBlob(4));
            alunos.add(a);
        }
        return alunos;

    }

    public boolean cpfExistente(String cpf) {
        // Consulta no banco de dados para verificar se o CPF já existe
        Cursor cursor = banco.query("aluno", new String[]{"id"}, "cpf = ?", new String[]{cpf}, null, null, null);
        boolean cpfExiste = cursor.getCount() > 0;
        cursor.close();
        return cpfExiste;
    }

    public boolean validaTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            return false;
        }

        // 1. Tenta validar o formato com a expressão regular primeiro.
        //    Isso aceita formatos como (11) 98888-7777, 11 988887777, etc.
        if (TELEFONE_PATTERN.matcher(telefone).matches()) {
            return true;
        }

        // 2. Se o formato exato falhar (por exemplo, o usuário digitou apenas números),
        //    removemos os caracteres não numéricos para uma verificação final.
        String apenasDigitos = telefone.replaceAll("[^0-9]", "");

        // 3. Verifica se o resultado tem exatamente 11 dígitos e o terceiro dígito é '9'.
        //    (DDD com 2 dígitos + '9' + número com 8 dígitos)
        if (apenasDigitos.length() == 11 && apenasDigitos.charAt(2) == '9') {
            return true;
        }

        // Se nenhuma das validações passar, o telefone é inválido.
        return false;
    }

    public boolean validaCpf(String CPF) {
        // Exibe a entrada recebida (usado para depuração)
        System.out.println("String de entrada do método: " + CPF);

        // Remove espaços e caracteres não numéricos (caso tenha sido digitado com . ou -)
        CPF = CPF.replaceAll("[^0-9]", "");

        // Verifica se o CPF tem exatamente 11 dígitos
        if (CPF.length() != 11) {
            return false;
        }

        // Verifica se o CPF não é uma sequência repetida (como 00000000000, 11111111111, etc.)
        /*
        \\d	Representa qualquer número de 0 a 9
        (\\d)	Captura o primeiro dígito do CPF
        \\1	Repete o mesmo dígito capturado antes
        {10}	Exige que o mesmo dígito apareça mais 10 vezes, totalizando 11 dígitos iguais */
        if (CPF.matches("(\\d)\\1{10}")) {
            return false;
        }

        char dig10, dig11; // Variáveis para armazenar os dígitos verificadores
        int soma, num, peso, resto;

        try {
            // Cálculo do Primeiro Dígito Verificador (D1)
            soma = 0;
            peso = 10; // O primeiro peso começa em 10 e vai diminuindo até 2

            for (int i = 0; i < 9; i++) {
                num = CPF.charAt(i) - '0'; // Converte o caractere numérico para inteiro
                soma += (num * peso); // Multiplica pelo peso correspondente e soma
                peso--; // Diminui o peso
            }

            resto = soma % 11;
            dig10 = (resto < 2) ? '0' : (char) ((11 - resto) + '0'); // Se resto < 2, D1 = 0, senão D1 = 11 - resto

            // Cálculo do Segundo Dígito Verificador (D2)
            soma = 0;
            peso = 11; // Agora os pesos começam em 11 e vão até 2

            for (int i = 0; i < 10; i++) { // Inclui o primeiro dígito verificador já calculado
                num = CPF.charAt(i) - '0'; // Converte o caractere numérico para inteiro
                soma += (num * peso); // Multiplica pelo peso correspondente e soma
                peso--; // Diminui o peso
            }

            resto = soma % 11;
            dig11 = (resto < 2) ? '0' : (char) ((11 - resto) + '0'); // Se resto < 2, D2 = 0, senão D2 = 11 - resto

            // Comparação dos Dígitos Verificadores
            return (dig10 == (char) (CPF.charAt(9))) && (dig11 == (char) (CPF.charAt(10))); // Verifica se os dígitos calculados são iguais aos do CPF informado
            //se for verdadeiro retorna true

        } catch (Exception e) { // Captura qualquer erro inesperado
            return false;
        }
    }


    public void excluir(Aluno a){
        banco.delete("aluno", "id = ?",new String[]{a.getId().toString()}); // no lugar do ? vai colocar o id do aluno
    }

    //-------------------- ATUALIZAR -------------------------------------------------------//
    public void atualizar(Aluno aluno){
        ContentValues values = new ContentValues(); //valores que irei inserir
        values.put("nome", aluno.getNome());
        values.put("cpf", aluno.getCpf());
        values.put("telefone", aluno.getTelefone());
        values.put("fotoBytes", aluno.getFotoBytes());//foto
        banco.update("aluno", values, "id = ?", new String[]{aluno.getId().toString()});
    }

}
