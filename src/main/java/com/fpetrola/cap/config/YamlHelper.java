package com.fpetrola.cap.config;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.fpetrola.cap.model.binders.implementations.*;

import java.io.*;
import java.net.URI;

public class YamlHelper {

    static <T> T deserializeModelFromURI(String uri, Class<T> class1) throws FileNotFoundException, YamlException {
        InputStream inputStream = new FileInputStream(new File(URI.create(uri)));
        YamlReader reader = new YamlReader(new InputStreamReader(inputStream), getYmlConfig());
        return reader.read(class1);
    }

    static String serializeModel(Object object) {
        try {
			Writer writer = new CharArrayWriter();
			YamlWriter yamlWriter = new YamlWriter(writer, getYmlConfig());
			yamlWriter.write(object);
			yamlWriter.close();
			return writer.toString();
		} catch (YamlException e) {
			throw new RuntimeException(e);
		}
    }

    private static YamlConfig getYmlConfig() {
        YamlConfig yamlConfig = new YamlConfig();
        addTag(yamlConfig, BasicORMMappingGenerator.class);
        addTag(yamlConfig, UppercaseORMMappingGenerator.class);
        addTag(yamlConfig, ConnectionExtractorFromHibernateXML.class);
        addTag(yamlConfig, ConnectionExtractorFromPersistenceXML.class);
        addTag(yamlConfig, DatabaseEntitiesExtractor.class);
        addTag(yamlConfig, DTOGenerator.class);
        addTag(yamlConfig, JPAEntityMappingWriter.class);
        addTag(yamlConfig, BinderList.class);
        addTag(yamlConfig, ModelManagement.class);
        addTag(yamlConfig, DatabaseConnectionDiscoverer.class);
        return yamlConfig;
    }

    private static void addTag(YamlConfig yamlConfig, Class<?> class1) {
        yamlConfig.setClassTag(class1.getSimpleName(), class1);
    }
}
