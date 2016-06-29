/*******************************************************************************
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package org.inaetics.pubsub.impl.serialization.jackson;

import java.io.IOException;
import java.util.Iterator;

import org.inaetics.pubsub.api.serialization.MultipartContainer;
import org.inaetics.pubsub.api.serialization.Serializer;
import org.osgi.service.log.LogService;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonSerializer implements Serializer {

  private ObjectMapper mapper = new ObjectMapper();
  private volatile LogService logService;
  public static final String SERIALIZER_JACKSON = "serializer.jackson";

  @Override
  public byte[] serialize(MultipartContainer obj) {
    try {
      return mapper.writeValueAsBytes(obj);

    } catch (JsonProcessingException e) {
      logService.log(LogService.LOG_ERROR, "Exception during JSON serialize", e);
    }

    return null;
  }

  @Override
  public MultipartContainer deserialize(byte[] bytes) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try {
      String json = new String(bytes);
      
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

      MultipartContainer container = new MultipartContainer();
      JsonNode jsonNode = mapper.readTree(json);
      Iterator<JsonNode> classes = jsonNode.get("classes").elements();
      Iterator<JsonNode> objects = jsonNode.get("objects").elements();

      while (objects.hasNext() && classes.hasNext()) {
        JsonNode object = (JsonNode) objects.next();
        JsonNode clazz = (JsonNode) classes.next();

        final Class<?> cls = Class.forName(clazz.asText());
        Object obj = mapper.convertValue(object, cls);
        container.addObject(obj);
      }

      return container;
    } catch (IOException e) {
      logService.log(LogService.LOG_ERROR, "Exception during JSON deserialize", e);
    } catch (ClassNotFoundException e) {
      logService.log(LogService.LOG_ERROR, "Exception during JSON deserialize", e);
    } finally {
      Thread.currentThread().setContextClassLoader(loader);
    }
    return null;
  }
}
