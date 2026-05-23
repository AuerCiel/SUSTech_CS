package OJ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

//快读块写
class FastReader {
    BufferedReader br;
    StringTokenizer st;

    public FastReader(InputStream is) {
        br = new BufferedReader(new InputStreamReader(is));
    }

    public String next() {
        while (st == null || !st.hasMoreElements()) {
            try {
                String line = br.readLine();
                if (line == null) return null;
                st = new StringTokenizer(line);
            } catch (IOException e) {
                return null;
            }
        }
        return st.nextToken();
    }

    public int nextInt() {
        String s = next();
        if (s == null) return -1;
        return Integer.parseInt(s);
    }
}