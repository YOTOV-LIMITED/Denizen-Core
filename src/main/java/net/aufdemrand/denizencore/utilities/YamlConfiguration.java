package net.aufdemrand.denizencore.utilities;

import net.aufdemrand.denizencore.utilities.debugging.dB;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

/**
 * Represents a YAML file.
 */
public class YamlConfiguration {

    public static YamlConfiguration load(String data) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowUnicode(true);
        Yaml yaml = new Yaml(options);
        Object obj = yaml.load(data);
        YamlConfiguration config = new YamlConfiguration();
        if (obj == null) {
            try {
                throw new Exception("Null YAML container - failed to load or empty?: '" + data + "'");
            }
            catch (Exception e) {
                dB.echoError(e);
            }
            return null;
        }
        else if (obj instanceof String) {
            config.contents = new HashMap<String, Object>();
            config.contents.put(null, obj);
        }
        else if (obj instanceof Map) {
            config.contents = (Map<String, Object>)obj;
        }
        else {
            dB.echoError("Invalid YAML object type: " + obj.toString() + " is " + obj.getClass().getSimpleName());
            return null;
        }
        patchNonsense(config.contents);
        return config;
    }

    Map<String, Object> contents = null;

    /**
     * Don't ask why, I can't explain this Java BS.
     */
    private static void patchNonsense(Map<String, Object> objs) {
        for (Object o: new HashSet<Object>(objs.keySet())) {
            if (!(o instanceof String)) {
                objs.put(o.toString(), objs.get(o));
                objs.remove(o);
            }
        }
        for (Map.Entry<String, Object> str: objs.entrySet()) {
            if (str.getValue() instanceof Map) {
                patchNonsense((Map<String, Object>)str.getValue());
            }
        }
    }

    public Set<String> getKeys(boolean deep) {
        if (!deep) {
            return contents.keySet();
        }
        else {
            return getKeysDeep(contents);
        }
    }

    private Set<String> getKeysDeep(Map<String, Object> objs) {
        Set<String> strings = objs.keySet();
        for (Map.Entry<String, Object> str: objs.entrySet()) {
            if (str.getValue() instanceof Map) {
                strings.addAll(getKeysDeep((Map<String, Object>)str.getValue()));
            }
        }
        return strings;
    }

    public String saveToString() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowUnicode(true);
        Yaml yaml = new Yaml(options);
        return yaml.dump(contents);
    }

    public Object get(String path) {
        List<String> parts = CoreUtilities.Split(path, '.');
        Map<String, Object> portion = contents;
        for (int i = 0; i < parts.size(); i++) {
            Object oPortion = portion.get(parts.get(i));
            if (oPortion == null) {
                return null;
            }
            else if (parts.size() == i + 1) {
                return oPortion;
            }
            else if (oPortion instanceof Map) {
                portion = (Map<String, Object>) oPortion;
            }
            else {
                return null;
            }
        }
        return null;
    }

    public void set(String path, Object o) {
        List<String> parts = CoreUtilities.Split(path, '.');
        Map<String, Object> portion = contents;
        for (int i = 0; i < parts.size(); i++) {
            Object oPortion = portion.get(parts.get(i));
            if (parts.size() == i + 1) {
                portion.put(parts.get(i), o);
                return;
            }
            else if (oPortion == null) {
                Map<String, Object> map = new HashMap<String, Object>();
                portion.put(parts.get(i), map);
                portion = map;
            }
            else if (oPortion instanceof Map) {
                portion = (Map<String, Object>) oPortion;
            }
            else {
                Map<String, Object> map = new HashMap<String, Object>();
                portion.put(parts.get(i), map);
                portion = map;
            }
        }
        dB.echoError("Failed to set somehow?");
    }

    public boolean contains(String path) {
        return get(path) != null;
    }

    public String getString(String path) {
        Object o = get(path);
        if (o == null)
            return null;
        return o.toString();
    }

    public String getString(String path, String def) {
        Object o = get(path);
        if (o == null)
            return def;
        return o.toString();
    }

    public List<String> getStringList(String path) {
        Object o = get(path);
        if (o == null)
            return null;
        return (List<String>)o;
    }

    public YamlConfiguration getConfigurationSection(String path) {
        List<String> parts = CoreUtilities.Split(path, '.');
        Map<String, Object> portion = contents;
        for (int i = 0; i < parts.size(); i++) {
            Object oPortion = portion.get(parts.get(i));
            if (oPortion == null) {
                return null;
            }
            else if (parts.size() == i + 1) {
                YamlConfiguration configuration = new YamlConfiguration();
                configuration.contents = (Map<String, Object>) oPortion;
                return configuration;
            }
            else if (oPortion instanceof Map) {
                portion = (Map<String, Object>) oPortion;
            }
            else {
                return null;
            }
        }
        return null;
    }
}
