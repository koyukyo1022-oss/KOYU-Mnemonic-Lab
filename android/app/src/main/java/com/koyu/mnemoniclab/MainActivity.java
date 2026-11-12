package com.koyu.mnemoniclab;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final SecureRandom random = new SecureRandom();
    private final ArrayList<String> records = new ArrayList<>();
    private TextView output;
    private EditText bitsInput, trialsInput;
    private static final int CREATE_FILE = 41;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xff10141d);
        getWindow().setNavigationBarColor(0xff10141d);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(22, 28, 22, 18);
        page.setBackgroundColor(0xff10141d);

        TextView title = text("KOYU MNEMONIC LAB", 24, 0xff70d6ff, true);
        page.addView(title);
        page.addView(text("数学碰撞模拟  /  多链测试标识符实验", 13, 0xff9aa8ba, false));

        LinearLayout params = new LinearLayout(this);
        params.setOrientation(LinearLayout.HORIZONTAL);
        params.setPadding(0, 22, 0, 12);
        bitsInput = input("16");
        trialsInput = input("100000");
        params.addView(bitsInput, new LinearLayout.LayoutParams(0, 52, 1));
        params.addView(trialsInput, new LinearLayout.LayoutParams(0, 52, 1));
        page.addView(params);
        page.addView(text("左：位数 4–32    右：最大抽样次数", 11, 0xffaab6c5, false));

        Button run = button("运行碰撞实验");
        page.addView(run);
        run.setOnClickListener(v -> runExperiment());

        Button chains = button("生成多链模拟标识符");
        page.addView(chains);
        chains.setOnClickListener(v -> makeIdentifiers());

        Button export = button("导出 CSV");
        page.addView(export);
        export.setOnClickListener(v -> exportCsv());

        output = text("等待实验……\n\n降低位数更容易观察到碰撞；增加位数会显著降低碰撞概率。\n", 13, 0xffdce7f5, false);
        output.setTypeface(Typeface.MONOSPACE);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xff0b0f16);
        scroll.addView(output);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, 0, 1);
        sp.setMargins(0, 18, 0, 14);
        page.addView(scroll, sp);

        page.addView(text("安全说明：生成的是模拟标识符，不是真实钱包地址；不扫描主网，不收集助记词或私钥。", 11, 0xffffca7a, false));
        setContentView(page);
    }

    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, 5, 0, 5);
        return t;
    }

    private EditText input(String value) {
        EditText e = new EditText(this);
        e.setSingleLine(true); e.setText(value); e.setTextColor(0xfff0f4fa);
        e.setHintTextColor(0xff8290a2); e.setHint("参数");
        e.setPadding(12, 0, 12, 0);
        e.setBackgroundColor(0xff1a2230);
        return e;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(0xffe8edf5);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff26364a));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, 52);
        p.setMargins(0, 7, 0, 0);
        b.setLayoutParams(p);
        return b;
    }

    private void log(String s) {
        output.append("\n" + s);
    }

    private double probability(int bits, int n) {
        if (n <= 1) return 0.0;
        double space = Math.pow(2.0, bits);
        return -Math.expm1(-((double)n * (n - 1)) / (2.0 * space));
    }

    private void runExperiment() {
        try {
            int bits = Integer.parseInt(bitsInput.getText().toString().trim());
            int trials = Integer.parseInt(trialsInput.getText().toString().trim());
            if (bits < 4 || bits > 32) throw new IllegalArgumentException("位数范围为 4–32。");
            if (trials < 1 || trials > 2000000) throw new IllegalArgumentException("抽样次数范围为 1–2,000,000。");

            HashSet<Long> seen = new HashSet<>();
            Long collision = null;
            int at = -1;
            long mask = bits == 32 ? 0xffffffffL : ((1L << bits) - 1L);
            for (int i = 1; i <= trials; i++) {
                long value = random.nextInt() & mask;
                if (!seen.add(value)) { at = i; collision = value; break; }
            }
            double p = probability(bits, trials);
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
            log("[" + time + "] " + bits + " bit / " + trials + " 次");
            log(at < 0 ? "结果：本轮未发现重复值。" : "结果：第 " + at + " 次首次重复，实验编号=" + collision);
            log("理论碰撞概率（生日悖论近似）：" + new DecimalFormat("0.000000").format(p * 100) + "%");
            records.add(time + ",collision," + bits + "," + trials + "," + at + "," + p + ",");
        } catch (Exception ex) {
            new AlertDialog.Builder(this).setTitle("参数错误").setMessage(ex.getMessage()).setPositiveButton("知道了", null).show();
        }
    }

    private String hex(byte[] data) {
        StringBuilder b = new StringBuilder();
        for (byte x : data) b.append(String.format(Locale.US, "%02x", x));
        return b.toString();
    }

    private void makeIdentifiers() {
        String[] chains = {"BTC", "ETH", "BNB", "SOL"};
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        log("多链模拟标识符（不可用于链上交易）：");
        try {
            for (String chain : chains) {
                byte[] raw = new byte[32]; random.nextBytes(raw);
                byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw);
                String id = chain + "-TEST-SIM-" + hex(digest).substring(0, 32);
                log(id);
                records.add(time + ",simulated_identifier,,,,," + chain + ":" + id);
            }
        } catch (Exception ignored) {}
    }

    private void exportCsv() {
        if (records.isEmpty()) {
            Toast.makeText(this, "请先运行实验或生成模拟标识符", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "koyu_mnemonic_lab_results.csv");
        startActivityForResult(intent, CREATE_FILE);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try (java.io.OutputStream os = getContentResolver().openOutputStream(uri)) {
                StringBuilder csv = new StringBuilder("time,type,bits,trials,first_collision_at,theoretical_probability,details\n");
                for (String r : records) csv.append(r).append("\n");
                os.write(csv.toString().getBytes(StandardCharsets.UTF_8));
                Toast.makeText(this, "CSV 已导出", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "导出失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
