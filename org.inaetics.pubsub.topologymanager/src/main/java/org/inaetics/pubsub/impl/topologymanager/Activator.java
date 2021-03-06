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
package org.inaetics.pubsub.impl.topologymanager;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.inaetics.pubsub.spi.discovery.DiscoveryManager;
import org.inaetics.pubsub.spi.pubsubadmin.PubSubAdmin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.hooks.service.ListenerHook;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.LogService;

public class Activator extends DependencyActivatorBase {

  @Override
  public void init(BundleContext context, DependencyManager manager) {

    String[] objectClass = new String[] {
            ListenerHook.class.getName(), EventHandler.class.getName(), ManagedService.class.getName()
    };

    String[] topics = new String[] {org.inaetics.pubsub.spi.utils.Constants.DISCOVERY_EVENT};

    Dictionary<String, Object> properties = new Hashtable<String, Object>();
    properties.put(Constants.SERVICE_PID, PubSubTopologyManager.SERVICE_PID);
    properties.put(EventConstants.EVENT_TOPIC, topics);

    manager.add(
        manager.createComponent()
        .setInterface(objectClass, properties)
        .setImplementation(PubSubTopologyManager.class)
            .setCallbacks(null, "start", "stop", "destroy")
            .add(createServiceDependency()
                .setService(PubSubAdmin.class)
                .setRequired(false)
                .setCallbacks("adminAdded", "adminRemoved"))
            .add(createServiceDependency()
                .setService(DiscoveryManager.class)
                .setRequired(false)
                .setCallbacks("discoveryManagerAdded", "discoveryManagerRemoved"))
            .add(createServiceDependency()
                .setService(LogService.class)
                .setRequired(false))
     );

  }
  
}
