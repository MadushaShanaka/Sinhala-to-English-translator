package main.java;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;

import com.darkprograms.speech.translator.GoogleTranslate;

public class GoogleTranslator {
    public static void main(String[] args) {
        String inputFile = "C:/Users/Madusha Shanaka/Downloads/input.csv"; // Update with your input CSV file path
        String outputFile = "C:/Users/Madusha Shanaka/Downloads/output.csv"; // Update with your output CSV file path

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
             FileWriter writer = new FileWriter(outputFile)) {

            String line;
            boolean isFirstLine = true;

            // Reading the CSV file line by line
            while ((line = br.readLine()) != null) {
                // Skip header
                if (isFirstLine) {
                    writer.write(line + ",Translated Name,Translated Location\n");
                    isFirstLine = false;
                    continue;
                }

                String[] columns = line.split(",");
                if (columns.length >= 3) {
                    String nameSinhala = columns[1];
                    String locationSinhala = columns[2];

                    // Translate name and location from Sinhala to English
                    String nameEnglish = GoogleTranslate.translate("si","en", nameSinhala);
                    String locationEnglish = translate("si","en", locationSinhala);

                    // Write the result to the new CSV file
                    writer.write(columns[0] + "," +
                            nameEnglish + "," + locationEnglish + "\n");
                }
            }

            System.out.println("Translation completed successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String translate(String sourceLanguage, String targetLanguage, String text) throws IOException {
        String urlText = generateURL(sourceLanguage, targetLanguage, text);
        URL url = new URL(urlText);
        String rawData = urlToText(url);
        if (rawData == null) {
            return null;
        } else {
            String[] raw = rawData.split("\"");
            return raw.length < 2 ? null : raw[1];
        }
    }

    private static String urlToText(URL url) throws IOException {
        URLConnection urlConn = url.openConnection();
        urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0) Gecko/20100101 Firefox/4.0");
        Reader r = new InputStreamReader(urlConn.getInputStream(), Charset.forName("UTF-8"));
        StringBuilder buf = new StringBuilder();

        while(true) {
            int ch = r.read();
            if (ch < 0) {
                String str = buf.toString();
                return str;
            }

            buf.append((char)ch);
        }
    }


    private static String generateURL(String sourceLanguage, String targetLanguage, String text) throws UnsupportedEncodingException {
        String encoded = URLEncoder.encode(text, "UTF-8");
        StringBuilder sb = new StringBuilder();
        sb.append("http://translate.google.com/translate_a/single");
        sb.append("?client=webapp");
        sb.append("&hl=en");
        sb.append("&sl=");
        sb.append(sourceLanguage);
        sb.append("&tl=");
        sb.append(targetLanguage);
        sb.append("&q=");
        sb.append(encoded);
        sb.append("&multires=1");
        sb.append("&otf=0");
        sb.append("&pc=0");
        sb.append("&trs=1");
        sb.append("&ssel=0");
        sb.append("&tsel=0");
        sb.append("&kc=1");
        sb.append("&dt=t");
        sb.append("&ie=UTF-8");
        sb.append("&oe=UTF-8");
        sb.append("&tk=");
        sb.append(generateToken(text));
        sb.append("&op=translate");
        return sb.toString();
    }

    private static String generateToken(String text) {
        int[] tkk = TKK();
        int b = tkk[0];
        int e = 0;
        int f = 0;

        ArrayList d;
        int g;
        for(d = new ArrayList(); f < text.length(); ++f) {
            g = text.charAt(f);
            if (128 > g) {
                d.add(e++, g);
            } else {
                if (2048 > g) {
                    d.add(e++, g >> 6 | 192);
                } else if (55296 == (g & 'ﰀ') && f + 1 < text.length() && 56320 == (text.charAt(f + 1) & 'ﰀ')) {
                    int var10000 = 65536 + ((g & 1023) << 10);
                    ++f;
                    g = var10000 + (text.charAt(f) & 1023);
                    d.add(e++, g >> 18 | 240);
                    d.add(e++, g >> 12 & 63 | 128);
                } else {
                    d.add(e++, g >> 12 | 224);
                    d.add(e++, g >> 6 & 63 | 128);
                }

                d.add(e++, g & 63 | 128);
            }
        }

        g = b;

        for(e = 0; e < d.size(); ++e) {
            g += (Integer)d.get(e);
            g = RL(g, "+-a^+6");
        }

        g = RL(g, "+-3^+b+-f");
        g ^= tkk[1];
        long a_l;
        if (0 > g) {
            a_l = 2147483648L + (long)(g & Integer.MAX_VALUE);
        } else {
            a_l = (long)g;
        }

        a_l = (long)((double)a_l % Math.pow(10.0, 6.0));
        return String.format(Locale.US, "%d.%d", a_l, a_l ^ (long)b);
    }

    private static int[] TKK() {
        return new int[]{406398, 2087938574};
    }
    private static int RL(int a, String b) {
        for(int c = 0; c < b.length() - 2; c += 3) {
            int d = b.charAt(c + 2);
            d = d >= 65 ? d - 87 : d - 48;
            d = b.charAt(c + 1) == '+' ? shr32(a, d) : a << d;
            a = b.charAt(c) == '+' ? a + (d & -1) : a ^ d;
        }

        return a;
    }

    private static int shr32(int x, int bits) {
        if (x < 0) {
            long x_l = 4294967295L + (long)x + 1L;
            return (int)(x_l >> bits);
        } else {
            return x >> bits;
        }
    }
}
