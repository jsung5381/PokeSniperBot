import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.PokemonGo;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by jsh3571 on 10/02/2017.
 */
public class Main {
    private static PokemonGo api;
    private static Locator locator;
    private static Location home;

    private static Properties prop = new Properties();

    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        loadProperties();

        String username = prop.getProperty("username");
        System.out.println("Logging in with username: " + username);
        System.out.print("Please type your password in: ");
        String password = SCANNER.nextLine();
        UserLogger userLogger = new UserLogger(username, password);

        api = userLogger.login();
        locator = new Locator(api);

        double homeLatitude =
                Double.parseDouble(prop.getProperty("defaultLatitude"));
        double homeLongitude =
                Double.parseDouble(prop.getProperty("defaultLongitude"));
        home = new Location(homeLatitude, homeLongitude);
        locator.locate(home);

        System.out.println("Please type your 'dest' latitude and longitude.");
        double destLatitude = Double.parseDouble(SCANNER.nextLine());
        double destLongitude = Double.parseDouble(SCANNER.nextLine());
        Location dest = new Location(destLatitude, destLongitude);

        Sniper sniper = new Sniper(api, locator, home);

        sniper.snipe(PokemonIdOuterClass.PokemonId.SNORLAX, dest);
    }

    private static void loadProperties() {
        try {
            InputStream input =
                    new FileInputStream("src/config.properties");

            // load a properties file
            prop.load(input);

            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
