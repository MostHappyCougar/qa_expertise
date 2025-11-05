package abs;

import java.io.IOException;

/**
 * Абстракция для десериализатора конфига
 */
public abstract class AConfigDeserializer
{
    /**
     * Метод для десериализации конфига
     * @throws IOException
     */
    public abstract void deserializeConfig() throws IOException;
}
