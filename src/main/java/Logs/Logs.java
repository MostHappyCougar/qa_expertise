package Logs;

import org.apache.logging.log4j.Logger;
import qa.common.logs.Console;

public class Logs {
    public static Logger log = new Console().logger;
}

