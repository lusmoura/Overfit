package com.example.projeto;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import io.opencensus.tags.Tag;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Spinner;
import android.content.Context;

import android.content.SharedPreferences;

import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private EditText name;
    private EditText age;
    private EditText weight;
    private EditText height;
    private Button confirmButton;
    private Spinner goal;
    private int ageV;
    private float weightV;
    private float heightV;
    private Date todayDate;
    private Date firstUsedDate;

    /**
     * Inicaliza a tela de Login
     * e salva as informacões do usuário
     * @param savedInstanceState: Usado para salvar o estado da activity,
     *                            para o usuário poder voltar para o mesmo estado
     *                            da tela no futuro
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeFields(); // Inicializa os campos
        checkUserExists();  // Trata os erros de preenchimento de informacão do usuário
        configureSpinner(); // Configura Spinner
    }

    /**
     * Inicializa os campos da tela
     */
    private void initializeFields() {
        name = findViewById(R.id.userName);
        age = findViewById(R.id.userAge);
        weight = findViewById(R.id.userWeight);
        height = findViewById(R.id.userHeight);
        confirmButton = findViewById(R.id.confirm);
        goal = findViewById(R.id.selectIntensitySpinner);
    }

    /**
     * Checa se o usuário
     * já existe, se não continua na tela de criacão
     * Se já existe vai para a tela Main
     */
    private void checkUserExists() {
        SharedPreferences userLoad = getSharedPreferences("user", Context.MODE_PRIVATE);
        String ERROR = "404";

        //Se o usuário tiver preenchido o campo nome, segue para a tela main
        if(!ERROR.equals(userLoad.getString("name", "404"))){
            Intent intent = new Intent( this , MainActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Torna mais natural o uso do spinner,
     * escondendo o teclado
     */
    private void configureSpinner() {
        goal.setOnTouchListener(new View.OnTouchListener() {

            // esconde teclado
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager in = (InputMethodManager) v.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(v.getApplicationWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                return false;
            }
        }) ;
    }

    /**
     *Método chamado quando o usuário volta
     * para a tela
     */
    @Override
    public void onResume(){
        super.onResume();

        checkUserOnResume(); // checa se há algum problema com usuário
        confirmClicked();    // clique no botão de confirmar
    }

    /**
     * Checa se o usuário
     * já existe, e mostra seus dados na tela
     * Se ele não existir o programa continua para inserir os dados
     */
    private void checkUserOnResume() {
        SharedPreferences userLoad = getSharedPreferences("user", Context.MODE_PRIVATE);
        String ERROR = "404";

        if (!ERROR.equals(userLoad.getString("name", "404"))){

            String ageNum = Integer.toString(userLoad.getInt("age", -1));
            String weightNum = Float.toString(userLoad.getFloat("weight", -1));
            String heightNum = Float.toString(userLoad.getFloat("height", -1));

            name.setText(userLoad.getString("name", ""));
            age.setText(ageNum);
            weight.setText(weightNum);
            height.setText(heightNum);
        }
    }

    /**
     *Confirma se os parâmetros que o usuário preecheu são
     * válidos, se sim salva eles e instancia a tela principal
     * Se não avisa o usuário e espera ele pôr valores válidos
     */
    private void confirmClicked() {
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInvalidString(name)) return;

                // checa se há algum valor inválido
                ageV = Math.round(getValue(age, "Idade Inválida"));
                weightV = getValue(height, "Altura Inválida");
                heightV = getValue(weight, "Peso Inválido");
                if(ageV == -1 || heightV <= 0 || weightV <= 0) return;


                editProfile(); // edição de perfil

                Intent intent = new Intent( v.getContext() , MainActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Converte o valor do campo para um float
     * Se o valor for inválido, retorna -1 e mostra a mensagem de erro
     * @param field campo que queremos o valor
     * @param errorMessage mensagem de erro caso o campo seja inválido
     * @return o valor do campo como float ou -1 se o campo tiver um valor inválido
     */
    private float getValue(EditText field, String errorMessage) {
        float value = -1;

        try{
            value = Float.parseFloat(field.getText().toString());
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
            return value;
        }

        return value;
    }

    /**
     * Checa se o campo é uma string vazia
     * @param field o campo que será checado
     * @return true se o campo é vazio, false do contrário
     */
    private boolean isInvalidString(EditText field) {
        if (field.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Nome Inválido", Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
    }

    /**
     * Atualiza o perfil do usuário com os dados preenchidos por ele
     * Se o perfil não existia antes cria ele
     */
    private void editProfile() {
        SharedPreferences userLoad = getSharedPreferences("user", Context.MODE_PRIVATE);
        String ERROR = "404";

        if(ERROR.equals(userLoad.getString("name", "404"))) {
            createProfile(ageV, weightV, heightV);
        } else {
            updateProfile(ageV, weightV, heightV);
        }
    }

    /**
     * Inicializa a data do aplicativo
     */
    private void configureDate(){
        Calendar today = Calendar.getInstance();

        SharedPreferences userLoad = getSharedPreferences("user", Context.MODE_PRIVATE);

        int day = today.get(Calendar.DAY_OF_MONTH);
        int month = today.get(Calendar.MONTH) + 1;
        int year = today.get(Calendar.YEAR);
        String todayString = year + "-" + month + "-" + day;
        String firstUsedString = userLoad.getString("date", "");

        todayDate = getDate(todayString);
        firstUsedDate = getDate(firstUsedString);

        assert todayDate != null;
        assert firstUsedDate != null;

        treatErrorOnDate(todayString);
    }

    /**
     * Transforma uma string que representa uma data em um tipo Date
     * @param dateString String que representa a data
     * @return Retorna uma variável do tipo Date com a data representada pelo dateString
     */
    private Date getDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    /**
     * Atualiza a data e informa o usuário caso o calendário
     * esteja corrrompido
     * @param todayString String que representa o dia de hoje
     */
    private void treatErrorOnDate(String todayString) {
        if ((int) ((todayDate.getTime() - firstUsedDate.getTime())/1000/60/60/24) > 0) {

            SharedPreferences.Editor user = getSharedPreferences("user", Context.MODE_PRIVATE).edit();
            user.putString("date", todayString);
            user.apply();

        } else if (todayDate.compareTo(firstUsedDate) < 0){
            Toast.makeText(getApplicationContext(), "Erro na data.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Cria um novo perfil de usuário
     * @param ageV Valor da idade do usuário
     * @param weightV Valor do peso do usuário
     * @param heightV Valor da altura do usuário
     */
    public void createProfile(int ageV, Float weightV, Float heightV){
        //Cria um SharedPreferences onde guardaremos os dados do usuário
        SharedPreferences.Editor user = getSharedPreferences("user", Context.MODE_PRIVATE).edit();

        //Grava nome, idade, peso, altura, propósito do usuário
        //E valores padrões para data inicial, XP total e Máxima XP de um dia
        user.putString("name", name.getText().toString());
        user.putInt("age", ageV);
        user.putFloat("weight", weightV);
        user.putFloat("height", heightV);
        user.putString("goal", goal.getSelectedItem().toString());
        user.putString("date", "2015-01-01");
        user.putInt("totalXP", 0);
        user.putInt("maxXP", 0);

        //Coloca pontos iniciais para o gráfico de acompanhamento do usuário
        user.putInt("ex1", 0);
        user.putInt("ex2", 0);
        user.putInt("ex3", 0);
        user.putInt("ex4", 0);
        user.putInt("ex5", 0);
        user.putInt("ex6", 0);
        user.putInt("ex7", 0);
        user.putInt("ex8", 0);
        user.putInt("ex9", 0);
        user.putInt("ex10", 0);
        user.putInt("ex11", 0);
        user.putInt("ex12", 0);
        user.putInt("ex13", 0);
        user.putInt("ex14", 0);

        //Valores iniciais para dias usados e de treino
        user.putInt("diasUsados", 1);
        user.putInt("diasTreino", 0);

        //Inscreve o usuário no tópico de notificacões de sua nova meta
        subscribeTopics();

        user.apply();

        configureDate();
    }

    /**
     * Atualiza o perfil do usuário, inclusive suas preferencias na firebase
     * @param ageV Valor da idade do usuário
     * @param weightV Valor do peso do usuário
     * @param heightV Valor da altura do usuário
     */
    public void updateProfile(int ageV, Float weightV, Float heightV){
        SharedPreferences.Editor user = getSharedPreferences("user", Context.MODE_PRIVATE).edit();

        user.putString("name", name.getText().toString());
        user.putInt("age", ageV);
        user.putFloat("weight", weightV);
        user.putFloat("height", heightV);
        user.putString("goal", goal.getSelectedItem().toString());


        //Inscreve o usuário no tópico de notificacões de sua nova meta
        subscribeTopics();
        user.apply();
    }

    /**
     *Inscreve o usuário no tópico de notificacões de sua nova meta
     */
    private void subscribeTopics(){
        //Inscreve o usuário no tópico de notificacões de sua nova meta na Firebase
        FirebaseMessaging.getInstance().subscribeToTopic(goal.getSelectedItem().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String TAG = "status";
                        String msg = "Intensidade Confirmada";
                        if (!task.isSuccessful()) {
                            msg = "Sem acesso a internet";
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

    }
}

