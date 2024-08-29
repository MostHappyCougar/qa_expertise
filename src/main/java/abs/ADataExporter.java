package abs;

import Structures.SFunctionality;

import java.io.IOException;
import java.util.ArrayList;

public abstract class ADataExporter
{
    public abstract void exportData(ArrayList<SFunctionality> functionalList) throws IOException;
}
