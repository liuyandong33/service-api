package build.dream.api.listeners;

import build.dream.api.mappers.CommonMapper;
import build.dream.common.listeners.BasicServletContextListener;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApiServletContextListener extends BasicServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
        super.previousInjectionBean(servletContextEvent.getServletContext(), CommonMapper.class);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
