package com.example.projeto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity{

    final private String TAG = "token";
    private  SharedPreferences userLoad;
    private Button aerobicButton;
    private Button weightLiftingButton;
    private ImageButton configButton;
    private ImageButton graphButton;

    /**
     * Inicializa a tela principal,
     * na qual se esolhe o tipo de atividade
     * @param savedInstanceState Usado para salvar o estado da acvivity,
     *                           para o usuário poder voltar para o mesmo estado
     *                           da tela no futuro
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureDate(); // configura informações de tempo decorrido
        configureButtons(); // configura botões
        updateFirebaseToken(); // atualiza valores com firebase
        configureGraphButton(); // configura botão do gráfico
    }

    /**
     * Configura botão do gráfico, definindo
     * que haverá uma mudança para a GraphicActivity
     */
    private void configureGraphButton() {
        graphButton = findViewById(R.id.graficoButton);

        graphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GraphActivity.class));
            }
        });
    }

    /**
     * Inicializa botões da tela e chama métodos
     * de ativação de cada um deles
     */
    private void configureButtons() {
        aerobicButton = findViewById(R.id.aerobicoButton);
        weightLiftingButton = findViewById(R.id.muscularButton);
        configButton = findViewById(R.id.backButton);

        aerobicClicked();
        weightLiftingClicked();
        configClicked();
    }

    /**
     * Ao clicar no botão Aeróbico, é inicializada a activity de
     * escolher um exercício e é definido um id no intent
     */
    private void aerobicClicked() {
        aerobicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( view.getContext() , ChooseActivity.class);
                intent.putExtra("idButton", "aerobic");
                startActivity(intent);
            }
        });
    }

    /**
     * Ao clicar no botão Muscular, é inicializada a activity de
     * escolher um exercício e é definido um id no intent
     */
    private void weightLiftingClicked() {
        weightLiftingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( view.getContext() , ChooseActivity.class);
                intent.putExtra("idButton", "weightlifting");
                startActivity(intent);
            }
        });
    }

    /**
     * Ao clicar no botão de configuração, a activity atual
     * é encerrada
     */
    private void configClicked() {
        configButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    /**
     * Caso o token seja atualizado, manda ele para a Firebase
     */
    private void updateFirebaseToken(){
        userLoad = getSharedPreferences("user", Context.MODE_PRIVATE);
        String goal = userLoad.getString("goal", "");

        //Tenta atualizar o token do aplicativo na firebase e loga o resultado
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Recebe novo id da instancia
                        String token = task.getResult().getToken();

                        // Log e toast
                        String msg = "Updated Token";
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    /**
     * Checa se a data atual é anterior ao primeiro dia utilizado.
     * Também chama funções que atualizam dia, pontos do gráfico e streak
     */
    private void configureDate(){
        Calendar today = Calendar.getInstance();

        userLoad = getSharedPreferences("user", Context.MODE_PRIVATE);

        // recebe dados da data e transforma em string
        int day = today.get(Calendar.DAY_OF_MONTH);
        int month = today.get(Calendar.MONTH) + 1;
        int year = today.get(Calendar.YEAR);
        String todayString = year + "-" + month + "-" + day;
        String firstUsedString = userLoad.getString("date", "");

        // gera Date a partir da string
        Date todayDate = getDate(todayString);
        Date firstUsedDate = getDate(firstUsedString);

        // checa se datas não são nulas
        assert todayDate != null;
        assert firstUsedDate != null;

        // se a data atual for anterior à primeira utilização do aplicativo, houve um erro
        if (todayDate.compareTo(firstUsedDate) < 0) {
            Toast.makeText(getApplicationContext(), "Erro na data.", Toast.LENGTH_LONG).show();

         // se a data for posterior à primeira utilização, então são feitas as atualizações necessárias
        } else if (todayDate.compareTo(firstUsedDate) > 0){
            SharedPreferences.Editor user = getSharedPreferences("user", Context.MODE_PRIVATE).edit();

            int days = (int) ((todayDate.getTime() - firstUsedDate.getTime())/1000/60/60/24);
            goToNextDay(user, days);
            updateStreak(user);

            user.putString("date", todayString);
            user.apply();
        }
    }

    /**
     * Atualiza xp máximo e "desloca" xp, criando um novo dia e
     * atualizando os anteriores
     * @param user usuário
     * @param days número de dias decorridos desde o último dia utilizado
     */
    private void goToNextDay(SharedPreferences.Editor user, int days) {
        userLoad = getSharedPreferences("user", Context.MODE_PRIVATE);

        updateMaxXp(user);

        // move os dados para o dia adequado
        for (int i = 0; i < days; i++) {
            user.putInt("ex14", userLoad.getInt("ex13", -1));
            user.putInt("ex13", userLoad.getInt("ex12", -1));
            user.putInt("ex12", userLoad.getInt("ex11", -1));
            user.putInt("ex11", userLoad.getInt("ex10", -1));
            user.putInt("ex10", userLoad.getInt("ex9", -1));
            user.putInt("ex9", userLoad.getInt("ex8", -1));
            user.putInt("ex8", userLoad.getInt("ex7", -1));
            user.putInt("ex7", userLoad.getInt("ex6", -1));
            user.putInt("ex6", userLoad.getInt("ex5", -1));
            user.putInt("ex5", userLoad.getInt("ex4", -1));
            user.putInt("ex4", userLoad.getInt("ex3", -1));
            user.putInt("ex3", userLoad.getInt("ex2", -1));
            user.putInt("ex2", userLoad.getInt("ex1", -1));
            user.putInt("ex1", 0);
            user.putInt("diasUsados", userLoad.getInt("diasUsados", -1) + 1);
            user.apply();
        }
    }

    /**
     * Atualiza o xp máximo ocorrido ao longo do uso
     * @param user usuário
     */
    private void updateMaxXp(SharedPreferences.Editor user) {
        userLoad = getSharedPreferences("user", Context.MODE_PRIVATE);

        int currMax = userLoad.getInt("maxXP", 0);
        int todayXP = userLoad.getInt("ex1", 0);

        if (currMax < todayXP) {
            user.putInt("maxXP", todayXP);
        }

        user.apply();
    }

    /**
     * Atualiza o streak do usuário
     * @param user usuário
     */
    private void updateStreak(SharedPreferences.Editor user) {
        // se o usuário utilizou na data atual, o streak é atualizado
        if(userLoad.getInt("ex2", -1) > 0){
            user.putInt("diasTreino", userLoad.getInt("diasTreino", -1) + 1);
        // se ele não utilizar, o streak é zerado
        } else {
            user.putInt("diasTreino", 0);
        }
    }

    /**
     * Recebe uma string e a retorna em forma de Date
     * @param dateString string da data
     * @return data formatada
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
}