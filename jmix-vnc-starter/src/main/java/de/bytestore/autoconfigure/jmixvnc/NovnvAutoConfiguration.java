package de.bytestore.autoconfigure.jmixvnc;

import de.bytestore.jmixvnc.NovnvConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({NovnvConfiguration.class})
public class NovnvAutoConfiguration {
}

