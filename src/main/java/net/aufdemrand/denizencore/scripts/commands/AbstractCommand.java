package net.aufdemrand.denizencore.scripts.commands;

import net.aufdemrand.denizencore.DenizenCore;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.interfaces.RegistrationableInstance;
import net.aufdemrand.denizencore.scripts.ScriptEntry;

// TODO: merge with BaseAbstractCommand
public abstract class AbstractCommand implements RegistrationableInstance {

    /**
     * Contains required options for a Command in a single class for the
     * ability to add optional options in the future.
     * <p/>
     * See {@link #withOptions} for information on using CommandOptions with this command.
     */
    public class CommandOptions {
        public String USAGE_HINT;
        public int REQUIRED_ARGS;

        public CommandOptions(String usageHint, int numberOfRequiredArgs) {
            this.USAGE_HINT = usageHint;
            this.REQUIRED_ARGS = numberOfRequiredArgs;
        }
    }

    private boolean braced = false;

    public void setBraced() {
        braced = true;
    }

    public boolean isBraced() {
        return braced;
    }

    private boolean preparseArgs = true;

    public void setParseArgs(boolean parse) {
        preparseArgs = parse;
    }

    public boolean shouldPreParse() {
        return preparseArgs;
    }

    @Override
    public AbstractCommand activate() {
        return this;
    }

    @Override
    public AbstractCommand as(String commandName) {
        // Register command with Registry with a Name
        name = commandName.toUpperCase();
        DenizenCore.getCommandRegistry().register(this.name, this);
        onEnable();
        return this;
    }

    protected String name;

    public CommandOptions commandOptions;

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the {@link CommandOptions} specified at startup.
     *
     * @return commandOptions
     */
    public CommandOptions getOptions() {
        return commandOptions;
    }

    /**
     * Returns USAGE_HINT specified in the {@link CommandOptions}, if specified.
     *
     * @return USAGE_HINT if specified, otherwise "No usage defined! See documentation for more information!"
     */
    public String getUsageHint() {
        return !commandOptions.USAGE_HINT.equals("") ? commandOptions.USAGE_HINT : "No usage defined! See documentation for more information!";
    }

    /**
     * Part of the Plugin disable sequence.
     * <p/>
     * Can be '@Override'n by a Command which requires a method when bukkit sends a
     * onDisable() to Denizen. (ie. Server shuts down or restarts)
     */
    public void onDisable() {

    }

    /**
     * Part of the Plugin enable sequence. This is called when the command is
     * instanced by the CommandRegistry, which is generally on a server startup.
     * <p/>
     * Can be '@Override'n by a Command which requires a method when starting, such
     * as registering as a Bukkit Listener.
     */
    public void onEnable() {

    }

    /**
     * Creates a new {@link CommandOptions} for this command.
     *
     * @param usageHint            A String representation of the suggested usage format of this command.
     *                             Typically []'s represent required arguments and ()'s represent optional arguments.
     *                             Example from SWITCH command: [LOCATION:x,y,z,world] (STATE:ON|OFF|TOGGLE) (DURATION:#)
     * @param numberOfRequiredArgs The minimum number of required arguments needed to ensure proper functionality. The
     *                             Executer will not parseArgs() for this command if this number is not met.
     * @return The newly created CommandOptions object for the possibility of setting other
     * criteria, though currently none exists.
     */
    public CommandOptions withOptions(String usageHint, int numberOfRequiredArgs) {
        this.commandOptions = new CommandOptions(usageHint, numberOfRequiredArgs);
        return commandOptions;
    }

    public abstract void execute(ScriptEntry scriptEntry) throws CommandExecutionException;

    /**
     * Called by the CommandExecuter before the execute() method is called. Arguments
     * should be iterated through and checked before continuing to execute(). Note that
     * PLAYER:<player> and NPC:<npc> arguments are parsed automatically by the Executer
     * and should not be handled by this Command otherwise. Their output is stored in the
     * attached {@link ScriptEntry} and can be retrieved with ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer(),
     * scriptEntry.getOfflinePlayer() (if the player specified is not online), and
     * ((BukkitScriptEntryData)scriptEntry.entryData).getNPC(). Remember that any of these have a possibility of being null
     * and should be handled accordingly if required by this Command.
     *
     * @param scriptEntry The {@link ScriptEntry}, which contains run-time context that may
     *                    be utilized by this Command.
     * @throws InvalidArgumentsException Will halt execution of this Command and hint usage to the console to avoid
     *                                   unwanted behavior due to missing information.
     */
    public abstract void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException;

}
