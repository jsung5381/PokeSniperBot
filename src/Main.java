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
import com.pokegoapi.exceptions.hash.HashException;
import com.pokegoapi.util.Log;
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

    public static void main(String[] args) {
        login(args[0], args[1]);
        locate(Double.parseDouble(args[2]),Double.parseDouble(args[3]), 1.0);
        catchPokemons();
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
                               double longtitude,
                               double altitude) {
        api.setLocation(latitude, longtitude, altitude);
    }

    private static void catchPokemons() {
        try {
            //Wait until map is updated for the current location
            api.getMap().awaitUpdate();

            Set<CatchablePokemon> catchablePokemons = api.getMap().getMapObjects().getPokemon();
            System.out.println("Pokemon in area: " + catchablePokemons.size());

            Random random = new Random();
            PokeBank pokebank = api.getInventories().getPokebank();

            for (CatchablePokemon cp : catchablePokemons) {
                // You need to Encounter first.
                EncounterResult encResult = cp.encounterPokemon();
                // if encounter was successful, catch
                if (encResult.wasSuccessful()) {
                    System.out.println("Encountered: " + cp.getPokemonId());
                    CatchOptions options =
                            new CatchOptions(api).
                                    useRazzberry(true).
                                    withPokeballSelector(PokeballSelector.SMART);
                    List<Pokeball> useablePokeballs
                            = api.getInventories().getItemBag().getUseablePokeballs();

                    double probability = cp.getCaptureProbability();
                    if (useablePokeballs.size() > 0) {
                        //Select pokeball with smart selector to print what pokeball is used
                        Pokeball pokeball = PokeballSelector.SMART.select(useablePokeballs, probability);
                        System.out.println("Attempting to catch: " + cp.getPokemonId() + " with " + pokeball
                                + " (" + probability + ")");
                        //Throw pokeballs until capture or flee
                        while (!cp.isDespawned()) {
                            //Wait between Pokeball throws
                            Thread.sleep(500 + random.nextInt(1000));
                            CatchResult result = cp.catchPokemon(options);
                            System.out.println("Threw ball: " + result.getStatus());
                            if (result.getStatus() == CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus.CATCH_SUCCESS) {
                                //Print pokemon stats
                                Pokemon pokemon = pokebank.getPokemonById(result.getCapturedPokemonId());
                                double iv = pokemon.getIvInPercentage();
                                int number = pokemon.getPokemonId().getNumber();
                                String name = PokeDictionary.getDisplayName(number, Locale.ENGLISH);
                                System.out.println("====" + name + "====");
                                System.out.println("CP: " + pokemon.getCp());
                                System.out.println("IV: " + iv + "%");
                                System.out.println("Height: " + pokemon.getHeightM() + "m");
                                System.out.println("Weight: " + pokemon.getWeightKg() + "kg");
                                System.out.println("Move 1: " + pokemon.getMove1());
                                System.out.println("Move 2: " + pokemon.getMove2());
                                //Rename the pokemon to <Name> IV%
                                pokemon.renamePokemon(name + " " + iv + "%");
                                //Set pokemon with IV above 90% as favorite
                                if (iv > 90) {
                                    pokemon.setFavoritePokemon(true);
                                }
                            }
                        }
                        //Wait for animation before catching next pokemon
                        Thread.sleep(3000 + random.nextInt(1000));
                    } else {
                        System.out.println("Skipping Pokemon, we have no Pokeballs!");
                    }
                } else {
                    System.out.println("Encounter failed. " + encResult.getStatus());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
