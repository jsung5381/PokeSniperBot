import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.exceptions.CaptchaActiveException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import okhttp3.OkHttpClient;

import java.util.Scanner;

/**
 * Created by jsh3571 on 10/02/2017.
 */

public class GoogleUserLogger {

    public GoogleUserLogger() {
        OkHttpClient httpClient = new OkHttpClient();

        try {
            // Instantiate a provider, it will give an url
            GoogleUserCredentialProvider provider =
                    new GoogleUserCredentialProvider(httpClient);

            // In this url, you will get a code for the google account that is logged
            System.out.println("Please go to " + provider.LOGIN_URL);
            System.out.print("Enter authorisation code: ");

            // Ask the user to enter it in the standard input
            Scanner sc = new Scanner(System.in);
            String authCode = sc.nextLine();

            // we should be able to login with this token
            provider.login(authCode);
            System.out.println("Refresh token:" + provider.getRefreshToken());
        } catch (LoginFailedException
                | RemoteServerException
                | CaptchaActiveException e) {
            e.printStackTrace();
        }
    }
}
