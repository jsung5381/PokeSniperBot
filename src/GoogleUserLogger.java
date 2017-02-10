import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.GoogleAutoCredentialProvider;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.exceptions.CaptchaActiveException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.util.SystemTimeImpl;
import okhttp3.OkHttpClient;

import java.util.Scanner;

/**
 * Created by jsh3571 on 10/02/2017.
 */

public class GoogleUserLogger {
    private String username, password;

    public GoogleUserLogger() {
        OkHttpClient httpClient = new OkHttpClient();

        try {
            // Instantiate a provider, it will give an url
            GoogleAutoCredentialProvider provider =
                    new GoogleAutoCredentialProvider(httpClient, username, password, new SystemTimeImpl());
            PokemonGo go = new PokemonGo(httpClient);
            // go.login(provider);
        } catch (LoginFailedException
                | RemoteServerException
                | CaptchaActiveException e) {
            e.printStackTrace();
        }
    }
}
