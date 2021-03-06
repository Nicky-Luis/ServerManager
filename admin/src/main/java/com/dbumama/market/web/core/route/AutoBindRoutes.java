package com.dbumama.market.web.core.route;

import com.dbumama.market.web.core.annotation.ClassSearcher;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.jfinal.config.Routes;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;

import java.util.List;

public class AutoBindRoutes extends Routes {

    private boolean autoScan = true;

    private List<Class<? extends Controller>> excludeClasses = Lists.newArrayList();

    private boolean includeAllJarsInLib;

    private List<String> includeJars = Lists.newArrayList();

    protected final Log logger = Log.getLog(getClass());

    private String suffix = "Controller";

    public AutoBindRoutes autoScan(boolean autoScan) {
        this.autoScan = autoScan;
        return this;
    }

    @SuppressWarnings("unchecked")
    public AutoBindRoutes addExcludeClasses(Class<? extends Controller>... clazzes) {
        if (clazzes != null) {
            for (Class<? extends Controller> clazz : clazzes) {
                excludeClasses.add(clazz);
            }
        }
        return this;
    }

    public AutoBindRoutes addExcludeClasses(List<Class<? extends Controller>> clazzes) {
        excludeClasses.addAll(clazzes);
        return this;
    }

    public AutoBindRoutes addJars(String... jars) {
        if (jars != null) {
            for (String jar : jars) {
                includeJars.add(jar);
            }
        }
        return this;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void config() {
        setBaseViewPath("/WEB-INF/template");

        List<Class<? extends Controller>> controllerClasses
                = ClassSearcher.of(Controller.class)
                .includeAllJarsInLib(includeAllJarsInLib)
                .injars(includeJars)
                .search();

        RouteBind controllerBind = null;
        for (Class controller : controllerClasses) {
            if (excludeClasses.contains(controller)) {
                continue;
            }
            controllerBind = (RouteBind) controller.getAnnotation(RouteBind.class);
            if (controllerBind == null) {
                if (!autoScan) {
                    continue;
                }
                this.add(controllerKey(controller), controller);
                logger.debug("routes.add(" + controllerKey(controller) + ", " + controller.getName() + ")");
            } else if (StrKit.isBlank(controllerBind.viewPath())) {
                this.add(controllerBind.path(), controller);
                logger.debug("routes.add(" + controllerBind.path() + ", " + controller.getName() + ")");
            } else {
                this.add(controllerBind.path(), controller, controllerBind.viewPath());
                logger.debug("routes.add(" + controllerBind.path() + ", " + controller + ","
                        + controllerBind.viewPath() + ")");
            }
        }
    }

    private String controllerKey(Class<Controller> clazz) {
        Preconditions.checkArgument(clazz.getSimpleName().endsWith(suffix),
                clazz.getName() + " is not annotated with @ControllerBind and not end with " + suffix);
        String controllerKey = "/" + StrKit.firstCharToLowerCase(clazz.getSimpleName());
        controllerKey = controllerKey.substring(0, controllerKey.indexOf(suffix));
        return controllerKey;
    }

    public AutoBindRoutes includeAllJarsInLib(boolean includeAllJarsInLib) {
        this.includeAllJarsInLib = includeAllJarsInLib;
        return this;
    }

    public AutoBindRoutes suffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

}
