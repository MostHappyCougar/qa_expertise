package ConfigReaders;

import abs.AConfigDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import ObjectsDeserialization.SDeserializedConfig;

import java.io.File;
import java.io.IOException;

/**
 * Реализация для десериализатора конфига
 */
public class YAMLConfigDeserializer extends AConfigDeserializer
{
    private final String configPath;
    private SDeserializedConfig deserializedConfig;
    public SDeserializedConfig getDeserializedConfig()
    {
        return this.deserializedConfig;
    }

    /**
     * Конструктор
     * @param configPath Путь до конфига
     */
    public YAMLConfigDeserializer(String configPath)
    {
        this.configPath = configPath;
    }

    @Override
    public void deserializeConfig() throws IOException
    {
        File config = new File(String.format("src/main/resources/%s.yaml", this.configPath));
        ObjectMapper yamlMap = new YAMLMapper();
        this.deserializedConfig = yamlMap.readValue(config, SDeserializedConfig.class);
    }
}
