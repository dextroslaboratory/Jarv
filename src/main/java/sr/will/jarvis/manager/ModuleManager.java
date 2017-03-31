package sr.will.jarvis.manager;

import net.dv8tion.jda.core.Permission;
import sr.will.jarvis.Jarvis;
import sr.will.jarvis.module.Module;
import sr.will.jarvis.module.admin.ModuleAdmin;
import sr.will.jarvis.module.chatbot.ModuleChatBot;
import sr.will.jarvis.module.levels.ModuleLevels;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModuleManager {
    private Jarvis jarvis;
    private HashMap<String, Module> modules = new HashMap<>();

    public ModuleManager(Jarvis jarvis) {
        this.jarvis = jarvis;
    }

    public void registerModule(String name, Module module) {
        modules.put(name, module);
    }

    public HashMap<String, Module> getModules() {
        return modules;
    }

    public Module getModule(String name) {
        return modules.get(name);
    }

    public void registerModules() {
        registerModule("admin", new ModuleAdmin(jarvis));
        registerModule("chatbot", new ModuleChatBot(jarvis));
        registerModule("levels", new ModuleLevels(jarvis));
    }

    public void enableModule(String guildId, String module) {
        jarvis.database.execute("INSERT INTO modules (guild, module) VALUES (?, ?);", guildId, module.toLowerCase());
    }

    public void disableModule(String guildId, String module) {
        jarvis.database.execute("DELETE FROM modules WHERE (guild = ? AND module = ?);", guildId, module.toLowerCase());
    }

    public boolean isModuleEnabled(String guildId, String module) {
        try {
            ResultSet result = jarvis.database.executeQuery("SELECT 1 FROM modules WHERE (guild = ? AND module = ?);", guildId, module.toLowerCase());
            return (result.first());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public ArrayList<Permission> getNeededPermissions() {
        ArrayList<Permission> permissions = new ArrayList<>();

        for (String name : modules.keySet()) {
            List<Permission> modulePerms = modules.get(name).getNeededPermissions();

            modulePerms.removeAll(permissions);
            permissions.addAll(modulePerms);
        }

        return permissions;
    }
}
