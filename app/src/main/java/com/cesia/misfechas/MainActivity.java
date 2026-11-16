package com.cesia.misfechas;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int VERDE = Color.rgb(46, 125, 50);
    private static final int VERDE_CLARO = Color.rgb(232, 245, 233);
    private static final int GRIS = Color.rgb(158, 158, 158);
    private static final int NARANJA = Color.rgb(230, 126, 34);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pedir permiso de notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        // (Re)programar todos los avisos cada vez que se abre la app
        Plan.programarAvisos(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.WHITE);
        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        raiz.setPadding(pad, pad, pad, pad);
        scroll.addView(raiz);

        TextView titulo = new TextView(this);
        titulo.setText("🌱 Mis Fechas de Campo");
        titulo.setTextSize(26);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setTextColor(VERDE);
        raiz.addView(titulo);

        TextView sub = new TextView(this);
        sub.setText("Fumigación y cosecha - Chimoré\nAvisos: el día anterior a las 18:00 y el mismo día a las 07:00");
        sub.setTextSize(14);
        sub.setTextColor(Color.DKGRAY);
        sub.setPadding(0, dp(4), 0, dp(12));
        raiz.addView(sub);

        boolean yaPuseProximo = false;
        String cicloActual = "";
        for (Plan.Evento e : Plan.EVENTOS) {
            String ciclo = e.fechaIso.compareTo("2027-01-19") <= 0
                    ? "CICLO ACTUAL - cosecha 18 de enero 2027"
                    : "CICLO ENERO - ABRIL 2027";
            if (!ciclo.equals(cicloActual)) {
                cicloActual = ciclo;
                TextView h = new TextView(this);
                h.setText(cicloActual);
                h.setTextSize(15);
                h.setTypeface(Typeface.DEFAULT_BOLD);
                h.setTextColor(Color.WHITE);
                h.setBackgroundColor(VERDE);
                h.setPadding(dp(10), dp(8), dp(10), dp(8));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, dp(10), 0, dp(8));
                h.setLayoutParams(lp);
                raiz.addView(h);
            }

            long dias = Plan.diasFaltan(e.fechaIso);
            boolean pasado = dias < 0;
            boolean esProximo = !pasado && !yaPuseProximo;
            if (esProximo) yaPuseProximo = true;

            LinearLayout tarjeta = new LinearLayout(this);
            tarjeta.setOrientation(LinearLayout.VERTICAL);
            tarjeta.setPadding(dp(12), dp(10), dp(12), dp(10));
            GradientDrawable fondo = new GradientDrawable();
            fondo.setCornerRadius(dp(10));
            fondo.setColor(esProximo ? VERDE_CLARO : Color.rgb(250, 250, 250));
            fondo.setStroke(dp(1), esProximo ? VERDE : Color.rgb(224, 224, 224));
            tarjeta.setBackground(fondo);
            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp2.setMargins(0, 0, 0, dp(8));
            tarjeta.setLayoutParams(lp2);

            TextView cuando = new TextView(this);
            cuando.setText(etiquetaDias(dias));
            cuando.setTextSize(13);
            cuando.setTypeface(Typeface.DEFAULT_BOLD);
            cuando.setTextColor(pasado ? GRIS : (dias == 0 ? Color.RED : (dias <= 3 ? NARANJA : VERDE)));
            tarjeta.addView(cuando);

            TextView fecha = new TextView(this);
            fecha.setText(Plan.fechaLinda(e.fechaIso));
            fecha.setTextSize(14);
            fecha.setTextColor(pasado ? GRIS : Color.DKGRAY);
            tarjeta.addView(fecha);

            TextView nombre = new TextView(this);
            nombre.setText(e.titulo);
            nombre.setTextSize(esProximo ? 21 : 18);
            nombre.setTypeface(Typeface.DEFAULT_BOLD);
            nombre.setTextColor(pasado ? GRIS : Color.BLACK);
            tarjeta.addView(nombre);

            if (e.detalle != null && !e.detalle.isEmpty()
                    && !e.detalle.startsWith("Ciclo")) {
                TextView det = new TextView(this);
                det.setText(e.detalle);
                det.setTextSize(15);
                det.setTextColor(pasado ? GRIS : Color.rgb(66, 66, 66));
                tarjeta.addView(det);
            }

            raiz.addView(tarjeta);
        }

        TextView pie = new TextView(this);
        pie.setText("Tu meta es clara: 17 a 20 paquetes 🌾");
        pie.setTextSize(16);
        pie.setTypeface(Typeface.DEFAULT_BOLD);
        pie.setTextColor(VERDE);
        pie.setGravity(Gravity.CENTER);
        pie.setPadding(0, dp(12), 0, dp(8));
        raiz.addView(pie);

        setContentView(scroll);
    }

    private String etiquetaDias(long dias) {
        if (dias < 0) return "✓ Ya pasó";
        if (dias == 0) return "🔴 ¡ES HOY!";
        if (dias == 1) return "🟠 Es MAÑANA";
        return "⏳ Faltan " + dias + " días";
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
