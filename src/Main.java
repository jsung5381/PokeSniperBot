import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.PokemonGo;

import java.util.Scanner;

/**
 * Created by jsh3571 on 10/02/2017.
 */
public class Main {
    private static PokemonGo api;
    private static Location home;
    private static Locator locator;
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        String username = SCANNER.nextLine();
        String password = SCANNER.nextLine();
        UserLogger userLogger = new UserLogger(username, password);

        api = userLogger.login();
        locator = new Locator(api);

        double homeLatitude = Double.parseDouble(SCANNER.nextLine());
        double homeLongitude = Double.parseDouble(SCANNER.nextLine());
        home = new Location(homeLatitude, homeLongitude);
        locator.locate(home);

        double destLatitude = Double.parseDouble(SCANNER.nextLine());
        double destLongitude = Double.parseDouble(SCANNER.nextLine());
        Location dest = new Location(destLatitude, destLongitude);

        Sniper sniper = new Sniper(api, locator, home);

        sniper.snipe(PokemonIdOuterClass.PokemonId.DRAGONITE, dest);
    }
}
