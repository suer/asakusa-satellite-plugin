package org.codefirst;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.User;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.Mailer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import org.kohsuke.stapler.DataBoundConstructor;

public class AsakusaSatelliteNotifier extends Notifier {
    private String appkey;
    private String baseUrl;
    private String roomNumber;
    private String message;

    @DataBoundConstructor
    public AsakusaSatelliteNotifier(String appkey, String baseUrl, String roomNumber, String message) {
        this.appkey = appkey;
        this.baseUrl = baseUrl;
        this.roomNumber = roomNumber;
        this.message = message;
    }

    /**
     * @return the appkey
     */
    public String getAppkey() {
        return appkey;
    }

    /**
     * @return the baseUrl
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @return the roomNumber
     */
    public String getRoomNumber() {
        return roomNumber;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        String message = new String(generatedMessage(build).getBytes(), "UTF-16");
        String apiUrl = (baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "api/v1/message.json");
        String postData = "room_id=" + roomNumber + "&message=" + message + "&api_key=" + appkey;

        URL url = new URL(apiUrl);
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);

        OutputStream os = connection.getOutputStream();
        OutputStreamWriter ps = new OutputStreamWriter(os, "UTF-8");
        ps.write(postData);
        ps.close();

        InputStream is = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String s;
        while ((s = reader.readLine()) != null) {
            // do nothing
        }
        reader.close();
        is.close();
        os.close();
        return true;
    }

    String generatedMessage(AbstractBuild<?, ?> build) {
        StringBuilder userBuilder = new StringBuilder();
        for (User user : build.getCulprits()) {
            userBuilder.append(user.getFullName() + " ");
        }
        String replacedMessage = message.replace("${user}", userBuilder.toString());
        replacedMessage = replacedMessage.replace("${result}", build.getResult().toString());
        replacedMessage = replacedMessage.replace("${project}", build.getProject().getName());
        replacedMessage = replacedMessage.replace("${number}", String.valueOf(build.number));
        replacedMessage = replacedMessage.replace("${url}", Mailer.descriptor().getUrl() + build.getUrl());

        try {
            return new String(replacedMessage.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return replacedMessage;
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        super.getDescriptor();
        return new DescriptorImpl(); // super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> project) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "AsakusaSatellite";
        }
    }
}
