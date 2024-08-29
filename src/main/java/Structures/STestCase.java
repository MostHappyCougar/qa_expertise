package Structures;

import java.util.ArrayList;
import java.util.Objects;

public class STestCase
{
    public STestCase(Integer caseId, String caseName)
    {
        this.caseId = caseId;
        this.caseName = caseName;
    }

    private final Integer caseId;
    public Integer getCaseId()
    {
        return this.caseId;
    }

    private final String caseName;
    public String getCaseName() {return this.caseName; }

    private String ownerName;
    public String getOwnerName()
    {
        return this.ownerName;
    }
    public void setOwnerName(String ownerName)
    {
        this.ownerName = ownerName;
    }

    private ArrayList<String> executorsList;
    public ArrayList<String> getExecutorsList()
    {
        return this.executorsList;
    }
    public void setExecutorsList(ArrayList<String> executorsList)
    {
        this.executorsList = executorsList;
    }

    private String caseStatus;
    public void setCaseStatus(String caseStatus) {this.caseStatus = caseStatus;}
    public String getCaseStatus() {return this.caseStatus;}

    @Override
    public int hashCode()
    {
        return Objects.hash(this.caseId);
    }
}
