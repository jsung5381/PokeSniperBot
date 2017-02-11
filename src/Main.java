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
import com.pokegoapi.auth.GoogleAutoCredentialProvider;
import com.pokegoapi.util.PokeDictionary;
import com.pokegoapi.util.SystemTimeImpl;
import com.pokegoapi.util.hash.HashProvider;
import com.pokegoapi.util.hash.legacy.LegacyHashProvider;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * Created by jsh3571 on 10/02/2017.
 */
public class Main {
    private static PokemonGo api;
    private static double latitude;
    private static double longitude;

    public static void main(String[] args) throws Exception {
        login(args[0], args[1]);

        latitude = Double.parseDouble(args[2]);
        longitude = Double.parseDouble(args[3]);
        locate(latitude, longitude, 15.0);

        snipe(PokemonIdOuterClass.PokemonId.CHANSEY, 37.50770728, 127.05064184);

        Thread.sleep(2000);
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

    private static void locate(double latitude,
                               double longitude,
                               double altitude) {
        System.out.println("Setting the current location to be: " +
                latitude + "," + longitude);

        api.setLocation(latitude, longitude, altitude);
    }

    private static void snipe(PokemonIdOuterClass.PokemonId pokemonId,
                              double latitude, double longitude) {
        try {
            locate(latitude, longitude, 15.0);
            updateMap();

            Set<CatchablePokemon> catchablePokemons = getCatchablePokemons();

            Random random = new Random();
            PokeBank pokebank = api.getInventories().getPokebank();

            // f(catchablePokemons);
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
                        Pokeball pokeball = PokeballSelector.SMART.select(useablePokeballs, probability);
                        System.out.println("Attempting to catch: " +
                                cp.getPokemonId() + " with " + pokeball +
                                " (" + probability + ")");

                        // Operate 'pull catch'
                        locate(latitude, longitude, 15.0);

                        while (!cp.isDespawned()) {
                            // Wait between Pokeball throws.
                            Thread.sleep(1000 + random.nextInt(1000));

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
                        Thread.sleep(3000 + random.nextInt(1000));
                    } else {
                        System.out.println("No pokeballs");
                    }
                } else {
                    System.out.println("Encounter failed. " + encResult.getStatus());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update current location and nearby objects.
     */
    private static void updateMap() {
        System.out.println("Updating the map and nearby objects..");

        try {
            api.getMap().awaitUpdate();
        } catch (InterruptedException e) {
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
}
