import ConfigReaders.YAMLConfigReader;
import DataExporters.extDataExporter;
import DataProcessors.stdDataProcessor;
import org.json.simple.parser.ParseException;
import deserializableObjects.SConfig;

import java.io.IOException;
import java.net.URISyntaxException;

public class main
{
    public static void main(String[] args) throws IOException, URISyntaxException, ParseException
    {
        YAMLConfigReader configReader = new YAMLConfigReader("config");
        configReader.deserializeConfig();
        SConfig deserializedConfig = configReader.getDeserializedConfig();

        stdDataProcessor processor = new stdDataProcessor(Integer.valueOf(args[0]), deserializedConfig.getTmsData());
        extDataExporter exporter = new extDataExporter(deserializedConfig.getRelevantMembers(), deserializedConfig.getTmsData().get("address"), deserializedConfig.getTmsData().get("projectId"));
        exporter.exportData(processor.getSortedFunctionalStats());
    }
}
