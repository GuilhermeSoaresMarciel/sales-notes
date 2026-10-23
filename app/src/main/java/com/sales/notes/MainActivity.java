package com.sales.notes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Handler;

public class MainActivity extends Activity {

    EditText searchInput;
    Button addButton;
    LinearLayout listLayout, contentLayout;
    TextView Title;

    String filePath;

    String listaAtual = null;
    String PessAtual = null;

    int Index_First = 0;

    int ABCbackground = Color.parseColor("#111111");
    int ABCStroke = Color.parseColor("#303640");
    int ABCText = Color.parseColor("#945757");

    private void AddColorABC(View view, int color, int color2, float cornerRadius, int strokeWidth) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(cornerRadius);
        bg.setColor(color);
        bg.setStroke(strokeWidth, color2);
        RippleDrawable ripple = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.BLACK}), bg, null);
        view.setBackground(ripple);
    }

    private void MyToast(String text) {
		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setPadding(20, 10, 20, 10);
		linearLayout.setGravity(Gravity.CENTER);
		AddColorABC(linearLayout, ABCStroke, Color.TRANSPARENT, 10, 0);

		TextView textView = new TextView(this);
		textView.setText(text);
		textView.setTextSize(16);
		textView.setTextColor(ABCText);
        textView.setTypeface(Typeface.DEFAULT_BOLD);

		linearLayout.addView(textView);

		Toast toast = Toast.makeText(this, null, Toast.LENGTH_SHORT);
		toast.setView(linearLayout);
		toast.show();
	}

	LinearLayout CreateLinearLayout() {
		LinearLayout linha = new LinearLayout(this);
		return linha;
    }

    private RotateAnimation criarRotacaoInfinita(long duracao) {
        RotateAnimation anim = new RotateAnimation(
            0, 360,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        anim.setDuration(duracao);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setInterpolator(new LinearInterpolator());
        return anim;
    }

    private void addNovaLista(String nomeLista) {
        try {
            String jsonStr = readJSON();
            JSONObject json = jsonStr.isEmpty() ? new JSONObject() : new JSONObject(jsonStr);
            JSONArray listas = json.has("listas") ? json.getJSONArray("listas") : new JSONArray();

            if (!listaExiste(listas, nomeLista)) {
                listas.put(nomeLista);
                json.put("listas", listas);
                saveJSON(json.toString());
            } else {
                MyToast("Lista já existe.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean listaExiste(JSONArray listas, String nome) throws JSONException {
        for (int i = 0; i < listas.length(); i++) {
            if (listas.getString(i).equalsIgnoreCase(nome)) return true;
        }
        return false;
    }

    private void carregarListas(LinearLayout layout) {
        layout.removeAllViews();
        final List<Button> botoes = new ArrayList<>();

        try {
            String jsonStr = readJSON();
            if (!jsonStr.isEmpty()) {
                JSONObject json = new JSONObject(jsonStr);
                JSONArray listas = json.optJSONArray("listas");
                if (listas != null) {
                    for (int i = 0; i < listas.length(); i++) {
                        String nome = listas.getString(i);

                        final Button btn = new Button(this);
                        btn.setText(nome);
                        btn.setTextSize(14);
                        btn.setPadding(20, 20, 20, 20);
                        btn.setTextColor(ABCText);
                        btn.setTypeface(Typeface.DEFAULT_BOLD);

                        botoes.add(btn);

                        if (i == 0 && Index_First == 0) {
                            Index_First++;
                            listaAtual = nome;
                            Title.setText("Sales Notes: " + nome);
                            AddColorABC(btn, ABCStroke, Color.TRANSPARENT, 10, 0);
                        } else {

                            if (nome.equals(listaAtual)) {
                                Title.setText("Sales Notes: " + nome);
                                AddColorABC(btn, ABCStroke, Color.TRANSPARENT, 10, 0);
                            } else {
                                AddColorABC(btn, Color.TRANSPARENT, ABCStroke, 10, 5);
                            }
                        }

                        final String listaSelecionada = nome;
                        btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    for (Button b : botoes) {
                                        AddColorABC(b, Color.TRANSPARENT, ABCStroke, 10, 5);
                                    }

                                    listaAtual = listaSelecionada;
                                    Title.setText("Sales Notes: " + listaSelecionada);
                                    AddColorABC(btn, ABCStroke, Color.TRANSPARENT, 10, 0);
                                    updateList(searchInput.getText().toString());
                                }
                            });

                        layout.addView(btn);
                        layout.addView(CreateLinearLayout(), dp(2), -2);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        filePath = getExternalFilesDir("SalesNotes") + "/data.json";

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 0, 40, 40);
        AddColorABC(mainLayout, ABCbackground, Color.TRANSPARENT, 0, 0);

        LinearLayout Title_LinearLayout = new LinearLayout(this);
        Title_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        Title_LinearLayout.setGravity(Gravity.CENTER_VERTICAL);
        mainLayout.addView(Title_LinearLayout, -1, dp(60));

        Title = new TextView(this);
        Title.setText("Sales Notes:");
        Title.setTextSize(20);
        Title.setPadding(0, 8, 0, 3);
        Title.setTypeface(Typeface.DEFAULT_BOLD);
        Title.setTextColor(ABCText);
        Title_LinearLayout.addView(Title);

        View spacer = new View(this);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        spacer.setLayoutParams(spacerParams);
        Title_LinearLayout.addView(spacer);

        TextView settings = new TextView(this);
        settings.setText("⚙");
        settings.setTextSize(20);
        settings.setPadding(0, 8, 0, 3);
        settings.setTypeface(Typeface.DEFAULT_BOLD);
        settings.setTextColor(ABCText);
        Title_LinearLayout.addView(settings);

        settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSettingsDialog();
                }
            });

        settings.startAnimation(criarRotacaoInfinita(5000));

        searchInput = new EditText(this);
        searchInput.setHint("Pesquisar Pessoa");
        searchInput.setTextSize(14);
        searchInput.setPadding(20, 20, 20, 20);
        searchInput.setTextColor(ABCText);
        searchInput.setTypeface(Typeface.DEFAULT_BOLD);
        AddColorABC(searchInput, ABCStroke, Color.TRANSPARENT, 100, 0);
        mainLayout.addView(searchInput);

        mainLayout.addView(CreateLinearLayout(), -2, dp(5));

        mainLayout.addView(CreateLinearLayout(), -2, dp(5));

        addButton = new Button(this);
        addButton.setText("Adicionar Pessoa");
        addButton.setTextSize(16);
        addButton.setPadding(20, 20, 20, 20);
        addButton.setTextColor(ABCText);
        addButton.setTypeface(Typeface.DEFAULT_BOLD);
        AddColorABC(addButton, ABCStroke, Color.TRANSPARENT, 10, 0);
        
        final Handler handler = new Handler();
        
        Runnable loop_2 = new Runnable() {
        @Override
        public void run() {
            if (listaAtual == null) {
                addButton.setVisibility(View.GONE);
            }else{
                addButton.setVisibility(View.VISIBLE);
                addButton.setText(listaAtual + ": Adicionar Pessoa");
            }
            handler.postDelayed(this, 500);
        }
        };

        handler.post(loop_2);

        mainLayout.addView(CreateLinearLayout(), -2, dp(5));

        LinearLayout Base_Screen_List = new LinearLayout(this);
        Base_Screen_List.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.addView(Base_Screen_List);
        mainLayout.addView(CreateLinearLayout(), -1, dp(5));
        mainLayout.addView(addButton);
        mainLayout.addView(CreateLinearLayout(), -1, dp(5));

        HorizontalScrollView scrollView_Screen_List = new HorizontalScrollView(this);
        LinearLayout.LayoutParams scrollViewLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        scrollView_Screen_List.setLayoutParams(scrollViewLayoutParams);
        Base_Screen_List.addView(scrollView_Screen_List);

        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        scrollView_Screen_List.addView(contentLayout);

        Base_Screen_List.addView(CreateLinearLayout(), dp(5), -2);

        Button Mdy_list_it = new Button(this);
        Mdy_list_it.setText("🖌️");
        Mdy_list_it.setTextSize(14);
        Mdy_list_it.setPadding(20, 20, 20, 20);
        Mdy_list_it.setTextColor(ABCText);
        Mdy_list_it.setTypeface(Typeface.DEFAULT_BOLD);
        Base_Screen_List.addView(Mdy_list_it);
        AddColorABC(Mdy_list_it, ABCText, Color.TRANSPARENT, 10, 0);
        Mdy_list_it.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listaAtual != null) {
                        showEditListDialog();
                    } else {
                        MyToast("Selecione uma lista primeiro");
                    }
                }
            });

        Base_Screen_List.addView(CreateLinearLayout(), dp(5), -2);

        Button Delet_list_it = new Button(this);
        Delet_list_it.setText("🚫");
        Delet_list_it.setTextSize(14);
        Delet_list_it.setPadding(20, 20, 20, 20);
        Delet_list_it.setTextColor(ABCText);
        Delet_list_it.setTypeface(Typeface.DEFAULT_BOLD);
        Base_Screen_List.addView(Delet_list_it);
        AddColorABC(Delet_list_it, ABCText, Color.TRANSPARENT, 10, 0);
        Delet_list_it.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listaAtual != null) {
                        showDeleteListDialog();
                    } else {
                        MyToast("Selecione uma lista primeiro");
                    }
                }
            });

        Base_Screen_List.addView(CreateLinearLayout(), dp(5), -2);

        Button Add_List = new Button(this);
        Add_List.setText("Add List");
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Add_List.setLayoutParams(buttonParams);
        Base_Screen_List.addView(Add_List);
        Add_List.setTextSize(14);
        Add_List.setPadding(20, 20, 20, 20);
        Add_List.setTextColor(ABCText);
        Add_List.setTypeface(Typeface.DEFAULT_BOLD);
        AddColorABC(Add_List, Color.TRANSPARENT, ABCStroke, 10, 5);

        Add_List.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddListDialog();
                }
            });

        Base_Screen_List.addView(CreateLinearLayout(), dp(5), -2);

        carregarListas(contentLayout);

        mainLayout.addView(CreateLinearLayout(), -2, dp(5));

        ScrollView scrollView = new ScrollView(this);
        listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(listLayout);

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1
        );

        scrollView.setLayoutParams(scrollParams);
        mainLayout.addView(scrollView);

        mainLayout.addView(CreateLinearLayout(), -2, dp(5));

        LinearLayout Linear_Storage = new LinearLayout(this);
        Linear_Storage.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.addView(Linear_Storage);

        LinearLayout.LayoutParams paramsPeso = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        Button btnExportar = new Button(this);
        btnExportar.setText("📤 Fazer Backup");
        btnExportar.setTextColor(ABCText);
        btnExportar.setTypeface(Typeface.DEFAULT_BOLD);
        AddColorABC(btnExportar, Color.TRANSPARENT, ABCStroke, 10, 5);
        btnExportar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exportarBackup();
                }
            });
        Linear_Storage.addView(btnExportar, paramsPeso);

        View espaco = CreateLinearLayout();
        espaco.setLayoutParams(new LinearLayout.LayoutParams(dp(5), LinearLayout.LayoutParams.MATCH_PARENT));
        Linear_Storage.addView(espaco);

        Button btnImportar = new Button(this);
        btnImportar.setText("📥 Restaurar Backup");
        btnImportar.setTextColor(ABCText);
        btnImportar.setTypeface(Typeface.DEFAULT_BOLD);
        AddColorABC(btnImportar, Color.TRANSPARENT, ABCStroke, 10, 5);
        btnImportar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    importarBackup();
                }
            });
        Linear_Storage.addView(btnImportar, paramsPeso);

        mainLayout.addView(CreateLinearLayout(), -2, dp(5));

        TextView Credit = new TextView(this);
        Credit.setText("Developed by: Guilherme Soares Marciel");
        Credit.setTextSize(13);
        Credit.setPadding(0, 3, 0, 3);
        Credit.setTypeface(Typeface.DEFAULT_BOLD);
        Credit.setTextColor(ABCText);
        Credit.setGravity(Gravity.CENTER);
        mainLayout.addView(Credit, -1, -2);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(mainLayout);

        addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddPersonVipDialog();
                }
            });

        searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    updateList(s.toString());
                }
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });

        updateList("");
    }

    private String readJSON() {
        StringBuilder json = new StringBuilder();
        try {
            File file = new File(filePath);
            if (!file.exists()) return "";
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private void saveJSON(String json) {
        try {
            File file = new File(filePath);
            File dir = file.getParentFile();
            if (!dir.exists()) dir.mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addPerson(String name, String listaSelecionada) {
        try {
            String jsonStr = readJSON();
            JSONObject json = jsonStr.isEmpty() ? new JSONObject() : new JSONObject(jsonStr);
            JSONArray people = json.has("people") ? json.getJSONArray("people") : new JSONArray();

            JSONObject newPerson = new JSONObject();
            newPerson.put("name", name);
            newPerson.put("lista", listaSelecionada);
            newPerson.put("compras", new JSONArray());
            newPerson.put("pagamentos", new JSONArray());
            people.put(newPerson);

            json.put("people", people);
            saveJSON(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void deletePerson(int index) {
        try {
            String jsonStr = readJSON();
            if (jsonStr.isEmpty()) return;

            JSONObject json = new JSONObject(jsonStr);
            JSONArray people = json.getJSONArray("people");

            if (index >= 0 && index < people.length()) {
                people.remove(index);
                json.put("people", people);
                saveJSON(json.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateList(String filter) {
        listLayout.removeAllViews();
        String jsonStr = readJSON();
        try {
            if (!jsonStr.isEmpty()) {
                JSONObject json = new JSONObject(jsonStr);
                JSONArray people = json.getJSONArray("people");

                for (int i = 0; i < people.length(); i++) {
                    JSONObject person = people.getJSONObject(i);
                    final String name = person.getString("name");

                    String listaPessoa = person.optString("lista", "");
                    if (listaAtual == null || !listaAtual.equals(listaPessoa)) continue;

                    if (name.toLowerCase().contains(filter.toLowerCase())) {
                        final int actualIndex = i;

                        LinearLayout itemLayout = new LinearLayout(this);
                        itemLayout.setOrientation(LinearLayout.VERTICAL);
                        itemLayout.setPadding(0, 20, 0, 20);
                        AddColorABC(itemLayout, ABCStroke, Color.TRANSPARENT, 10, 0);

                        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        itemParams.setMargins(0, dp(8), 0, dp(8));
                        itemLayout.setLayoutParams(itemParams);

                        LinearLayout topRow = new LinearLayout(this);
                        topRow.setOrientation(LinearLayout.HORIZONTAL);

                        TextView nameView = new TextView(this);
                        nameView.setText(name);
                        nameView.setTextSize(16);
                        nameView.setPadding(8, 0, 8, 0);
                        nameView.setTypeface(Typeface.DEFAULT_BOLD);
                        nameView.setTextColor(ABCText);
                        nameView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                        Button Edit_it = new Button(this);
                        Edit_it.setText("🖌️");
                        Edit_it.setTextSize(14);
                        Edit_it.setPadding(20, 20, 20, 20);
                        Edit_it.setTextColor(ABCText);
                        Edit_it.setTypeface(Typeface.DEFAULT_BOLD);
                        AddColorABC(Edit_it, ABCbackground, Color.TRANSPARENT, 10, 0);
                        Edit_it.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PessAtual = name;
                                    showEditPersDialog();
                                }
                            });

                        final Button displayButton = new Button(this);
                        displayButton.setText("🔻");
                        displayButton.setTextSize(14);
                        displayButton.setPadding(20, 20, 20, 20);
                        displayButton.setTypeface(Typeface.DEFAULT_BOLD);
                        displayButton.setTextColor(ABCText);
                        AddColorABC(displayButton, ABCbackground, Color.TRANSPARENT, 10, 0);

                        Button deleteButton = new Button(this);
                        deleteButton.setText("🚫");
                        deleteButton.setTextSize(14);
                        deleteButton.setPadding(20, 20, 20, 20);
                        deleteButton.setTypeface(Typeface.DEFAULT_BOLD);
                        deleteButton.setTextColor(ABCText);
                        AddColorABC(deleteButton, ABCbackground, Color.TRANSPARENT, 10, 0);

                        topRow.addView(nameView);
                        topRow.addView(displayButton);
                        topRow.addView(CreateLinearLayout(), dp(5), -2);
                        topRow.addView(deleteButton);
                        topRow.addView(CreateLinearLayout(), dp(5), -2);
                        topRow.addView(Edit_it);
                        topRow.addView(CreateLinearLayout(), dp(5), -2);

                        final LinearLayout subLayout = new LinearLayout(this);
                        subLayout.setOrientation(LinearLayout.VERTICAL);
                        subLayout.setVisibility(View.GONE);

                        LinearLayout actionRow = new LinearLayout(this);
                        actionRow.setOrientation(LinearLayout.HORIZONTAL);

                        Button boughtButton = new Button(this);
                        boughtButton.setText("Comprou");
                        boughtButton.setTextSize(14);
                        boughtButton.setTypeface(Typeface.DEFAULT_BOLD);
                        boughtButton.setTextColor(ABCText);
                        AddColorABC(boughtButton, Color.TRANSPARENT, ABCbackground, 10, 5);

                        Button paidButton = new Button(this);
                        paidButton.setText("Pagou");
                        paidButton.setTextSize(14);
                        paidButton.setTypeface(Typeface.DEFAULT_BOLD);
                        paidButton.setTextColor(ABCText);
                        AddColorABC(paidButton, Color.TRANSPARENT, ABCbackground, 10, 5);

                        actionRow.addView(CreateLinearLayout(), dp(5), -2);
                        actionRow.addView(boughtButton);
                        actionRow.addView(CreateLinearLayout(), dp(5), -2);
                        actionRow.addView(paidButton);
                        subLayout.addView(CreateLinearLayout(), -1, dp(5));
                        subLayout.addView(actionRow);

                        final LinearLayout historyLayout = new LinearLayout(this);
                        historyLayout.setOrientation(LinearLayout.VERTICAL);
                        subLayout.addView(historyLayout);

                        displayButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (subLayout.getVisibility() == View.GONE) {
                                        subLayout.setVisibility(View.VISIBLE);
                                        displayButton.setText("🔺");
                                        loadHistory(historyLayout, actualIndex);
                                    } else {
                                        subLayout.setVisibility(View.GONE);
                                        displayButton.setText("🔻");
                                    }
                                }
                            });

                        deleteButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                                    LinearLayout layout = new LinearLayout(MainActivity.this);
                                    layout.setOrientation(LinearLayout.VERTICAL);
                                    AddColorABC(layout, ABCbackground, Color.TRANSPARENT, 0, 0);

                                    TextView title = new TextView(MainActivity.this);
                                    title.setText("Confirmar Remoção");
                                    title.setTypeface(Typeface.DEFAULT_BOLD);
                                    title.setTextSize(20);
                                    title.setTextColor(ABCText);
                                    layout.addView(title);

                                    TextView subject = new TextView(MainActivity.this);
                                    subject.setText("Tem certeza que deseja remover \"" + name + "\"?");
                                    subject.setTextColor(ABCText);
                                    subject.setTypeface(Typeface.DEFAULT_BOLD);
                                    subject.setPadding(5, 5, 5, 5);
                                    layout.addView(subject);

                                    Button setPositiveButton = new Button(MainActivity.this);
                                    setPositiveButton.setText("Excluir");
                                    setPositiveButton.setTextColor(ABCText);
                                    setPositiveButton.setTypeface(Typeface.DEFAULT_BOLD);
                                    setPositiveButton.setPadding(5, 15, 5, 15);
                                    AddColorABC(setPositiveButton, Color.TRANSPARENT, ABCStroke, 10, 5);
                                    layout.addView(setPositiveButton);

                                    layout.addView(CreateLinearLayout(), -1, dp(5));

                                    Button setNegativeButton = new Button(MainActivity.this);
                                    setNegativeButton.setText("Cancelar");
                                    setNegativeButton.setTextColor(ABCText);
                                    setNegativeButton.setTypeface(Typeface.DEFAULT_BOLD);
                                    setNegativeButton.setPadding(5, 15, 5, 15);
                                    setNegativeButton.setLayoutParams(new LinearLayout.LayoutParams(
                                                                          LinearLayout.LayoutParams.MATCH_PARENT,
                                                                          LinearLayout.LayoutParams.WRAP_CONTENT
                                                                      ));
                                    AddColorABC(setNegativeButton, Color.TRANSPARENT, ABCStroke, 10, 5);
                                    layout.addView(setNegativeButton);

                                    builder.setView(layout);
                                    final AlertDialog dialog = builder.create();

                                    setPositiveButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                deletePerson(actualIndex);
                                                updateList(searchInput.getText().toString());
                                                dialog.dismiss();
                                            }
                                        });

                                    setNegativeButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                            }
                                        });

                                    dialog.show();
                                }
                            });

                        boughtButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showCompraDialog(actualIndex);
                                }
                            });

                        paidButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showPagamentoDialog(actualIndex);
                                }
                            });

                        itemLayout.addView(topRow);
                        itemLayout.addView(subLayout);
                        listLayout.addView(itemLayout);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showEditPersDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                                   LinearLayout.LayoutParams.MATCH_PARENT,
                                   LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(layout, ABCbackground, Color.TRANSPARENT, 0, 0);

        final TextView Title = new TextView(this);
        Title.setText("Renomear Pessoa");
        Title.setTypeface(Typeface.DEFAULT_BOLD);
        Title.setTextSize(20);
        Title.setTextColor(ABCText);
        layout.addView(Title);

        TextView subject = new TextView(this);
        subject.setText("Tem certeza de que deseja renomear a pessoa: \n" + PessAtual);
        subject.setTypeface(Typeface.DEFAULT_BOLD);
        subject.setTextColor(ABCText);
        subject.setPadding(5, 5, 5, 5);
        layout.addView(subject);

        final EditText itemInput = new EditText(this);
        itemInput.setHint(PessAtual);
        itemInput.setText(PessAtual);
        itemInput.setTextSize(14);
        itemInput.setPadding(20, 20, 20, 20);
        itemInput.setTextColor(ABCText);
        itemInput.setTypeface(Typeface.DEFAULT_BOLD);
        itemInput.setLayoutParams(new LinearLayout.LayoutParams(
                                      LinearLayout.LayoutParams.MATCH_PARENT,
                                      LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(itemInput, ABCStroke, Color.TRANSPARENT, 100, 0);
        layout.addView(itemInput);


        layout.addView(CreateLinearLayout(), -1, dp(5));

        Button setPositiveButton = new Button(this);
        setPositiveButton.setText("Salvar");
        setPositiveButton.setTextColor(ABCText);
        setPositiveButton.setTypeface(Typeface.DEFAULT_BOLD);
        setPositiveButton.setPadding(5, 15, 5, 15);
        setPositiveButton.setLayoutParams(new LinearLayout.LayoutParams(
                                              LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(setPositiveButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setPositiveButton);

        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(layout)
            .create();

        setPositiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String novoNome = itemInput.getText().toString().trim();
                    if (!novoNome.isEmpty()) {
                        try {
                            String jsonStr = readJSON();
                            JSONObject json = jsonStr.isEmpty() ? new JSONObject() : new JSONObject(jsonStr);
                            JSONArray people = json.getJSONArray("people");

                            for (int i = 0; i < people.length(); i++) {
                                JSONObject pessoa = people.getJSONObject(i);
                                String nomeAtual = pessoa.optString("name", "");
                                String lista = pessoa.optString("lista", "");
                                if (nomeAtual.equals(PessAtual) && lista.equals(listaAtual)) {
                                    pessoa.put("name", novoNome);
                                    people.put(i, pessoa);
                                    break;
                                }
                            }

                            json.put("people", people);
                            saveJSON(json.toString());
                            MyToast("Pessoa renomeada com sucesso!");
                            updateList(searchInput.getText().toString());
                            dialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            MyToast("Erro ao renomear a pessoa.");
                            dialog.dismiss();
                        }
                    } else {
                        MyToast("Digite um nome válido.");
                        dialog.dismiss();
                    }
                }
            });

        layout.addView(CreateLinearLayout(), -1, dp(5));

        Button setNegativeButton = new Button(this);
        setNegativeButton.setText("Cancelar");
        setNegativeButton.setTextColor(ABCText);
        setNegativeButton.setTypeface(Typeface.DEFAULT_BOLD);
        setNegativeButton.setPadding(5, 15, 5, 15);
        setNegativeButton.setLayoutParams(new LinearLayout.LayoutParams(
                                              LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(setNegativeButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setNegativeButton);

        setNegativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        dialog.show();

        dialog.getWindow().setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void showEditListDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                                   LinearLayout.LayoutParams.MATCH_PARENT,
                                   LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(layout, ABCbackground, Color.TRANSPARENT, 0, 0);

        final TextView Title = new TextView(this);
        Title.setText("Renomear Lista");
        Title.setTypeface(Typeface.DEFAULT_BOLD);
        Title.setTextSize(20);
        Title.setTextColor(ABCText);
        layout.addView(Title);

        TextView subject = new TextView(this);
        subject.setText("Tem certeza de que deseja renomear a lista: \n" + listaAtual);
        subject.setTypeface(Typeface.DEFAULT_BOLD);
        subject.setTextColor(ABCText);
        subject.setPadding(5, 5, 5, 5);
        layout.addView(subject);

        final EditText itemInput = new EditText(this);
        itemInput.setHint("Nome da Lista");
        itemInput.setText(listaAtual);
        itemInput.setTextSize(14);
        itemInput.setPadding(20, 20, 20, 20);
        itemInput.setTextColor(ABCText);
        itemInput.setTypeface(Typeface.DEFAULT_BOLD);
        itemInput.setLayoutParams(new LinearLayout.LayoutParams(
                                      LinearLayout.LayoutParams.MATCH_PARENT,
                                      LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(itemInput, ABCStroke, Color.TRANSPARENT, 100, 0);
        layout.addView(itemInput);

        layout.addView(CreateLinearLayout(), -1, dp(5));

        Button setPositiveButton = new Button(this);
        setPositiveButton.setText("Salvar");
        setPositiveButton.setTextColor(ABCText);
        setPositiveButton.setTypeface(Typeface.DEFAULT_BOLD);
        setPositiveButton.setPadding(5, 15, 5, 15);
        setPositiveButton.setLayoutParams(new LinearLayout.LayoutParams(
                                              LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(setPositiveButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setPositiveButton);

        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(layout)
            .create();

        setPositiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String novoNome = itemInput.getText().toString().trim();
                    if (!novoNome.isEmpty()) {
                        try {
                            String jsonStr = readJSON();
                            JSONObject json = jsonStr.isEmpty() ? new JSONObject() : new JSONObject(jsonStr);

                            JSONArray listas = json.optJSONArray("listas");
                            if (listas != null) {
                                for (int i = 0; i < listas.length(); i++) {
                                    if (listas.getString(i).equalsIgnoreCase(listaAtual)) {
                                        listas.put(i, novoNome);
                                        break;
                                    }
                                }
                                json.put("listas", listas);
                            }

                            JSONArray pessoas = json.optJSONArray("people");
                            if (pessoas != null) {
                                for (int i = 0; i < pessoas.length(); i++) {
                                    JSONObject pessoa = pessoas.getJSONObject(i);
                                    if (listaAtual.equalsIgnoreCase(pessoa.optString("lista"))) {
                                        pessoa.put("lista", novoNome);
                                    }
                                }
                                json.put("people", pessoas);
                            }

                            saveJSON(json.toString());

                            listaAtual = novoNome;
                            Title.setText("Sales Notes: " + listaAtual);
                            carregarListas(contentLayout);
                            updateList(searchInput.getText().toString());
                            MyToast("Lista renomeada com sucesso!");
                            dialog.dismiss();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            MyToast("Erro ao renomear a lista.");
                            dialog.dismiss();
                        }
                    } else {
                        MyToast("Digite um nome válido.");
                        dialog.dismiss();
                    }
                }
            });

        layout.addView(CreateLinearLayout(), -1, dp(5));

        Button setNegativeButton = new Button(this);
        setNegativeButton.setText("Cancelar");
        setNegativeButton.setTextColor(ABCText);
        setNegativeButton.setTypeface(Typeface.DEFAULT_BOLD);
        setNegativeButton.setPadding(5, 15, 5, 15);
        setNegativeButton.setLayoutParams(new LinearLayout.LayoutParams(
                                              LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(setNegativeButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setNegativeButton);

        setNegativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        dialog.show();

        dialog.getWindow().setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void showDeleteListDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        AddColorABC(layout, ABCbackground, Color.TRANSPARENT, 0, 0);

        final TextView Title = new TextView(this);
        Title.setText("Excluir lista");
        Title.setTypeface(Typeface.DEFAULT_BOLD);
        Title.setTextSize(20);
        Title.setTextColor(ABCText);
        layout.addView(Title);

        TextView subject = new TextView(this);
        subject.setText("Tem certeza de que deseja excluir a lista: \n" + listaAtual);
        subject.setTextColor(ABCText);
        subject.setTypeface(Typeface.DEFAULT_BOLD);
        subject.setPadding(5, 5, 5, 5);
        layout.addView(subject);

        Button setPositiveButton = new Button(this);
        setPositiveButton.setText("Excluir");
        setPositiveButton.setTextColor(ABCText);
        setPositiveButton.setTypeface(Typeface.DEFAULT_BOLD);
        setPositiveButton.setPadding(5, 15, 5, 15);
        AddColorABC(setPositiveButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setPositiveButton);

        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(layout)
            .create();

        setPositiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String jsonStr = readJSON();
                        JSONObject json = jsonStr.isEmpty() ? new JSONObject() : new JSONObject(jsonStr);

                        JSONArray listas = json.optJSONArray("listas");
                        if (listas != null) {
                            for (int i = 0; i < listas.length(); i++) {
                                if (listas.getString(i).equalsIgnoreCase(listaAtual)) {
                                    listas.remove(i);
                                    break;
                                }
                            }
                            json.put("listas", listas);
                        }

                        JSONArray pessoas = json.optJSONArray("people");
                        if (pessoas != null) {
                            JSONArray novaListaPessoas = new JSONArray();
                            for (int i = 0; i < pessoas.length(); i++) {
                                JSONObject pessoa = pessoas.getJSONObject(i);
                                if (!listaAtual.equalsIgnoreCase(pessoa.optString("lista"))) {
                                    novaListaPessoas.put(pessoa);
                                }
                            }
                            json.put("people", novaListaPessoas);
                        }

                        saveJSON(json.toString());

                        Index_First = 0;
                        MyToast("Lista \"" + listaAtual + "\" excluída com sucesso!");
                        listaAtual = null;
                        Title.setText("Sales Notes:");
                        carregarListas(contentLayout);
                        updateList(searchInput.getText().toString());
                        dialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        MyToast("Erro ao excluir a lista.");
                        dialog.dismiss();
                    }
                }
            });

        layout.addView(CreateLinearLayout(), -1, dp(5));

        Button setNegativeButton = new Button(this);
        setNegativeButton.setText("Cancelar");
        setNegativeButton.setTextColor(ABCText);
        setNegativeButton.setTypeface(Typeface.DEFAULT_BOLD);
        setNegativeButton.setPadding(5, 15, 5, 15);
        setNegativeButton.setLayoutParams(new LinearLayout.LayoutParams(
                                              LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(setNegativeButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setNegativeButton);

        setNegativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        dialog.show();
    }

    private void showAddListDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                                   LinearLayout.LayoutParams.MATCH_PARENT,
                                   LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(layout, ABCbackground, Color.TRANSPARENT, 0, 0);

        TextView Title = new TextView(this);
        Title.setText("Nova Lista");
        Title.setTypeface(Typeface.DEFAULT_BOLD);
        Title.setTextSize(20);
        Title.setTextColor(ABCText);
        layout.addView(Title);

        final EditText name_list = new EditText(this);
        name_list.setHint("Nome da Lista");
        name_list.setTextSize(14);
        name_list.setPadding(20, 20, 20, 20);
        name_list.setTextColor(ABCText);
        name_list.setTypeface(Typeface.DEFAULT_BOLD);
        name_list.setLayoutParams(new LinearLayout.LayoutParams(
                                      LinearLayout.LayoutParams.MATCH_PARENT,
                                      LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(name_list, ABCStroke, Color.TRANSPARENT, 100, 0);
        layout.addView(name_list);

        layout.addView(CreateLinearLayout(), -1, dp(5));

        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(layout)
            .create();

        Button setPositiveButton = new Button(this);
        setPositiveButton.setText("Salvar");
        setPositiveButton.setTextColor(ABCText);
        setPositiveButton.setTypeface(Typeface.DEFAULT_BOLD);
        setPositiveButton.setPadding(5, 15, 5, 15);
        setPositiveButton.setLayoutParams(new LinearLayout.LayoutParams(
                                              LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(setPositiveButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setPositiveButton);

        setPositiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nomeLista = name_list.getText().toString().trim();
                    if (!nomeLista.isEmpty()) {
                        addNovaLista(nomeLista);
                        listaAtual = nomeLista;
                        carregarListas(contentLayout);
                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                        MyToast("Digite um nome para a lista.");
                    }
                }
            });

        layout.addView(CreateLinearLayout(), -1, dp(5));

        Button setNegativeButton = new Button(this);
        setNegativeButton.setText("Cancelar");
        setNegativeButton.setTextColor(ABCText);
        setNegativeButton.setTypeface(Typeface.DEFAULT_BOLD);
        setNegativeButton.setPadding(5, 15, 5, 15);
        setNegativeButton.setLayoutParams(new LinearLayout.LayoutParams(
                                              LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(setNegativeButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setNegativeButton);

        setNegativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        dialog.show();

        dialog.getWindow().setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }
    
    private void showAddPersonVipDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                                   LinearLayout.LayoutParams.MATCH_PARENT,
                                   LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(layout, ABCbackground, Color.TRANSPARENT, 0, 0);

        TextView Title = new TextView(this);
        Title.setText("Nova Pessoa");
        Title.setTypeface(Typeface.DEFAULT_BOLD);
        Title.setTextSize(20);
        Title.setTextColor(ABCText);
        layout.addView(Title);

        final EditText name_list = new EditText(this);
        name_list.setHint("Nome da Pessoa");
        name_list.setTextSize(14);
        name_list.setPadding(20, 20, 20, 20);
        name_list.setTextColor(ABCText);
        name_list.setTypeface(Typeface.DEFAULT_BOLD);
        name_list.setLayoutParams(new LinearLayout.LayoutParams(
                                      LinearLayout.LayoutParams.MATCH_PARENT,
                                      LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(name_list, ABCStroke, Color.TRANSPARENT, 100, 0);
        layout.addView(name_list);

        layout.addView(CreateLinearLayout(), -1, dp(5));

        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(layout)
            .create();

        Button setPositiveButton = new Button(this);
        setPositiveButton.setText("Salvar");
        setPositiveButton.setTextColor(ABCText);
        setPositiveButton.setTypeface(Typeface.DEFAULT_BOLD);
        setPositiveButton.setPadding(5, 15, 5, 15);
        setPositiveButton.setLayoutParams(new LinearLayout.LayoutParams(
                                              LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(setPositiveButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setPositiveButton);

        setPositiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nomeLista = name_list.getText().toString().trim();
                    if (!nomeLista.isEmpty()) {
                        if (listaAtual != null) {
                            addPerson(nomeLista, listaAtual);
                            MyToast("Adicionado com sucesso");
                            name_list.setText("");
                            updateList(searchInput.getText().toString());
                            dialog.dismiss();
                        }else{
                            MyToast("Selecione uma lista primeiro");
                            dialog.dismiss();
                        }
                    } else {
                        dialog.dismiss();
                        MyToast("Digite o nome da pessoa");
                    }
                }
            });

        layout.addView(CreateLinearLayout(), -1, dp(5));

        Button setNegativeButton = new Button(this);
        setNegativeButton.setText("Cancelar");
        setNegativeButton.setTextColor(ABCText);
        setNegativeButton.setTypeface(Typeface.DEFAULT_BOLD);
        setNegativeButton.setPadding(5, 15, 5, 15);
        setNegativeButton.setLayoutParams(new LinearLayout.LayoutParams(
                                              LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(setNegativeButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setNegativeButton);

        setNegativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        dialog.show();

        dialog.getWindow().setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void showSettingsDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        AddColorABC(layout, ABCbackground, Color.TRANSPARENT, 0, 0);

        TextView Title = new TextView(this);
        Title.setText("Settings");
        Title.setTypeface(Typeface.DEFAULT_BOLD);
        Title.setTextSize(20);
        Title.setTextColor(ABCText);
        layout.addView(Title);

        ScrollView scrollView = new ScrollView(this);
        layout.addView(scrollView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(150)));

        TextView subject = new TextView(this);
        subject.setText("Olá, meu nome é Guilherme Soares Marciel e sou o desenvolvedor do aplicativo Sales Notes.\n\n" +
                        "Criei este aplicativo com o objetivo de ajudar pequenos empreendedores a gerenciar seu negócio de forma simples, eficiente e inteligente. Com o Sales Notes, você pode registrar todas as compras e vendas realizadas pelos seus clientes diretamente no seu celular, mantendo o controle total das movimentações, mesmo sem conexão com a internet.\n\n" +
                        "O foco do aplicativo é proporcionar uma experiência prática e intuitiva, tornando a organização financeira mais acessível a todos. Tudo isso com a vantagem de funcionar 100% offline.\n\n" +
                        "Importante: os dados são armazenados localmente no seu dispositivo. Por isso, caso o aplicativo seja desinstalado ou instalado em outro celular, o histórico de vendas e pagamentos não será recuperado automaticamente. Estamos trabalhando para incluir em breve funcionalidades como compartilhamento e backup do histórico, permitindo que você transfira seus dados para outros dispositivos com facilidade.\n\n" +
                        "Agradeço pela sua confiança.\n" +
                        "Sales Notes — Desenvolvido por Guilherme Soares Marciel");
        subject.setTextColor(ABCText);
        subject.setTypeface(Typeface.DEFAULT_BOLD);
        subject.setPadding(5, 5, 5, 5);
        scrollView.addView(subject);

        Button setPositiveButton = new Button(this);
        setPositiveButton.setText("Continuar");
        setPositiveButton.setTextColor(ABCText);
        setPositiveButton.setTypeface(Typeface.DEFAULT_BOLD);
        setPositiveButton.setPadding(5, 15, 5, 15);
        AddColorABC(setPositiveButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setPositiveButton);

        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(layout)
            .create();

        setPositiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        dialog.show();
    }

    private void importarBackup() {
        try {
            File backupFile = new File("/storage/emulated/0/Download/SalesNotes_Backup.json");
            if (!backupFile.exists()) {
                MyToast("Arquivo de backup não encontrado.");
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(backupFile));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            saveJSON(json.toString());
            carregarListas(contentLayout);
            updateList(searchInput.getText().toString());
            MyToast("Backup restaurado com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
            MyToast("Erro ao importar o backup.");
        }
    }

    private void exportarBackup() {
        try {
            String json = readJSON();
            if (!json.isEmpty()) {
                File backupFile = new File("/storage/emulated/0/Download/SalesNotes_Backup.json");
                FileWriter writer = new FileWriter(backupFile);
                writer.write(json);
                writer.close();
                MyToast("Backup exportado com sucesso!");
            } else {
                MyToast("Nada para exportar.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            MyToast("Erro ao exportar o backup.");
        }
    }

    private void showCompraDialog(final int index) {
    LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                                   LinearLayout.LayoutParams.MATCH_PARENT,
                                   LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(layout, ABCbackground, Color.TRANSPARENT, 0, 0);

        TextView Title = new TextView(this);
        Title.setText("Nova Compra");
        Title.setTypeface(Typeface.DEFAULT_BOLD);
        Title.setTextSize(20);
        Title.setTextColor(ABCText);
        layout.addView(Title);
    
    final EditText itemInput = new EditText(this);
        itemInput.setHint("Comprou?");
        itemInput.setTextSize(14);
        itemInput.setPadding(20, 20, 20, 20);
        itemInput.setTextColor(ABCText);
        itemInput.setTypeface(Typeface.DEFAULT_BOLD);
        itemInput.setLayoutParams(new LinearLayout.LayoutParams(
                                      LinearLayout.LayoutParams.MATCH_PARENT,
                                      LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(itemInput, ABCStroke, Color.TRANSPARENT, 100, 0);
        layout.addView(itemInput);
        
        layout.addView(CreateLinearLayout(), -1, dp(5));
    
    final EditText valorInput = new EditText(this);
        valorInput.setHint("Valor?");
        valorInput.setTextSize(14);
        valorInput.setPadding(20, 20, 20, 20);
        valorInput.setTextColor(ABCText);
        valorInput.setTypeface(Typeface.DEFAULT_BOLD);
        valorInput.setLayoutParams(new LinearLayout.LayoutParams(
                                      LinearLayout.LayoutParams.MATCH_PARENT,
                                      LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(valorInput, ABCStroke, Color.TRANSPARENT, 100, 0);
        layout.addView(valorInput);
    
    layout.addView(CreateLinearLayout(), -1, dp(5));
    
    final AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(layout)
            .create();
    
    Button setPositiveButton = new Button(this);
        setPositiveButton.setText("Salvar");
        setPositiveButton.setTextColor(ABCText);
        setPositiveButton.setTypeface(Typeface.DEFAULT_BOLD);
        setPositiveButton.setPadding(5, 15, 5, 15);
        setPositiveButton.setLayoutParams(new LinearLayout.LayoutParams(
                                              LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(setPositiveButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setPositiveButton);
    
    setPositiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String item = itemInput.getText().toString().trim();
                    String valor = valorInput.getText().toString().trim();
                    if (!item.isEmpty() && !valor.isEmpty()) {
                        addCompra(index, item, valor);
                        updateList(searchInput.getText().toString());
                        dialog.dismiss();
                    }
                }
            });

        layout.addView(CreateLinearLayout(), -1, dp(5));
    
    Button setNegativeButton = new Button(this);
        setNegativeButton.setText("Cancelar");
        setNegativeButton.setTextColor(ABCText);
        setNegativeButton.setTypeface(Typeface.DEFAULT_BOLD);
        setNegativeButton.setPadding(5, 15, 5, 15);
        setNegativeButton.setLayoutParams(new LinearLayout.LayoutParams(
                                              LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(setNegativeButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setNegativeButton);

        setNegativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        dialog.show();

        dialog.getWindow().setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void showPagamentoDialog(final int index) {
    LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                                   LinearLayout.LayoutParams.MATCH_PARENT,
                                   LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(layout, ABCbackground, Color.TRANSPARENT, 0, 0);

        TextView Title = new TextView(this);
        Title.setText("Novo Pagamento");
        Title.setTypeface(Typeface.DEFAULT_BOLD);
        Title.setTextSize(20);
        Title.setTextColor(ABCText);
        layout.addView(Title);
        
        final EditText valorInput = new EditText(this);
        valorInput.setHint("Valor pago");
        valorInput.setTextSize(14);
        valorInput.setPadding(20, 20, 20, 20);
        valorInput.setTextColor(ABCText);
        valorInput.setTypeface(Typeface.DEFAULT_BOLD);
        valorInput.setLayoutParams(new LinearLayout.LayoutParams(
                                      LinearLayout.LayoutParams.MATCH_PARENT,
                                      LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(valorInput, ABCStroke, Color.TRANSPARENT, 100, 0);
        layout.addView(valorInput);
    
    layout.addView(CreateLinearLayout(), -1, dp(5));
    
    final AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(layout)
            .create();
    
    Button setPositiveButton = new Button(this);
        setPositiveButton.setText("Salvar");
        setPositiveButton.setTextColor(ABCText);
        setPositiveButton.setTypeface(Typeface.DEFAULT_BOLD);
        setPositiveButton.setPadding(5, 15, 5, 15);
        setPositiveButton.setLayoutParams(new LinearLayout.LayoutParams(
                                              LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(setPositiveButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setPositiveButton);
    
    setPositiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String valor = valorInput.getText().toString().trim();
                    if (!valor.isEmpty()) {
                        addPagamento(index, valor);
                        updateList(searchInput.getText().toString());
                        dialog.dismiss();
                    }else{
                        MyToast("Digite algo");
                    }
                }
            });

        layout.addView(CreateLinearLayout(), -1, dp(5));
        
        
        Button setNegativeButton = new Button(this);
        setNegativeButton.setText("Cancelar");
        setNegativeButton.setTextColor(ABCText);
        setNegativeButton.setTypeface(Typeface.DEFAULT_BOLD);
        setNegativeButton.setPadding(5, 15, 5, 15);
        setNegativeButton.setLayoutParams(new LinearLayout.LayoutParams(
                                              LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT));
        AddColorABC(setNegativeButton, Color.TRANSPARENT, ABCStroke, 10, 5);
        layout.addView(setNegativeButton);

        setNegativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        dialog.show();

        dialog.getWindow().setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void addCompra(int index, String item, String valor) {
        try {
            String jsonStr = readJSON();
            JSONObject json = new JSONObject(jsonStr);
            JSONArray people = json.getJSONArray("people");
            JSONObject person = people.getJSONObject(index);
            JSONArray compras = person.has("compras") ? person.getJSONArray("compras") : new JSONArray();

            JSONObject novaCompra = new JSONObject();
            novaCompra.put("item", item);
            novaCompra.put("valor", valor);
            novaCompra.put("data", getToday());
            compras.put(novaCompra);

            person.put("compras", compras);
            people.put(index, person);
            json.put("people", people);
            saveJSON(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addPagamento(int index, String valor) {
        try {
            String jsonStr = readJSON();
            JSONObject json = new JSONObject(jsonStr);
            JSONArray people = json.getJSONArray("people");
            JSONObject person = people.getJSONObject(index);
            JSONArray pagamentos = person.has("pagamentos") ? person.getJSONArray("pagamentos") : new JSONArray();

            JSONObject novoPagamento = new JSONObject();
            novoPagamento.put("valor", valor);
            novoPagamento.put("data", getToday());
            pagamentos.put(novoPagamento);

            person.put("pagamentos", pagamentos);
            people.put(index, person);
            json.put("people", people);
            saveJSON(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadHistory(LinearLayout layout, final int index) {
        layout.removeAllViews();
        try {
            String jsonStr = readJSON();
            JSONObject json = new JSONObject(jsonStr);
            JSONArray people = json.getJSONArray("people");
            JSONObject person = people.getJSONObject(index);

            TextView comprasTitle = new TextView(this);
            comprasTitle.setText("Compras:");
            comprasTitle.setTextColor(ABCText);
            comprasTitle.setTypeface(Typeface.DEFAULT_BOLD);
            comprasTitle.setPadding(8, 0, 8, 0);
            layout.addView(comprasTitle);

            JSONArray compras = person.getJSONArray("compras");
            double totalCompras = 0.0;
            for (int i = 0; i < compras.length(); i++) {
                JSONObject c = compras.getJSONObject(i);

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);

                TextView entry = new TextView(this);
                entry.setText("- " + c.getString("item") + " R$" + c.getString("valor") + " em " + c.getString("data"));
                entry.setTextColor(ABCText);
                entry.setTypeface(Typeface.DEFAULT_BOLD);
                entry.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                entry.setPadding(8, 0, 8, 0);
                row.addView(entry);

                final int compraIndex = i;
                Button removeCompraBtn = new Button(this);
                removeCompraBtn.setText("Remover");
                removeCompraBtn.setTextColor(ABCText);
                removeCompraBtn.setTypeface(Typeface.DEFAULT_BOLD);
                AddColorABC(removeCompraBtn, Color.TRANSPARENT, ABCbackground, 10, 5);
                removeCompraBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                LinearLayout layout = new LinearLayout(MainActivity.this);
                                    layout.setOrientation(LinearLayout.VERTICAL);
                                    AddColorABC(layout, ABCbackground, Color.TRANSPARENT, 0, 0);

                                    TextView title = new TextView(MainActivity.this);
                                    title.setText("Deseja remover esta compra?");
                                    title.setTypeface(Typeface.DEFAULT_BOLD);
                                    title.setTextSize(20);
                                    title.setTextColor(ABCText);
                                    layout.addView(title);
                                    
                                    Button setPositiveButton = new Button(MainActivity.this);
                                    setPositiveButton.setText("Sim");
                                    setPositiveButton.setTextColor(ABCText);
                                    setPositiveButton.setTypeface(Typeface.DEFAULT_BOLD);
                                    setPositiveButton.setPadding(5, 15, 5, 15);
                                    AddColorABC(setPositiveButton, Color.TRANSPARENT, ABCStroke, 10, 5);
                                    layout.addView(setPositiveButton);
                                    
                                    builder.setView(layout);
                                    final AlertDialog dialog = builder.create();
                                    
                                    setPositiveButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                removeCompra(index, compraIndex);
                                                updateList(searchInput.getText().toString());
                                                dialog.dismiss();
                                            }
                                        });
                                    
                                    layout.addView(CreateLinearLayout(), -1, dp(5));
                                    
                                    Button setNegativeButton = new Button(MainActivity.this);
                                    setNegativeButton.setText("Cancelar");
                                    setNegativeButton.setTextColor(ABCText);
                                    setNegativeButton.setTypeface(Typeface.DEFAULT_BOLD);
                                    setNegativeButton.setPadding(5, 15, 5, 15);
                                    setNegativeButton.setLayoutParams(new LinearLayout.LayoutParams(
                                                                          LinearLayout.LayoutParams.MATCH_PARENT,
                                                                          LinearLayout.LayoutParams.WRAP_CONTENT
                                                                      ));
                                    AddColorABC(setNegativeButton, Color.TRANSPARENT, ABCStroke, 10, 5);
                                    layout.addView(setNegativeButton);
                                    
                                    setNegativeButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                            }
                                        });
                                dialog.show();
                        }
                    });
                row.addView(removeCompraBtn);

                row.addView(CreateLinearLayout(), dp(5), -2);

                layout.addView(CreateLinearLayout(), -2, dp(5));
                layout.addView(row);

                totalCompras += Double.parseDouble(c.getString("valor").replace(",", "."));
            }

            TextView pagamentosTitle = new TextView(this);
            pagamentosTitle.setText("Pagamentos:");
            layout.addView(pagamentosTitle);
            pagamentosTitle.setTextColor(ABCText);
            pagamentosTitle.setTypeface(Typeface.DEFAULT_BOLD);
            pagamentosTitle.setPadding(8, 0, 8, 0);

            JSONArray pagamentos = person.getJSONArray("pagamentos");
            double totalPagamentos = 0.0;
            for (int i = 0; i < pagamentos.length(); i++) {
                JSONObject p = pagamentos.getJSONObject(i);

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);

                TextView entry = new TextView(this);
                entry.setText("- R$" + p.getString("valor") + " em " + p.getString("data"));
                entry.setTextColor(ABCText);
                entry.setTypeface(Typeface.DEFAULT_BOLD);
                entry.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                entry.setPadding(8, 0, 8, 0);
                row.addView(entry);

                final int pagamentoIndex = i;
                Button removePagamentoBtn = new Button(this);
                removePagamentoBtn.setText("Remover");
                removePagamentoBtn.setTextColor(ABCText);
                removePagamentoBtn.setTypeface(Typeface.DEFAULT_BOLD);
                AddColorABC(removePagamentoBtn, Color.TRANSPARENT, ABCbackground, 10, 5);
                removePagamentoBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                LinearLayout layout = new LinearLayout(MainActivity.this);
                                    layout.setOrientation(LinearLayout.VERTICAL);
                                    AddColorABC(layout, ABCbackground, Color.TRANSPARENT, 0, 0);

                                    TextView title = new TextView(MainActivity.this);
                                    title.setText("Deseja remover este pagamento?");
                                    title.setTypeface(Typeface.DEFAULT_BOLD);
                                    title.setTextSize(20);
                                    title.setTextColor(ABCText);
                                    layout.addView(title);
                                    
                                    Button setPositiveButton = new Button(MainActivity.this);
                                    setPositiveButton.setText("Sim");
                                    setPositiveButton.setTextColor(ABCText);
                                    setPositiveButton.setTypeface(Typeface.DEFAULT_BOLD);
                                    setPositiveButton.setPadding(5, 15, 5, 15);
                                    AddColorABC(setPositiveButton, Color.TRANSPARENT, ABCStroke, 10, 5);
                                    layout.addView(setPositiveButton);
                                    
                                    builder.setView(layout);
                                    final AlertDialog dialog = builder.create();
                                    
                                    setPositiveButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                removePagamento(index, pagamentoIndex);
                                                updateList(searchInput.getText().toString());
                                                dialog.dismiss();
                                            }
                                        });
                                    
                                    layout.addView(CreateLinearLayout(), -1, dp(5));
                                    
                                    Button setNegativeButton = new Button(MainActivity.this);
                                    setNegativeButton.setText("Cancelar");
                                    setNegativeButton.setTextColor(ABCText);
                                    setNegativeButton.setTypeface(Typeface.DEFAULT_BOLD);
                                    setNegativeButton.setPadding(5, 15, 5, 15);
                                    setNegativeButton.setLayoutParams(new LinearLayout.LayoutParams(
                                                                          LinearLayout.LayoutParams.MATCH_PARENT,
                                                                          LinearLayout.LayoutParams.WRAP_CONTENT
                                                                      ));
                                    AddColorABC(setNegativeButton, Color.TRANSPARENT, ABCStroke, 10, 5);
                                    layout.addView(setNegativeButton);
                                    
                                    setNegativeButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                            }
                                        });
                                dialog.show();
                        }
                    });
                row.addView(removePagamentoBtn);

                row.addView(CreateLinearLayout(), dp(5), -2);

                layout.addView(CreateLinearLayout(), -2, dp(5));
                layout.addView(row);

                totalPagamentos += Double.parseDouble(p.getString("valor").replace(",", "."));
            }

            double saldo = totalCompras - totalPagamentos;
            TextView saldoView = new TextView(this);
            saldoView.setText(String.format("💰 Deve: R$%.2f", saldo));
            saldoView.setPadding(8, 20, 0, 0);
            saldoView.setTextColor(ABCText);
            saldoView.setTypeface(Typeface.DEFAULT_BOLD);
            layout.addView(saldoView);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void removeCompra(int personIndex, int compraIndex) {
        try {
            String jsonStr = readJSON();
            JSONObject json = new JSONObject(jsonStr);
            JSONArray people = json.getJSONArray("people");
            JSONObject person = people.getJSONObject(personIndex);
            JSONArray compras = person.getJSONArray("compras");

            if (compraIndex >= 0 && compraIndex < compras.length()) {
                compras.remove(compraIndex);
                person.put("compras", compras);
                people.put(personIndex, person);
                json.put("people", people);
                saveJSON(json.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void removePagamento(int personIndex, int pagamentoIndex) {
        try {
            String jsonStr = readJSON();
            JSONObject json = new JSONObject(jsonStr);
            JSONArray people = json.getJSONArray("people");
            JSONObject person = people.getJSONObject(personIndex);
            JSONArray pagamentos = person.getJSONArray("pagamentos");

            if (pagamentoIndex >= 0 && pagamentoIndex < pagamentos.length()) {
                pagamentos.remove(pagamentoIndex);
                person.put("pagamentos", pagamentos);
                people.put(personIndex, person);
                json.put("people", people);
                saveJSON(json.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getToday() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }

    private int dp(int i) {
        return (int) TypedValue.applyDimension(1, (float) i, getResources().getDisplayMetrics());
    }
}
