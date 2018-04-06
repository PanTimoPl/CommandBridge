package pl.kacperduras.commandbridge.bungee;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.apache.commons.lang3.Validate;
import pl.kacperduras.commandbridge.shaded.CommandBridgeAPI;
import pl.kacperduras.commandbridge.shaded.CommandBridgeConfig;
import pl.kacperduras.commandbridge.shaded.util.ConfigUtils;

public final class CommandBridgeBungee extends Plugin implements CommandBridgeAPI {

  private CommandBridgeConfig config;

  @Override
  public void onLoad() {
    this.config = ConfigUtils.loadConfig(
        new File(this.getDataFolder(), "config.yml"), CommandBridgeConfig.class);
  }

  @Override
  public void onEnable() {
    if (this.config.getChannels().getBukkit().isEnabled()) {
      this.getProxy().registerChannel(this.config.getChannels().getBukkit().getName());
    }

    if (this.config.getChannels().getBungeeCord().isEnabled()) {
      this.getProxy().registerChannel(this.config.getChannels().getBungeeCord().getName());
    }
  }

  @Override
  public void onDisable() {
    if (this.config.getChannels().getBukkit().isEnabled()) {
      this.getProxy().unregisterChannel(this.config.getChannels().getBukkit().getName());
    }

    if (this.config.getChannels().getBungeeCord().isEnabled()) {
      this.getProxy().unregisterChannel(this.config.getChannels().getBungeeCord().getName());
    }
  }

  @Override
  public void bukkit(String nickname, String server, String command) {
    Validate.notNull(nickname);
    Validate.notNull(server);
    Validate.notNull(command);

    ProxiedPlayer player = this.getProxy().getPlayer(nickname);
    if (player == null) {
      return;
    }

    ByteArrayOutputStream outputByteArray = new ByteArrayOutputStream();
    DataOutputStream output = new DataOutputStream(outputByteArray);
    try {
      output.writeUTF(nickname);
      output.writeUTF(command);
    } catch (IOException ex) {
      player.sendMessage(ChatColor.RED + "Fatal error: " + ex.getMessage());
      player.sendMessage(ChatColor.RED + "More informations is available in the console.");

      ex.printStackTrace();
    }

    ServerInfo target = this.getProxy().getServerInfo(server);
    if (target == null) {
      return;
    }

    target.sendData(this.config.getChannels().getBukkit().getName(), outputByteArray.toByteArray());
  }

  @Override
  public void bungee(String nickname, String command) {
    throw new RuntimeException("This platform doesn't support sending messages to BungeeCord!");
  }

  @Override
  public boolean isBungee() {
    return true;
  }

}