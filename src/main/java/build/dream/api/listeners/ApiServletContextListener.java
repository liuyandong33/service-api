package build.dream.api.listeners;

import build.dream.api.tasks.HandleSyncDataTask;
import build.dream.common.listeners.BasicServletContextListener;
import build.dream.common.mappers.CommonMapper;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApiServletContextListener extends BasicServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
        super.previousInjectionBean(servletContextEvent.getServletContext(), CommonMapper.class);
        new HandleSyncDataTask().start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
