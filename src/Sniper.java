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
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by jsh3571 on 12/02/2017.
 */
public class Sniper {
    private final PokemonGo go;
    private final Locator locator;
    private final Location home;

    public Sniper(PokemonGo go, Locator locator, Location home) {
        // Assign the api, go.
        this.go = go;

        // Assign the locator, locator.
        this.locator = locator;

        // Home.
        this.home = home;
    }

    public void snipe(PokemonIdOuterClass.PokemonId pokemonId,
                      Location destination) throws Exception {
        // Flying to the destination to catch desired pokemon.
        locator.locate(destination);

        Set<CatchablePokemon> catchablePokemons =
                go.getMap().getMapObjects().getPokemon();

        if (catchablePokemons.size() == 0) {
            locator.locate(home);
        } else {
            snipe(pokemonId, catchablePokemons);
        }
    }

    private void snipe(PokemonIdOuterClass.PokemonId pokemonId,
                       Set<CatchablePokemon> catchablePokemons) {

        PokeBank pokebank = go.getInventories().getPokebank();

        try {
            for (CatchablePokemon cp : catchablePokemons) {
                System.out.println("Pokemon seen: " + cp.getPokemonId());
                if (cp.getPokemonId() != pokemonId)
                    continue;

                EncounterResult encResult = cp.encounterPokemon();
                if (encResult.wasSuccessful()) {
                    System.out.println("Encountered: " + cp.getPokemonId());

                    List<Pokeball> useablePokeballs
                            = go.getInventories().getItemBag().getUseablePokeballs();
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
                        // locate(home);

                        while (!cp.isDespawned()) {
                            // Wait between Pokeball throws.
                            TimeUnit.SECONDS.sleep(3);

                            // Catching pokemon.
                            CatchResult result = cp.catchPokemon(getCatchOptions());
                            System.out.println("Threw ball at " + pokemonId + ": " + result.getStatus());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CatchOptions getCatchOptions() {
        return new CatchOptions(go).
                useRazzberry(true).
                withPokeballSelector(PokeballSelector.SMART);
    }
}
