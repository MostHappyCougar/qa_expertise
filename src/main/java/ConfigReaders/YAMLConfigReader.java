package ConfigReaders;

import abs.AConfigReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import deserializableObjects.SConfig;

import java.io.File;
import java.io.IOException;

public class YAMLConfigReader extends AConfigReader
{
    private final String configName;
    private SConfig deserializedConfig;
    public SConfig getDeserializedConfig()
    {
        return this.deserializedConfig;
    }

    public YAMLConfigReader(String configPath)
    {
        this.configName = configPath;
    }

    @Override
    public void deserializeConfig() throws IOException
    {
        File config = new File(String.format("src/main/resources/%s.yaml", this.configName));
        ObjectMapper yamlMap = new YAMLMapper();
        this.deserializedConfig = yamlMap.readValue(config, SConfig.class);
    }
}
