import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.PokemonGo;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by jsh3571 on 10/02/2017.
 */
public class Main {
    private static PokemonGo api;
    private static Locator locator;

    private static final Properties props = new Properties();

    public static void main(String[] args) throws Exception {
        loadProperties();
        login();

        Sniper sniper = new Sniper(api, locator, getHomeLocation());
        sniper.snipe(PokemonIdOuterClass.PokemonId.SNORLAX, getDestLocation());
    }

    private static void loadProperties() {
        try (InputStream inputStream =
                     new FileInputStream("config.properties")) {

            // Loading properties file.
            props.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void login() {
        String username = props.getProperty("username");
        String password = props.getProperty("password");
        UserLogger userLogger = new UserLogger(username, password);

        // Initializing fields.
        api = userLogger.login();
        locator = new Locator(api);
    }

    private static Location getHomeLocation() {
        double homeLatitude =
                Double.parseDouble(props.getProperty("defaultLatitude"));
        double homeLongitude =
                Double.parseDouble(props.getProperty("defaultLongitude"));

        return new Location(homeLatitude, homeLongitude);
    }

    private static Location getDestLocation() {
        double destLatitude =
                Double.parseDouble(props.getProperty("targetLatitude"));
        double destLongitude =
                Double.parseDouble(props.getProperty("targetLongitude"));

        return new Location(destLatitude, destLongitude);
    }
}
