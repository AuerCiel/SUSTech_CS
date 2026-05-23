package OJ;
import java.util.Scanner;


public class OJ_8 {
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        long n = in.nextLong();
        long MOD = 1000001011L;
        long pow2 = q_pow(2, n, MOD);
        long ans;
        if (n % 2 == 1) {
            ans = (14L * pow2 % MOD - 9L * (n % MOD) - 10) % MOD;
        } else {
            ans = (14L * pow2 % MOD - 9L * (n % MOD) - 11) % MOD;
        }
        ans = (ans % MOD + MOD) % MOD;
        // 除以3 => 乘以3在mod下的逆元
        long inv3 = q_pow(3, MOD - 2, MOD);
        ans = ans * inv3 % MOD;
        System.out.println(ans);
        in.close();
    }
    //快速幂取模
    public static long q_pow(long a, long b, long mod) {
        long res = 1;
        a %= mod;
        while(b > 0) {
            if ((b & 1) == 1) {
                res = res * a % mod;
            }
            a = a * a % mod;
            b >>= 1;
        }
        return res;
    }
}