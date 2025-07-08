package com.adobe.aem.guides.wknd.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Set;

@Component(
    service = Servlet.class,
    property = {
        Constants.SERVICE_DESCRIPTION + "=TarMK Mode & Runmodes Status Servlet",
        "sling.servlet.paths=/bin/tarmk/mode/status"
    }
)
public class TarmkModeStatusServlet extends SlingAllMethodsServlet {

    @Reference
    private ConfigurationAdmin configAdmin;

    @Reference
    private SlingSettingsService slingSettingsService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject json = new JSONObject();
        try {
            // Read TarMK Cold Standby mode
            Configuration config = configAdmin.getConfiguration(
                    "org.apache.jackrabbit.oak.segment.standby.store.StandbyStoreService", null);
            Dictionary<String, Object> props = config.getProperties();

            if (props != null && props.get("mode") != null) {
                String mode = (String) props.get("mode");
                try {
                    json.put("tarmkMode", mode);
                } catch (org.json.JSONException e) {
                    json.put("error", "JSON error in tarmkMode: " + e.getMessage());
                }
            } else {
                try {
                    json.put("error", "Mode property not found in OSGi config");
                } catch (org.json.JSONException e) {
                    // unlikely
                }
            }

            // Read Sling runmodes
            Set<String> runModes = slingSettingsService.getRunModes();
            JSONArray runModesArray = new JSONArray();
            for (String runMode : runModes) {
                runModesArray.put(runMode);
            }

            try {
                json.put("runmodes", runModesArray);
            } catch (org.json.JSONException e) {
                // unlikely
            }

        } catch (Exception e) {
            try {
                json.put("error", "Exception: " + e.getMessage());
            } catch (org.json.JSONException jsonEx) {
                // unlikely
            }
        }

        response.getWriter().write(json.toString());
    }
}
