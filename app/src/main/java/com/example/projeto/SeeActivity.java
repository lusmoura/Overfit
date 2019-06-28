package com.example.projeto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SeeActivity extends AppCompatActivity {

    private ImageButton back_button;
    private ImageButton confirm_button;
    private TextView name;
    private TextView intensity;
    private TextView xp;
    private TextView desc;
    private ImageView image;

    /**
     * Inicaliza a tela de de visualizacão de atividade
     * @param savedInstanceState: Usado para salvar o estado da activity,
     *                            para o usuário poder voltar para o mesmo estado
     *                            da tela no futuro
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see);

        initializeFields();
        getActivityData();
        configureConfirmButton();
        configureCloseButton();
    }

    /**
     * Inicializa os campos da tela: Nome, intensidade
     * xp, imagem e descricão
     */
    private void initializeFields() {
        name = findViewById(R.id.activityName);
        intensity = findViewById(R.id.intensityText);
        xp = findViewById(R.id.xpText);
        image = findViewById(R.id.activityImage);
        desc = findViewById(R.id.activityDescription);
    }

    /**
     * Pega os dados da atividade selecionada que serão
     * Monstrados na tela
     */
    private void getActivityData() {
        final String TAG = "result";
        Intent intent = getIntent();
        String type = intent.getSerializableExtra("type").toString();
        int id = (int) intent.getSerializableExtra("activityId");
        String code = getResources().getResourceEntryName(id);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("exercises").document(type + code.substring(code.length()-1));

        //Usa a firebase para pegar os dados da atividade selecionada:
        //Nome, intensidade, path para a imagem, XP por exercício e descricão
        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            name.setText((CharSequence) document.get("Name"));
                            intensity.setText((CharSequence) document.get("Intensity"));
                            image.setImageResource(ChooseActivity.getImageId(SeeActivity.this, document.get("path").toString()));
                            xp.setText((CharSequence) document.get("XP"));
                            desc.setText((CharSequence) document.get("Description"));
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    /**
     * Configura o botão de fechar a tela e retornar para a selecão de atividade
     */
    private void configureCloseButton() {
        back_button = findViewById(R.id.closeButton);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Configura o botão que submete a quantidade de exercício feita e atualiza
     * a quantidade de experiência ganha no dia
     */
    private void configureConfirmButton() {
        confirm_button = findViewById(R.id.confirmButton);
        final EditText userInfo = findViewById(R.id.userInfo);

        //testa se o valor colocado pelo usuário é válido, se for
        //Computa o total de experiência que ele ganhou usando o XP
        //por exercício e a quantidade de exercício que ele fez,
        //por fiz atualiza o xp total, e o xp do dia
        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor user = getSharedPreferences("user", Context.MODE_PRIVATE).edit();
                SharedPreferences userLoad = getSharedPreferences("user", Context.MODE_PRIVATE);

                int exerciseDone;
                try {
                    exerciseDone = Integer.parseInt(userInfo.getText().toString());
                } catch (Exception e) {
                    return;
                }

                int xpFactor = Integer.parseInt(xp.getText().toString().split(" ")[1]);
                int totalXP = xpFactor * exerciseDone;

                user.putInt("ex1", userLoad.getInt("ex1", -1) + totalXP);
                user.putInt("totalXP", userLoad.getInt("totalXP", -1) + totalXP);
                user.apply();
                finish();
            }
        });
    }
}
