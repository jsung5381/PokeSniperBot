import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.GoogleAutoCredentialProvider;
import com.pokegoapi.exceptions.CaptchaActiveException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.util.SystemTimeImpl;
import com.pokegoapi.util.hash.HashProvider;
import com.pokegoapi.util.hash.legacy.LegacyHashProvider;
import okhttp3.OkHttpClient;

/**
 * Created by jsh3571 on 12/02/2017.
 */
public class UserLogger {
    private PokemonGo go;
    private GoogleAutoCredentialProvider authProvider;
    private HashProvider hashProvider;

    public UserLogger(String username, String password) {
        OkHttpClient http = new OkHttpClient();
        go = new PokemonGo(http);

        try {
            authProvider = new GoogleAutoCredentialProvider(http,
                    username, password, new SystemTimeImpl());
            hashProvider = new LegacyHashProvider();
        } catch (LoginFailedException | CaptchaActiveException |
                RemoteServerException e) {
            e.printStackTrace();
        }
    }

    public void login() {
        try {
            go.login(authProvider, hashProvider);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
