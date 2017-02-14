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

    /**
     * A constructor of Sniper which performs catching targeted pokemon.
     * @param go api(i.e. player)
     * @param locator used to locate player
     * @param home represents home destination
     */
    public Sniper(PokemonGo go, Locator locator, Location home) {
        // Assign the api, go.
        this.go = go;

        // Assign the locator, locator.
        this.locator = locator;

        // Assign default location, home.
        this.home = home;
    }

    /**
     * Catch target pokemon given by its id and destination.
     * @param pokemonId represents pokemon name
     * @param destination location where the pokemon currently appears
     * @throws Exception
     */
    public void snipe(PokemonIdOuterClass.PokemonId pokemonId,
                      Location destination) throws Exception {
        // Flying to the destination to search desired pokemon.
        locator.locate(destination);

        // Get catchable pokemons at updated location.
        Set<CatchablePokemon> catchablePokemons =
                go.getMap().getMapObjects().getPokemon();

        // If there is no desired pokemon around the player, come back home.
        if (!isExistIn(pokemonId, catchablePokemons)) {
            System.out.println("Target pokemon does not exist within range.");
            locator.locate(home);
        }

        // Otherwise, snipe the desired pokemon from catchable pokemons.
        else {
            System.out.println("Found target pokemon within range, now snipe!");
            snipe(pokemonId, catchablePokemons);
        }
    }

    private boolean isExistIn(PokemonIdOuterClass.PokemonId pokemonId,
                              Set<CatchablePokemon> catchablePokemons) {

        for (CatchablePokemon pokemon : catchablePokemons)
            if (pokemon.getPokemonId() == pokemonId)
                return true;

        return false;
    }

    private void snipe(PokemonIdOuterClass.PokemonId pokemonId,
                       Set<CatchablePokemon> catchablePokemons) {
        try {
            // From the catchable pokemons, find target pokemon.
            for (CatchablePokemon cp : catchablePokemons) {
                System.out.println("Pokemon seen: " + cp.getPokemonId());

                // If the pokemon seen is not what we want, try next pokemon.
                if (cp.getPokemonId() != pokemonId)
                    continue;

                System.out.println("Found the desired pokemon!!");

                // Now encountering pokemon, meaning click(tab) on the pokemon.
                EncounterResult encResult = cp.encounterPokemon();
                if (encResult.wasSuccessful()) {

                    // Getting pokeballs and probability.
                    List<Pokeball> usablePokeballs = getUsablePokeballs();
                    double probability = cp.getCaptureProbability();

                    if (usablePokeballs.size() > 0) {
                        Pokeball pokeball =
                                PokeballSelector.SMART.select(
                                        usablePokeballs, probability);

                        System.out.println("Attempting to catch: "
                                + cp.getPokemonId() + "(" + probability + ") " +
                                "with " + pokeball);

                        // Do 'pull catch' by flying back home before catch.
                        System.out.println("Flying back home to" +
                                " avoid softban..");
                        locator.locate(home);

                        // Catch the pokemon while spawned.
                        catchPokemon(cp);

                        // Waiting for animation before complete catch action.
                        TimeUnit.SECONDS.sleep(5);
                    } else {
                        System.out.println("No more pokeballs.");
                    }
                } else {
                    System.out.println("Encounter failed. " +
                            encResult.getStatus());
                }
            }
        } catch (Exception e) {
            System.out.println("Exception occured!!");
            System.out.println("Flying back home to avoid softban.");
            try {
                locator.locate(home);
            } catch (Exception _e) {
                _e.printStackTrace();
            }

            e.printStackTrace();
        }
    }

    private List<Pokeball> getUsablePokeballs() {
        return go.getInventories().getItemBag().getUseablePokeballs();
    }

    private void catchPokemon(CatchablePokemon catchablePokemon)
            throws Exception {

        while (!catchablePokemon.isDespawned()) {
            // Wait between Pokeball throws.
            TimeUnit.SECONDS.sleep(3);

            // Catching pokemon.
            CatchResult result =
                    catchablePokemon.catchPokemon(getCatchOptions());

            System.out.println("Threw ball at " +
                    catchablePokemon.getPokemonId() + ": " +
                    result.getStatus());

            if (isCaptured(result.getStatus())) {

                // Getting pokebank which contains current pokemons we have.
                PokeBank pokebank = go.getInventories().getPokebank();

                // Obtaining Pokemon object of the captured pokemon.
                Pokemon pokemon =
                        pokebank.getPokemonById(
                                result.getCapturedPokemonId());

                // Using the object above, print stats of the pokemon.
                printCapturedPokemon(pokemon);
            }
        }
    }

    private CatchOptions getCatchOptions() {
        return new CatchOptions(go).
                useRazzberry(true).
                withPokeballSelector(PokeballSelector.SMART);
    }

    private boolean isCaptured(CatchPokemonResponseOuterClass.
                                       CatchPokemonResponse.
                                       CatchStatus catchStatus) {
        return catchStatus ==
                CatchPokemonResponseOuterClass.
                        CatchPokemonResponse.CatchStatus.CATCH_SUCCESS;
    }

    private void printCapturedPokemon(Pokemon pokemon) {
        String name =
                PokeDictionary.getDisplayName(
                        pokemon.getPokemonId().getNumber(), Locale.ENGLISH);

        System.out.println("====== " + name + " ======");
        System.out.println("CP: " + pokemon.getCp());
        System.out.println("IV: " + pokemon.getIvInPercentage() + "%");
        System.out.println("Move 1: " + pokemon.getMove1());
        System.out.println("Move 2: " + pokemon.getMove2());
        System.out.println("==========================");
    }
}
