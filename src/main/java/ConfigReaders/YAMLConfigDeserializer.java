package ConfigReaders;

import abs.AConfigDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import ObjectsDeserialization.SDeserializedConfig;

import java.io.File;
import java.io.IOException;

public class YAMLConfigDeserializer extends AConfigDeserializer
{
    private final String configName;
    private SDeserializedConfig deserializedConfig;
    public SDeserializedConfig getDeserializedConfig()
    {
        return this.deserializedConfig;
    }

    public YAMLConfigDeserializer(String configPath)
    {
        this.configName = configPath;
    }

    @Override
    public void deserializeConfig() throws IOException
    {
        File config = new File(String.format("src/main/resources/%s.yaml", this.configName));
        ObjectMapper yamlMap = new YAMLMapper();
        this.deserializedConfig = yamlMap.readValue(config, SDeserializedConfig.class);
    }
}
