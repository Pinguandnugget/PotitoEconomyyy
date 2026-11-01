package com.potitosafio.eco;

import java.lang.reflect.*;
import java.util.*;
import net.milkbowl.vault.economy.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatColor;

public final class PotitoEconomyProbe extends JavaPlugin {
    private Bank bank;
    private PotitoVault provider;
    private boolean transactions;
    @Override public void onEnable() {
        saveDefaultConfig();
        transactions = getConfig().getBoolean("enable-transactions", false);
        bank = new Bank();
        try {
            bank.connect();
            provider = new PotitoVault();
            getServer().getServicesManager().register(Economy.class, provider, this, ServicePriority.Highest);
            getLogger().info("Proveedor Vault registrado. Transacciones: " + transactions);
        } catch (Exception e) {
            getLogger().severe("No se pudo conectar con banco DEDsafio: " + e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    @Override public void onDisable() { getServer().getServicesManager().unregisterAll(this); }
    private final class Bank {
        private Object instance;
        private Method read, write;
        void connect() throws Exception {
            Object craft = Bukkit.getServer();
            Object server = craft.getClass().getMethod("getServer").invoke(craft);
            Class<?> type = Class.forName("net.ruleta.v");
            Method factory = null;
            for (Method m : type.getMethods()) {
                if (m.getName().equals("a") && Modifier.isStatic(m.getModifiers())
                        && m.getParameterCount()==1 && m.getReturnType()==type
                        && m.getParameterTypes()[0].isInstance(server)) { factory=m; break; }
            }
            if (factory==null) throw new NoSuchMethodException("Banco.a(MinecraftServer)");
            instance=factory.invoke(null,server);
            if (instance==null) throw new IllegalStateException("Banco no disponible");
            read=type.getMethod("a",UUID.class);
            write=type.getMethod("a",UUID.class,long.class);
        }
        long balance(UUID id) {
            if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Operacion bancaria fuera del hilo principal");
            try { return (Long)read.invoke(instance,id); }
            catch (Exception e) { throw new IllegalStateException("Fallo leyendo banco",e); }
        }
        void set(UUID id,long value) {
            if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Operacion bancaria fuera del hilo principal");
            try { write.invoke(instance,id,value); }
            catch (Exception e) { throw new IllegalStateException("Fallo actualizando banco",e); }
        }
    }
    private static final long MAX_SAFE = 9007199254740991L;
    private static long amount(double value) {
        if (!Double.isFinite(value) || value<0 || value>MAX_SAFE || Math.rint(value)!=value)
            throw new IllegalArgumentException("Solo cantidades enteras positivas de deditas");
        return (long)value;
    }
    private static UUID id(String name) {
        Player online=Bukkit.getPlayerExact(name);
        if (online!=null) return online.getUniqueId();
        @SuppressWarnings("deprecation") OfflinePlayer offline=Bukkit.getOfflinePlayer(name);
        if (!offline.hasPlayedBefore()) throw new IllegalArgumentException("Jugador desconocido: "+name);
        return offline.getUniqueId();
    }
    private EconomyResponse result(double amount,double balance,EconomyResponse.ResponseType type,String error) {
        return new EconomyResponse(amount,balance,type,error);
    }
    private final class PotitoVault extends AbstractEconomy {
        public boolean isEnabled(){return isEnabledPlugin();}
        private boolean isEnabledPlugin(){return PotitoEconomyProbe.this.isEnabled();}
        public String getName(){return "PotitoEconomy";}
        public boolean hasBankSupport(){return false;}
        public int fractionalDigits(){return 0;}
        public String format(double n){return String.format(java.util.Locale.US,"%.0f Deditas",n);}
        public String currencyNamePlural(){return "Deditas";}
        public String currencyNameSingular(){return "Dedita";}
        public boolean hasAccount(String player){try {id(player);return true;}catch(Exception e){return false;}}
        public boolean hasAccount(String player,String world){return hasAccount(player);}
        public double getBalance(String player){return bank.balance(id(player));}
        public double getBalance(String player,String world){return getBalance(player);}
        public boolean has(String player,double n){try{return bank.balance(id(player))>=amount(n);}catch(Exception e){return false;}}
        public boolean has(String player,String world,double n){return has(player,n);}
        public EconomyResponse withdrawPlayer(String player,double n){return change(id(player),n,false);}
        public EconomyResponse withdrawPlayer(String player,String world,double n){return withdrawPlayer(player,n);}
        public EconomyResponse depositPlayer(String player,double n){return change(id(player),n,true);}
        public EconomyResponse depositPlayer(String player,String world,double n){return depositPlayer(player,n);}
        @Override public EconomyResponse withdrawPlayer(OfflinePlayer player,double n){return change(player.getUniqueId(),n,false);}
        @Override public EconomyResponse depositPlayer(OfflinePlayer player,double n){return change(player.getUniqueId(),n,true);}
        @Override public double getBalance(OfflinePlayer player){return bank.balance(player.getUniqueId());}
        @Override public boolean has(OfflinePlayer player,double n){try{return bank.balance(player.getUniqueId())>=amount(n);}catch(Exception e){return false;}}
        private synchronized EconomyResponse change(UUID player,double raw,boolean deposit) {
            double before=0;
            try {
                long n=amount(raw);
                before=bank.balance(player);
                if (!transactions) return result(0,before,EconomyResponse.ResponseType.FAILURE,"Transacciones desactivadas en config.yml");
                if (!deposit && before<n) return result(0,before,EconomyResponse.ResponseType.FAILURE,"Saldo insuficiente");
                if (deposit && before>MAX_SAFE-n) return result(0,before,EconomyResponse.ResponseType.FAILURE,"Limite de saldo");
                long after=deposit?(long)before+n:(long)before-n;
                bank.set(player,after);
                return result(n,after,EconomyResponse.ResponseType.SUCCESS,null);
            } catch(Exception e) {
                getLogger().warning("Transaccion rechazada: "+e);
                return result(0,before,EconomyResponse.ResponseType.FAILURE,e.getMessage());
            }
        }
        public EconomyResponse createBank(String n,String p){return unsupported();}
        public EconomyResponse deleteBank(String n){return unsupported();}
        public EconomyResponse bankBalance(String n){return unsupported();}
        public EconomyResponse bankHas(String n,double a){return unsupported();}
        public EconomyResponse bankWithdraw(String n,double a){return unsupported();}
        public EconomyResponse bankDeposit(String n,double a){return unsupported();}
        public EconomyResponse isBankOwner(String n,String p){return unsupported();}
        public EconomyResponse isBankMember(String n,String p){return unsupported();}
        public List<String> getBanks(){return Collections.emptyList();}
        private EconomyResponse unsupported(){return result(0,0,EconomyResponse.ResponseType.NOT_IMPLEMENTED,"No soportado");}
        public boolean createPlayerAccount(String p){return hasAccount(p);}
        public boolean createPlayerAccount(String p,String world){return hasAccount(p);}
    }
    private static void coin(TextComponent parent,long n,char glyph) {
        if (parent.getExtra()!=null && !parent.getExtra().isEmpty()) parent.addExtra(new TextComponent("  "));
        TextComponent number=new TextComponent(n+" ");
        number.setColor(ChatColor.WHITE);parent.addExtra(number);
        TextComponent icon=new TextComponent(String.valueOf(glyph));
        icon.setFont("ruleta:monedas");icon.setColor(ChatColor.WHITE);parent.addExtra(icon);
    }
    private void show(Player p,long n,String prefix){
        TextComponent t=new TextComponent(prefix);t.setColor(ChatColor.AQUA);
        if(n>=10000)coin(t,n/10000,'r');
        if(n%10000>=100)coin(t,n%10000/100,'v');
        if(n%100>0||n==0)coin(t,n%100,'a');
        p.spigot().sendMessage(t);
    }
    @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
        if(args.length==0){sender.sendMessage("§bUsa /potitoeco saldo [jugador] o /potitoeco estado");return true;}
        if(args[0].equalsIgnoreCase("estado")){
            if(!sender.hasPermission("potitoeco.admin")){sender.sendMessage("§cSin permiso.");return true;}
            sender.sendMessage("§aBanco conectado. Vault: PotitoEconomy. Transacciones: "+(transactions?"§aactivadas":"§cdesactivadas"));
            return true;
        }
        if(args[0].equalsIgnoreCase("saldo")){
            if(args.length>1&&!sender.hasPermission("potitoeco.admin")){sender.sendMessage("§cSolo administradores pueden consultar saldos ajenos.");return true;}
            if(args.length==1&&!sender.hasPermission("potitoeco.saldo")){sender.sendMessage("§cSin permiso.");return true;}
            try{
                UUID target;
                if(args.length>1)target=id(args[1]);
                else if(sender instanceof Player p)target=p.getUniqueId();
                else {sender.sendMessage("§cDesde consola indica un jugador.");return true;}
                long balance=bank.balance(target);
                if(sender instanceof Player p)show(p,balance,args.length>1?"Saldo de "+args[1]+": ":"Saldo bancario: ");
                else sender.sendMessage("Saldo: "+balance);
            }catch(Exception e){sender.sendMessage("§cLectura no disponible: "+e.getMessage());getLogger().warning("Fallo banco: "+e);}
            return true;
        }
        sender.sendMessage("§eUsa /potitoeco saldo [jugador] o /potitoeco estado");
        return true;
    }
}
