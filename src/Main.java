import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Networking.Responses.CatchPokemonResponseOuterClass;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.PokeBank;
import com.pokegoapi.api.inventory.Pokeball;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.api.settings.CatchOptions;
import com.pokegoapi.api.settings.PokeballSelector;
import com.pokegoapi.util.PokeDictionary;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by jsh3571 on 10/02/2017.
 */
public class Main {
    private static Location home;
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        String username = sc.nextLine();
        String password = sc.nextLine();
        UserLogger userLogger = new UserLogger(username, password);
        userLogger.login();

        String homeLatitude = sc.nextLine();
        String homeLongitude = sc.nextLine();
        home = new Location(
                Double.parseDouble(homeLatitude),
                Double.parseDouble(homeLongitude));
        locate(home);

        String destLatitude = sc.nextLine();
        String destLongitude = sc.nextLine();
        Location dest = new Location(
                Double.parseDouble(destLatitude),
                Double.parseDouble(destLongitude));
        snipe(PokemonIdOuterClass.PokemonId.DRAGONITE, dest);

        TimeUnit.SECONDS.sleep(15);
    }

    private static void locate(Location location) {
        System.out.println("Setting the current location to be: " +
                location.getLatitude() + "," + location.getLongitude());

        api.setLocation(
                location.getLatitude(),
                location.getLongitude(),
                location.getALTITUDE());
    }

    private static void snipe(PokemonIdOuterClass.PokemonId pokemonId,
                              Location location) {
        try {
            locate(location);
            TimeUnit.SECONDS.sleep(15);

            // updateMap();

            Set<CatchablePokemon> catchablePokemons = getCatchablePokemons();

            snipe(pokemonId, catchablePokemons);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Set<CatchablePokemon> getCatchablePokemons() {
        return api.getMap().getMapObjects().getPokemon();
    }

    private static CatchOptions getCatchOptions() {
        return new CatchOptions(api)
                .useRazzberry(true)
                .withPokeballSelector(PokeballSelector.SMART);
    }

    private static void snipe(PokemonIdOuterClass.PokemonId pokemonId,
                              Set<CatchablePokemon> catchablePokemons)
            throws Exception {

        PokeBank pokebank = api.getInventories().getPokebank();

        System.out.println("Catchable pokemons.size(): " +catchablePokemons.size());
        if (catchablePokemons.size() == 0) {
            System.out.println("No pokemons to be caught.");
            locate(home);
            return;
        }

        for (CatchablePokemon cp : catchablePokemons) {
            System.out.println("Pokemon seen: " + cp.getPokemonId());
            if (cp.getPokemonId() != pokemonId)
                continue;

            EncounterResult encResult = cp.encounterPokemon();
            if (encResult.wasSuccessful()) {
                System.out.println("Encountered: " + cp.getPokemonId());

                List<Pokeball> useablePokeballs
                        = api.getInventories().getItemBag().getUseablePokeballs();
                double probability = cp.getCaptureProbability();

                if (useablePokeballs.size() > 0) {
                    Pokeball pokeball =
                            PokeballSelector.SMART.select(useablePokeballs, probability);
                    System.out.println("Attempting to catch: " +
                            cp.getPokemonId() + " with " + pokeball +
                            " (" + probability + ")");

                    // Operate 'pull catch'
                    System.out.println("Flying back to home location to " +
                            "avoid softban.");
                    locate(home);

                    while (!cp.isDespawned()) {
                        // Wait between Pokeball throws.
                        TimeUnit.SECONDS.sleep(3);

                        // Catching pokemon.
                        CatchResult result = cp.catchPokemon(getCatchOptions());
                        System.out.println("Threw ball: " + result.getStatus());
                        if (result.getStatus() ==
                                CatchPokemonResponseOuterClass.
                                        CatchPokemonResponse.
                                        CatchStatus.CATCH_SUCCESS) {

                            // Print pokemon stats
                            Pokemon pokemon = pokebank.getPokemonById(result.getCapturedPokemonId());
                            String name = PokeDictionary.getDisplayName(pokemon.getPokemonId().getNumber(), Locale.ENGLISH);
                            System.out.println("====" + name + "====");
                            System.out.println("CP: " + pokemon.getCp());
                            System.out.println("IV: " + pokemon.getIvInPercentage() + "%");
                            System.out.println("Move 1: " + pokemon.getMove1());
                            System.out.println("Move 2: " + pokemon.getMove2());
                        }
                    }

                    // Waiting for animation before complete catch action.
                    TimeUnit.SECONDS.sleep(5);
                } else {
                    System.out.println("No pokeballs");
                }
            } else {
                System.out.println("Encounter failed. " + encResult.getStatus());
            }
        }
    }

}
