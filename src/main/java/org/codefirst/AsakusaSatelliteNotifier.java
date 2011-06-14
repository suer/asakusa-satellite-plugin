package org.codefirst;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;

import org.kohsuke.stapler.DataBoundConstructor;

public class AsakusaSatelliteNotifier extends Notifier {
    private String appkey;
    private String baseUrl;
    private String roomNumber;

    @DataBoundConstructor
    public AsakusaSatelliteNotifier(String appkey, String baseUrl, String roomNumber) {
        this.appkey = appkey;
        this.baseUrl = baseUrl;
        this.roomNumber = roomNumber;
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

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        String message = extractMessage(build);

        String apiUrl = (baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "api/v1/message.json");
        String postData = "room_id=" + roomNumber + "&message=" + message + "&api_key=" + appkey;

        System.out.println(apiUrl);
        System.out.println(postData);
        URL url = new URL(apiUrl);
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);

        OutputStream os = connection.getOutputStream();
        PrintStream ps = new PrintStream(os);
        ps.print(postData);
        ps.close();

        InputStream is = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String s;
        while ((s = reader.readLine()) != null) {
            // do nothing
        }
        reader.close();
        os.close();
        return true;
    }

    String extractMessage(AbstractBuild<?, ?> build) {
        String message = "project: " + build.getProject().getName() + "\n";
        message += "build: " + build.getNumber() + "\n";
        message += "result: " + build.getResult().toString();
        return message;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
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
