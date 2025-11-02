package ObjectsDeserialization;

import java.util.HashMap;
import java.util.HashSet;

public class SDeserializedConfig
{
    private HashSet<String> relevantMembers;
    public void setRelevantMembers(HashSet<String> relevantMembers)
    {
        this.relevantMembers = relevantMembers;
    }
    public HashSet<String> getRelevantMembers()
    {
        return this.relevantMembers;
    }
    
    private Integer expertisePercentageThreshold;
    public void setExpertisePercentageThreshold(Integer expertisePercentageThreshold)
    {
        this.expertisePercentageThreshold = expertisePercentageThreshold;
    }
    public Integer getExpertisePercentageThreshold()
    {
    	/**Получить из десериализованного конфига значение минимального порога экспертности**/
        return this.expertisePercentageThreshold;
    }

    private HashMap<String, String> tmsData;
    public void setTmsData(HashMap<String, String> tmsData)
    {
        this.tmsData = tmsData;
    }
    public HashMap<String, String> getTmsData()
    {
        return this.tmsData;
    }
}
