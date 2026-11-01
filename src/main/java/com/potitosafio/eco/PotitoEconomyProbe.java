package com.potitosafio.eco;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.lang.reflect.Method;
import java.util.UUID;

/** Solo lectura: no registra un proveedor de Vault ni modifica saldos. */
public final class PotitoEconomyProbe extends JavaPlugin {
    @Override public void onEnable() {
        getLogger().info("PotitoEconomy PROBE: solo lectura; no se modifican saldos.");
    }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("potitoeco.admin")) {
            sender.sendMessage("§cSin permiso."); return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("estado")) {
            try {
                Class.forName("net.ruleta.v", false, getClass().getClassLoader());
                sender.sendMessage("§aClase bancaria encontrada. Usa /potitoeco saldo para comprobar lectura.");
            } catch (Throwable e) {
                sender.sendMessage("§cNo se puede acceder a net.ruleta.v: " + e.getClass().getSimpleName());
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("saldo")) {
            if (!(sender instanceof Player player)) { sender.sendMessage("Solo jugadores."); return true; }
            try {
                Object server = Bukkit.getServer();
                Method getServer = server.getClass().getMethod("getServer");
                Object minecraftServer = getServer.invoke(server);
                Class<?> bankClass = Class.forName("net.ruleta.v");
                Method factory = null;
                for (Method candidate : bankClass.getMethods()) {
                    if (!candidate.getName().equals("a") || !java.lang.reflect.Modifier.isStatic(candidate.getModifiers()) || candidate.getParameterCount() != 1) continue;
                    if (candidate.getReturnType() == bankClass && candidate.getParameterTypes()[0].isInstance(minecraftServer)) {
                        factory = candidate;
                        break;
                    }
                }
                if (factory == null) throw new NoSuchMethodException("No se encontro fabrica bancaria compatible con MinecraftServer");
                Object bank = factory.invoke(null, minecraftServer);
                Method balance = bankClass.getMethod("a", UUID.class);
                long amount = (Long) balance.invoke(bank, player.getUniqueId());
                sender.sendMessage("§bSaldo banco DEDsafio: §f" + amount);
            } catch (Throwable e) {
                sender.sendMessage("§cLectura no disponible: " + e.getClass().getSimpleName());
                getLogger().warning("Fallo de prueba bancaria: " + e);
            }
            return true;
        }
        sender.sendMessage("§eUsa /potitoeco estado o /potitoeco saldo"); return true;
    }
}
