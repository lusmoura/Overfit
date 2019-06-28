package com.example.projeto;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class GraphActivity extends AppCompatActivity {

    private SharedPreferences userLoad;
    private GraphView graph;

    /**
     * Inicializa a tela que contém o acompanhamento do usuário
     * @param savedInstanceState Usado para salvar o estado da acvivity,
     *      *                    para o usuário poder voltar para o mesmo estado
     *      *                    da tela no futuro
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);

        showIMC();
        showMaxXp();
        showTotalXP();
        showStreak();
        configureGraph();
        configureTrainButton();
    }

    /**
     * Calcula e mostra imc do usuário
     */
    private void showIMC() {
        userLoad = getSharedPreferences("user", Context.MODE_PRIVATE);
        TextView imcView = findViewById(R.id.imcValue);

        //pega os valores de peso e altura do usuário
        float weight = userLoad.getFloat("weight", -1);
        float height = userLoad.getFloat("height", -1);

        //fórmula do IMC
        float imc = weight / (height*height);
        String imcString = String.format ("%,.2f", imc);

        imcView.setText(imcString);
    }

    /**
     * Mostra o xp máximo já obtido pelo usuário
     */
    private void showMaxXp() {
        userLoad = getSharedPreferences("user", Context.MODE_PRIVATE);
        TextView maxXpView = findViewById(R.id.maxXpValue);

        int maxXP = userLoad.getInt("maxXP", 0);
        String maxXPString = String.valueOf(maxXP);

        maxXpView.setText(maxXPString);
    }

    /**
     * Mostra o xp total obtido pelo usuário
     */
    private void showTotalXP() {
        userLoad = getSharedPreferences("user", Context.MODE_PRIVATE);
        TextView totalXpView = findViewById(R.id.totalXPValue);

        int totalXP = userLoad.getInt("totalXP", 0);
        String totalXPString = String.valueOf(totalXP);

        totalXpView.setText(totalXPString);
    }

    /**
     * Mostra a quantidade de dias seguidos de treino
     */
    private void showStreak() {
        userLoad = getSharedPreferences("user", Context.MODE_PRIVATE);
        String streakString = Integer.toString(userLoad.getInt("diasTreino", -1));

        TextView streak = findViewById(R.id.streak);
        streak.setText(streakString);
    }

    /**
     * Cria e configura o gráfico que mostra o xp dos últimos 14 dias
     */
    public void configureGraph() {
        graph = findViewById(R.id.graph);

        //cria um gráfico para mostrar o acompanhamento do usuário
        LineGraphSeries<DataPoint> series = crateLinearGraph();

        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);

        setGraphMinMax();
        configureGraphStyle(series);

        graph.addSeries(series);
    }

    /**
     * Inicializa uma série para plotar gráfico como linha
     * @return Série para plotar o gráfico como linha
     */
    private LineGraphSeries crateLinearGraph() {
        //retorna um gráfico de linha com os pontos representando os últimos 14 dias de atividade do usuário
        return new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(userLoad.getInt("diasUsados", -1) - 13, userLoad.getInt("ex14", -1)),
                new DataPoint(userLoad.getInt("diasUsados", -1) - 12, userLoad.getInt("ex13", -1)),
                new DataPoint(userLoad.getInt("diasUsados", -1) - 11, userLoad.getInt("ex12", -1)),
                new DataPoint(userLoad.getInt("diasUsados", -1) - 10, userLoad.getInt("ex11", -1)),
                new DataPoint(userLoad.getInt("diasUsados", -1) - 9, userLoad.getInt("ex10", -1)),
                new DataPoint(userLoad.getInt("diasUsados", -1) - 8, userLoad.getInt("ex9", -1)),
                new DataPoint(userLoad.getInt("diasUsados", -1) - 7, userLoad.getInt("ex8", -1)),
                new DataPoint(userLoad.getInt("diasUsados", -1) - 6, userLoad.getInt("ex7", -1)),
                new DataPoint(userLoad.getInt("diasUsados", -1) - 5, userLoad.getInt("ex6", -1)),
                new DataPoint(userLoad.getInt("diasUsados", -1) - 4, userLoad.getInt("ex5", -1)),
                new DataPoint(userLoad.getInt("diasUsados", -1) - 3, userLoad.getInt("ex4", -1)),
                new DataPoint(userLoad.getInt("diasUsados", -1) - 2, userLoad.getInt("ex3", -1)),
                new DataPoint(userLoad.getInt("diasUsados", -1) - 1, userLoad.getInt("ex2", -1)),
                new DataPoint(userLoad.getInt("diasUsados", -1), userLoad.getInt("ex1", -1)),

        });
    }

    /**
     * Define valores mínimos e máximos dos eixos do gráfico
     */
    private void setGraphMinMax() {
        if((userLoad.getInt("diasUsados", - 1) - 13) >= 1){
            graph.getViewport().setMinX(userLoad.getInt("diasUsados", - 1) - 13);
            graph.getViewport().setMaxX(userLoad.getInt("diasUsados", - 1));
        } else {
            graph.getViewport().setMinX(1);
            graph.getViewport().setMaxX(14);
        }
    }

    /**
     * Define estilo do gráfico
     * @param series Série para plotar o gráfico como linha
     */
    private void configureGraphStyle(LineGraphSeries series) {
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getGridLabelRenderer().setNumHorizontalLabels(14);
        series.setColor(Color.MAGENTA);
        series.setThickness(8);
    }

    /**
     * Configura botão do treino, definindo
     * que haverá uma mudança para a MainActivity
     */
    public void configureTrainButton() {
        ImageButton trainButton = findViewById(R.id.treinoButton);

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}