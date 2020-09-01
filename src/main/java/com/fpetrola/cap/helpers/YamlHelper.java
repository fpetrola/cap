package com.fpetrola.cap.helpers;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.fpetrola.cap.model.binders.ModelManagement;
import com.fpetrola.cap.model.binders.implementations.*;

import java.io.*;
import java.net.URI;

public class YamlHelper {

    public static <T> T deserializeModelFromURI(String uri, Class<T> class1) throws FileNotFoundException, YamlException {
        InputStream inputStream = new FileInputStream(new File(URI.create(uri)));
        YamlReader reader = new YamlReader(new InputStreamReader(inputStream), getYmlConfig());
        return reader.read(class1);
    }

    public static String serializeModel(Object object) {
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
        addTag(yamlConfig, BasicORMMappingGenerator.class, "create-entities-orm-mapping");
        addTag(yamlConfig, UppercaseORMMappingGenerator.class, "create-entities-orm-mapping-using-uppercase");
        addTag(yamlConfig, ConnectionExtractorFromHibernateXML.class, "load-database-connection-from-hibernate-xml");
        addTag(yamlConfig, ConnectionExtractorFromPersistenceXML.class, "load-database-connection-from-persistence-xml");
        addTag(yamlConfig, DatabaseEntitiesExtractor.class, "extract-entities-from-database");
        addTag(yamlConfig, DTOGenerator.class, "create-dtos");
        addTag(yamlConfig, JPAEntityMappingWriter.class,  "write-orm-mappings-to-java-class");
        addTag(yamlConfig, BinderList.class, "list");
        addTag(yamlConfig, ModelManagement.class, "developer-model");
        addTag(yamlConfig, DatabaseConnectionDiscoverer.class, "search-for-a-database");
        addTag(yamlConfig, RepositoryGenerator.class, "create-repository-for-entity");
        addTag(yamlConfig, DtoMapperGenerator.class, "create-dto-mapper-for-entity");
        return yamlConfig;
    }

    private static void addTag(YamlConfig yamlConfig, Class<?> class1) {
        yamlConfig.setClassTag(class1.getSimpleName(), class1);
    }
    private static void addTag(YamlConfig yamlConfig, Class<?> class1, String tag) {
        yamlConfig.setClassTag(tag, class1);
    }
}
