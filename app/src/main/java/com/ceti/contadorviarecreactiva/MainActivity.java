package com.ceti.contadorviarecreactiva;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private final static String TODAY_STRING = "dia";

    public enum TipoAsistente
    {
        CICLISTA,
        CAMINANTE,
        RUEDAS_MANUAL,
        ELECTRICO
    }

    private int contadorCiclistas;
    private int contadorCaminantes;
    private int contadorRuedasManual;
    private int contadorElectrico;

    private TextView ciclistasTv;
    private TextView caminantesTv;
    private TextView ruedasTv;
    private TextView electricoTv;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardView ciclistasCv = findViewById(R.id.ciclistas_cv);
        CardView caminantesCv = findViewById(R.id.caminantes_cv);
        CardView ruedasManualCv = findViewById(R.id.ruedas_cv);
        CardView electricoCv = findViewById(R.id.electrico_cv);

        ciclistasTv = findViewById(R.id.ciclistas_tv);
        caminantesTv = findViewById(R.id.caminantes_tv);
        ruedasTv = findViewById(R.id.ruedas_tv);
        electricoTv = findViewById(R.id.electrico_tv);

        // Definir listeners
        View.OnClickListener counterListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TipoAsistente tipoAsistente = null;
                switch (v.getId())
                {
                    case R.id.ciclistas_cv:
                        tipoAsistente = TipoAsistente.CICLISTA;
                        break;

                    case R.id.caminantes_cv:
                        tipoAsistente = TipoAsistente.CAMINANTE;
                        break;

                    case R.id.ruedas_cv:
                        tipoAsistente = TipoAsistente.RUEDAS_MANUAL;
                        break;

                    case R.id.electrico_cv:
                        tipoAsistente = TipoAsistente.ELECTRICO;
                        break;
                }

                if (tipoAsistente != null)
                    addToCounter(tipoAsistente);
            }
        };

        // Definir listeners
        ciclistasCv.setOnClickListener(counterListener);
        caminantesCv.setOnClickListener(counterListener);
        ruedasManualCv.setOnClickListener(counterListener);
        electricoCv.setOnClickListener(counterListener);

        // Validar y obtener
        validateDay();
        getCounters();

        // Definir valores
        setValues();

        // Checar permisos
        checkPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.reset_counters)
        {
            resetCounters();
            getCounters();
            setValues();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setValues()
    {
        ciclistasTv.setText(String.valueOf(contadorCiclistas));
        caminantesTv.setText(String.valueOf(contadorCaminantes));
        ruedasTv.setText(String.valueOf(contadorRuedasManual));
        electricoTv.setText(String.valueOf(contadorElectrico));
    }

    private void addToCounter(TipoAsistente tipoAsistente)
    {
        int value = 1;

        switch (tipoAsistente)
        {
            case CICLISTA:
                contadorCiclistas++;
                value = contadorCiclistas;
                break;

            case CAMINANTE:
                contadorCaminantes++;
                value = contadorCaminantes;
                break;

            case RUEDAS_MANUAL:
                contadorRuedasManual++;
                value = contadorRuedasManual;
                break;

            case ELECTRICO:
                contadorElectrico++;
                value = contadorElectrico;
                break;
        }

        // Actualizar views
        setValues();

        // Guardar en archivos
        saveCounterValue(tipoAsistente.name(), value);
        addRegisterToFile(tipoAsistente.name(), value);
    }

    private void getCounters()
    {
        // Crear instancia de shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("Contadores", MODE_PRIVATE);

        // Validar si es del día de hoy
        contadorCiclistas = sharedPreferences.getInt(TipoAsistente.CICLISTA.name(), 0);
        contadorCaminantes = sharedPreferences.getInt(TipoAsistente.CAMINANTE.name(), 0);
        contadorRuedasManual = sharedPreferences.getInt(TipoAsistente.RUEDAS_MANUAL.name(), 0);
        contadorElectrico = sharedPreferences.getInt(TipoAsistente.ELECTRICO.name(), 0);
    }

    private void validateDay()
    {
        // Crear instancia de shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("Contadores", MODE_PRIVATE);

        // Validar si es del día de hoy
        String savedDate = sharedPreferences.getString(TODAY_STRING, "");
        if (savedDate == null) savedDate = "";
        if (savedDate.equals(todayDateToString()))
        {
            // Reiniciar contadores
            resetCounters();
        }
    }

    private void saveCounterValue(String key, int val)
    {
        // Crear instancia de shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("Contadores", MODE_PRIVATE);

        // Guardar nuevo valor
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, val).apply();
    }

    private void resetCounters()
    {
        // Crear instancia de shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("Contadores", MODE_PRIVATE);
        sharedPreferences.edit().putInt(TipoAsistente.CICLISTA.name(), 0).apply();
        sharedPreferences.edit().putInt(TipoAsistente.CAMINANTE.name(), 0).apply();
        sharedPreferences.edit().putInt(TipoAsistente.RUEDAS_MANUAL.name(), 0).apply();
        sharedPreferences.edit().putInt(TipoAsistente.ELECTRICO.name(), 0).apply();
    }

    private String todayDateToString()
    {
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        return dateFormat.format(today);
    }

    private String nowToString()
    {
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(today);
    }

    private void checkPermission()
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
    }

    private void addRegisterToFile(String key, int value)
    {
        String fileName = key + "_" + todayDateToString() + ".txt";
        String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/RegistrosViaRecreactiva";

        File dir = new File(dirPath);
        if (!dir.exists())
        {
            dir.mkdir();
        }

        File file = new File(dir, fileName);
        if (!file.exists())
        {
            // Escribir cabecera
            String header = "Numero,Tipo,FechaHora\n\r";
            writeToFile(file, header);
        }

        // Agregar registro
        writeToFile(file, value + "," + key + "," + nowToString() + "\n\r");
    }

    private void writeToFile(File file, String data)
    {
        try
        {
            FileOutputStream stream = new FileOutputStream(file, file.exists());
            stream.write(data.getBytes());
            stream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
