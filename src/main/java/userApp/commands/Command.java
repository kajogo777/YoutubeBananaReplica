package userApp.commands;

import java.util.HashMap;

public interface Command {
    public abstract void execute (HashMap<String, Object> props);

}
