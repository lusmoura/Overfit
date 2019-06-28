package com.example.projeto;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ChooseActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView[] images;
    private ImageButton[] startButtons;
    private ImageButton backButton;
    private LinearLayout[] layouts;
    private TextView[] name;
    private TextView[] xp;
    private TextView[] intensity;
    private String type;

    /**
     * Inicializa a tela de escolher a atividade
     * @param savedInstanceState Usado para salvar o estado da activity,
     *                           para o usuário poder voltar para o mesmo estado
     *                           da tela no futuro
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        initializeNames();      // inicializa nomes
        initializeXP();         // inicializa XP
        initializeIntensity();  // inicaliza intensidades
        initializeLayouts();    // inicializa layouts
        initializeImages();     // inicializa imagenss

        configureStartButtons();  // configura botões de início
        configureBackButton();    // configura botão de retorno
        setScreen();              // inicializa tela com informações das atividades aeróbicas ou musculares
    }

    /**
     * Inicializa os campos dos nomes das atividades
     */
    public void initializeNames() {
        name = new TextView[4];

        name[0] = findViewById(R.id.activityName1);
        name[1] = findViewById(R.id.activityName2);
        name[2] = findViewById(R.id.activityName3);
        name[3] = findViewById(R.id.activityName4);
    }

    /**
     * Inicializa os campos dos xp das atividades
     */
    public void initializeXP() {
        xp = new TextView[4];

        xp[0] = findViewById(R.id.xpText1);
        xp[1] = findViewById(R.id.xpText2);
        xp[2] = findViewById(R.id.xpText3);
        xp[3] = findViewById(R.id.xpText4);
    }

    /**
     * Inicializa os campos das intensidades das atividades
     */
    public void initializeIntensity() {
        intensity = new TextView[4];

        intensity[0] = findViewById(R.id.intensityText1);
        intensity[1] = findViewById(R.id.intensityText2);
        intensity[2] = findViewById(R.id.intensityText3);
        intensity[3] = findViewById(R.id.intensityText4);
    }

    /**
     * Inicializa os layouts das atividades
     */
    private void initializeLayouts() {
        layouts = new LinearLayout[4];

        layouts[0] = findViewById(R.id.activityLayout1);
        layouts[1] = findViewById(R.id.activityLayout2);
        layouts[2] = findViewById(R.id.activityLayout3);
        layouts[3] = findViewById(R.id.activityLayout4);
    }

    /**
     * Inicializa os campos das imagens das atividades
     */
    public void initializeImages() {
        images = new ImageView[4];

        images[0] = findViewById(R.id.activityImage1);
        images[1] = findViewById(R.id.activityImage2);
        images[2] = findViewById(R.id.activityImage3);
        images[3] = findViewById(R.id.activityImage4);
    }

    /**
     * Inicializa os botões das atividades
     */
    private void configureStartButtons() {
        startButtons = new ImageButton[4];

        startButtons[0] = findViewById(R.id.startButton1);
        startButtons[1] = findViewById(R.id.startButton2);
        startButtons[2] = findViewById(R.id.startButton3);
        startButtons[3] = findViewById(R.id.startButton4);

        startButtons[0].setOnClickListener(this);
        startButtons[1].setOnClickListener(this);
        startButtons[2].setOnClickListener(this);
        startButtons[3].setOnClickListener(this);
    }

    /**
     * Cnnfigura o efeito de clicar em um botão, que é
     * Ir para a tela da atividade selecionada
     * @param v botão clicado
     */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(ChooseActivity.this, SeeActivity.class);
        intent.putExtra("activityId", v.getId()); // id da atividade selecionada
        intent.putExtra("type", type); // tipo da atividade selecionada
        startActivity(intent);
    }

    /**
     * Configura o botão que retorna para a tela anterior
     */
    private void configureBackButton() {
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     *Pega os dados dos exercísios do tipos escolhido para mostrar na tela
     */
    private void setScreen() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String TAG = "DocSnippets";
        Intent intent = getIntent();

        type = intent.getSerializableExtra("idButton").toString();

        //Faz uma query na Firebase para buscar todos os exercícios do tipo selecionado
        //Pegar os dados de cada um deles e monstrá-los para o usuário
        db.collection("exercises").whereEqualTo("type", type).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int cnt = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                name[cnt].setText((CharSequence) document.get("Name"));
                                xp[cnt].setText((CharSequence) document.get("XP"));
                                intensity[cnt].setText((CharSequence) document.get("Intensity"));
                                images[cnt].setImageResource(getImageId(ChooseActivity.this, document.get("path").toString()));
                                setLayoutMargins(layouts[cnt], images[cnt].getHeight());
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                ++cnt;
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    /**
     * Posiciona as margens dos layouts dos exercícios
     * @param layout O layout que terá suas margens posicionadas
     * @param imageHeight Altura da imagem do exercíio que fica dentro do layout
     */
    public void setLayoutMargins(LinearLayout layout, int imageHeight) {
        imageHeight *= 1.8;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.setMargins(10, -imageHeight, 10, 0);
        layout.setLayoutParams(params);
    }

    /**
     * Pega o id de uma imagem a partir de parte do seu nome
     * @param context Contexto onde estamos chamando a funcão
     * @param imageName Nome da imagem
     * @return retorna o int que é o id da imagem
     */
    public static int getImageId(Context context, String imageName) {
        return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
    }
}