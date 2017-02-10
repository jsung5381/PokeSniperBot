import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.GoogleAutoCredentialProvider;
import com.pokegoapi.util.SystemTimeImpl;
import com.pokegoapi.util.hash.HashProvider;
import com.pokegoapi.util.hash.legacy.LegacyHashProvider;
import okhttp3.OkHttpClient;

/**
 * Created by jsh3571 on 10/02/2017.
 */
public class Main {
    private static PokemonGo api;

    public static void main(String[] args) {
        login(args[0], args[1]);
        locate(37.497942,127.0254323, 1);

    }

    private static void login(String username, String password) {
        OkHttpClient http = new OkHttpClient();
        api = new PokemonGo(http);
        try {
            GoogleAutoCredentialProvider provider =
                    new GoogleAutoCredentialProvider(
                            http, username, password, new SystemTimeImpl());
            HashProvider hasher = new LegacyHashProvider();

            api.login(provider, hasher);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void locate(double latitude, double longtitude, double altitude) {
        api.setLocation(latitude, longtitude, altitude);
    }
}
