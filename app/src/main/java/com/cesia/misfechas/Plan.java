package com.cesia.misfechas;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Plan {

    public static class Evento {
        public final String fechaIso;
        public final String titulo;
        public final String detalle;

        public Evento(String fechaIso, String titulo, String detalle) {
            this.fechaIso = fechaIso;
            this.titulo = titulo;
            this.detalle = detalle;
        }
    }

    private static final String CTRL = "Insecticida: en la MAÑANA\nFungicida: en la TARDE";

    public static final Evento[] EVENTOS = new Evento[]{
            // ===== Ciclo actual: cosecha final 18 ene 2027 =====
            new Evento("2026-10-18", "Cosecha base (17 paquetes)", "Ciclo actual - Chimoré"),
            new Evento("2026-10-25", "Bellator Quattour (aplicación aparte)", "Insecticida + fungicida, no mueve ninguna otra fecha"),
            new Evento("2026-10-27", "Nutrición Foliar 1", "Ciclo actual - Chimoré"),
            new Evento("2026-11-10", "Nutrición Foliar 2 (abono)", "Ciclo actual - Chimoré"),
            new Evento("2026-11-11", "Control 1", CTRL),
            new Evento("2026-11-19", "Nutrición al suelo", "Ciclo actual - Chimoré"),
            new Evento("2026-11-25", "Control 2", CTRL),
            new Evento("2026-11-26", "Nutrición 2da fase (engrose)", "Ciclo actual - Chimoré"),
            new Evento("2026-12-09", "Control 3", CTRL),
            new Evento("2026-12-23", "Control 4 - ÚLTIMO control", CTRL),
            new Evento("2026-12-24", "Empieza la CARENCIA", "No fumigar más hasta la cosecha (hasta el 17 de enero)"),
            new Evento("2027-01-18", "🌾 COSECHA FINAL", "Meta: 17 a 20 paquetes"),

            // ===== Ciclo enero - abril 2027 =====
            new Evento("2027-01-20", "Cosecha base / inicio del ciclo", "Ciclo enero-abril 2027"),
            new Evento("2027-01-25", "Control 1", CTRL),
            new Evento("2027-01-29", "Nutrición Foliar 1", "Ciclo enero-abril 2027"),
            new Evento("2027-02-08", "Control 2", CTRL),
            new Evento("2027-02-12", "Nutrición Foliar 2 (abono)", "Ciclo enero-abril 2027"),
            new Evento("2027-02-21", "Nutrición al suelo", "Ciclo enero-abril 2027"),
            new Evento("2027-02-22", "Control 3", CTRL),
            new Evento("2027-02-28", "Nutrición 2da fase (engrose)", "Ciclo enero-abril 2027"),
            new Evento("2027-03-08", "Control 4", CTRL),
            new Evento("2027-03-22", "Control 5 - ÚLTIMO control", CTRL),
            new Evento("2027-03-23", "Empieza la CARENCIA", "No fumigar más hasta la cosecha (hasta el 19 de abril)"),
            new Evento("2027-04-20", "🌾 COSECHA FINAL", "Ciclo enero-abril 2027"),
    };

    /** Milisegundos del día indicado a la hora indicada (hora local). */
    public static long millisEn(String fechaIso, int hora) {
        try {
            Calendar c = Calendar.getInstance();
            java.util.Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fechaIso);
            c.setTime(d);
            c.set(Calendar.HOUR_OF_DAY, hora);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    /** Programa dos avisos por evento futuro: el día anterior 18:00 y el mismo día 07:00. */
    public static void programarAvisos(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        long ahora = System.currentTimeMillis();
        for (int i = 0; i < EVENTOS.length; i++) {
            Evento e = EVENTOS[i];
            // Aviso del día anterior a las 18:00
            long antes = millisEn(e.fechaIso, 18) - 24L * 60 * 60 * 1000;
            if (antes > ahora) {
                programar(am, ctx, i * 10 + 1, antes,
                        "🌱 Mañana toca: " + e.titulo, textoAviso(e));
            }
            // Aviso del mismo día a las 07:00
            long mismoDia = millisEn(e.fechaIso, 7);
            if (mismoDia > ahora) {
                programar(am, ctx, i * 10 + 2, mismoDia,
                        "🚜 Hoy toca: " + e.titulo, textoAviso(e));
            }
        }
    }

    private static String textoAviso(Evento e) {
        String fecha = fechaLinda(e.fechaIso);
        if (e.detalle == null || e.detalle.isEmpty()) return fecha;
        return fecha + "\n" + e.detalle;
    }

    private static void programar(AlarmManager am, Context ctx, int codigo, long cuando,
                                  String titulo, String texto) {
        Intent intent = new Intent(ctx, AvisoReceiver.class);
        intent.putExtra("titulo", titulo);
        intent.putExtra("texto", texto);
        intent.putExtra("id", codigo);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, codigo, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cuando, pi);
    }

    /** "2026-11-25" -> "miércoles 25 de noviembre de 2026" */
    public static String fechaLinda(String fechaIso) {
        try {
            java.util.Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fechaIso);
            String s = new SimpleDateFormat("EEEE d 'de' MMMM 'de' yyyy", new Locale("es")).format(d);
            return s.substring(0, 1).toUpperCase(new Locale("es")) + s.substring(1);
        } catch (Exception e) {
            return fechaIso;
        }
    }

    /** Días completos desde hoy (inicio del día) hasta la fecha del evento. Negativo si ya pasó. */
    public static long diasFaltan(String fechaIso) {
        long hoy = inicioDelDia(System.currentTimeMillis());
        long ese = inicioDelDia(millisEn(fechaIso, 12));
        return Math.round((ese - hoy) / (24.0 * 60 * 60 * 1000));
    }

    private static long inicioDelDia(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
