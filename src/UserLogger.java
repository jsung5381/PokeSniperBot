import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.GoogleAutoCredentialProvider;
import com.pokegoapi.util.SystemTimeImpl;
import com.pokegoapi.util.hash.HashProvider;
import com.pokegoapi.util.hash.legacy.LegacyHashProvider;
import com.pokegoapi.util.hash.pokehash.PokeHashKey;
import com.pokegoapi.util.hash.pokehash.PokeHashProvider;
import okhttp3.OkHttpClient;

/**
 * Created by jsh3571 on 12/02/2017.
 */
public class UserLogger {
    private PokemonGo go;
    private GoogleAutoCredentialProvider authProvider;
    private HashProvider hashProvider;

    public UserLogger(String username, String password, String hashKey) {
        OkHttpClient http = new OkHttpClient();
        go = new PokemonGo(http);

        try {
            authProvider = new GoogleAutoCredentialProvider(http,
                    username, password, new SystemTimeImpl());
            hashProvider = getHashProvider(hashKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PokemonGo login() {
        try {
            go.login(authProvider, hashProvider);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return go;
    }

    private HashProvider getHashProvider(String pokeHashKey) {
        boolean hasKey = pokeHashKey != null && pokeHashKey.length() > 0;
        if (hasKey) {
            return new PokeHashProvider(PokeHashKey.from(pokeHashKey), true);
        } else {
            return new LegacyHashProvider();
        }
    }
}
