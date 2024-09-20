import ConfigReaders.YAMLConfigDeserializer;
import DataExporters.extDataExporter;
import DataProcessors.stdDataProcessor;
import org.json.simple.parser.ParseException;
import ObjectsDeserialization.SDeserializedConfig;

import java.io.IOException;
import java.net.URISyntaxException;

public class main
{
    public static void main(String[] args) throws IOException, URISyntaxException, ParseException
    {
        YAMLConfigDeserializer configReader = new YAMLConfigDeserializer("config");
        configReader.deserializeConfig();
        SDeserializedConfig deserializedConfig = configReader.getDeserializedConfig();

        stdDataProcessor processor = new stdDataProcessor(Integer.valueOf(args[0]), deserializedConfig.getTmsData());
        extDataExporter exporter = new extDataExporter(deserializedConfig.getRelevantMembers(), deserializedConfig.getTmsData().get("address"), deserializedConfig.getTmsData().get("projectId"));
        exporter.exportData(processor.getSortedFunctionalStats());
    }
}
