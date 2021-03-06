/*
 * Copyright ©2018 vbill.cn.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package cn.vbill.middleware.porter.common.client;

import cn.vbill.middleware.porter.common.cluster.client.ZookeeperClient;
import cn.vbill.middleware.porter.common.cluster.config.ZookeeperConfig;
import cn.vbill.middleware.porter.common.plugin.config.PluginServiceConfig;
import cn.vbill.middleware.porter.common.config.SourceConfig;
import cn.vbill.middleware.porter.common.config.source.NameSourceConfig;
import cn.vbill.middleware.porter.common.exception.ClientException;
import cn.vbill.middleware.porter.common.exception.ClientMatchException;
import cn.vbill.middleware.porter.common.plugin.PluginServiceClient;
import cn.vbill.middleware.porter.common.task.loader.PublicClientContext;
import cn.vbill.middleware.porter.common.util.compile.JavaFileCompiler;
import cn.vbill.middleware.porter.common.warning.client.EmailClient;
import cn.vbill.middleware.porter.common.warning.config.EmailConfig;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @param <T>
 * @author: zhangkewei[zhang_kw@suixingpay.com]
 * @date: 2018年02月02日 14:06
 * @version: V1.0
 * @review: zhangkewei[zhang_kw@suixingpay.com]/2018年02月02日 14:06
 */
public abstract class AbstractClient<T extends SourceConfig> implements Client {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);

    //插件服务配置文件
    @JSONField(serialize = false, deserialize = false)
    private static final List<String> PLUGIN_SERVICE_CLIENTS =
            SpringFactoriesLoader.loadFactoryNames(PluginServiceClient.class, JavaFileCompiler.getInstance());

    private final AtomicBoolean status = new AtomicBoolean(false);
    private boolean isPublic = false;

    private volatile T config;

    public AbstractClient() {
    }

    public AbstractClient(T config) {
        this();
        this.config = config;
    }

    @Override
    public void start() throws Exception {
        if (status.compareAndSet(false, true)) {
            LOGGER.info("starting");
            doStart();
        } else {
            LOGGER.info("already start!");
        }
    }

    @Override
    public void shutdown() throws Exception {
        if (status.compareAndSet(true, false)) {
            LOGGER.info("closing");
            doShutdown();
        } else {
            LOGGER.info("already shutdown!");
        }
    }

    /**
     * doStart
     *
     * @date 2018/8/10 下午2:49
     * @param: []
     * @return: void
     */
    protected abstract void doStart() throws Exception;

    /**
     * doShutdown
     *
     * @date 2018/8/10 下午2:49
     * @param: []
     * @return: void
     */
    protected abstract void doShutdown() throws Exception;

    @Override
    public boolean isStarted() {
        return status.get() && isAlready();
    }


    @Override
    public T getConfig() {
        return config;
    }

    /**
     * 默认为准备好的。如果客户端连接初始化费时，需在客户端实现中设置
     *
     * @return
     */
    protected boolean isAlready() {
        return true;
    }

    /**
     * getClient
     *
     * @date 2018/8/10 下午2:51
     * @param: [config]
     * @return: cn.vbill.middleware.porter.common.client.Client
     */
    public static Client getClient(SourceConfig config) throws ClientException {
        if (config instanceof NameSourceConfig) {
            return PublicClientContext.INSTANCE.getSource(((NameSourceConfig) config).getSourceName());
        }
        if (config instanceof ZookeeperConfig) {
            return new ZookeeperClient((ZookeeperConfig) config);
        }
        if (config instanceof EmailConfig) {
            return new EmailClient((EmailConfig) config);
        }

        //自定义插件配置文件
        if (config instanceof PluginServiceConfig) {
            //如果仍不能匹配客户端，尝试从插件服务SPI加载
            for (String c : PLUGIN_SERVICE_CLIENTS) {
                if (c.equals(((PluginServiceConfig) config).getInstanceClientType())) {
                    try {

                        return (Client) ClassUtils.forName(c, JavaFileCompiler.getInstance()).getConstructor(config.getClass()).newInstance(config);
                    } catch (Throwable e) {
                        continue;
                    }
                }
            }
        }
        throw new ClientMatchException();
    }

    @Override
    public String getClientInfo() {
        return null != config && null != config.getProperties() ? config.getProperties().toString() : StringUtils.EMPTY;
    }

    @Override
    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}
