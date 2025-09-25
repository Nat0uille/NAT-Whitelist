package fr.Nat0uille.NATWhitelist.API;

import fr.Nat0uille.NATWhitelist.Main;
import fr.Nat0uille.NATWhitelist.Listeners.WhitelistListener;
import java.sql.SQLException;
import java.util.UUID;

public class NATWhitelistImpl implements NATWhitelistAPI {

    private final Main main;
    private final WhitelistListener whitelistListener;

    public NATWhitelistImpl(Main main) {
        this.main = main;
        this.whitelistListener = main.getWhitelistListener();
    }

    @Override
    public boolean add(UUID uuid, String playerName) throws SQLException {
        return whitelistListener.add(uuid, playerName);
    }
}
