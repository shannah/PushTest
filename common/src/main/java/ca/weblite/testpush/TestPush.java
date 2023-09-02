package ca.weblite.testpush;

import com.codename1.background.BackgroundFetch;
import com.codename1.components.SpanLabel;
import com.codename1.io.Log;
import com.codename1.io.Properties;
import com.codename1.push.Push;
import com.codename1.push.PushCallback;
import com.codename1.system.Lifecycle;
import com.codename1.ui.*;
import com.codename1.ui.layouts.*;
import com.codename1.util.Callback;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This file was generated by <a href="https://www.codenameone.com/">Codename One</a> for the purpose
 * of building native mobile applications using Java.
 */
public class TestPush extends Lifecycle implements PushCallback {

    private String pushKey;

    private static String TOKEN;
    private static String GCM_SERVER_KEY;

    @Override
    public void runApp() {
        try {
            loadAppProperties();
        } catch (IOException e) {
            Log.e(e);
            showErrorForm();
            return;
        }
        Log.p("Registering push");
        Display.getInstance().registerPush();
        showLoadingForm();
    }

    private void loadAppProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(Display.getInstance().getResourceAsStream(getClass(), "/app.properties"));
        TOKEN = properties.getProperty("cn1.pushToken");
        GCM_SERVER_KEY = properties.getProperty("fcm.apiKey");
    }

    private void showPushForm() {
        Form hi = new Form("Push Test", BoxLayout.y());
        hi.add(new SpanLabel("Enter a message and press send to send a push notification to this device"));
        TextField message = new TextField();
        hi.add(message);

        Button sendButton = new Button("Send Push");
        hi.add(sendButton);

        Container responseContainer = new Container(BoxLayout.y());
        hi.add(responseContainer);
        sendButton.addActionListener(e -> sendPush(message.getText(), responseContainer));

        hi.show();
    }

    private void showLoadingForm() {
        Form loading = new Form("Loading", BoxLayout.y());
        loading.add(new SpanLabel("Please wait..."));
        loading.show();
    }

    private void showErrorForm() {
        Form error = new Form("Error", BoxLayout.y());
        error.add(new SpanLabel("An error occurred while loading the data"));
        error.show();
    }

    private void sendPush(String textMessage, Container responseContainer){
        CN.setTimeout(4000, ()-> {
            Log.p("Sending push....");
            boolean result = new Push(TOKEN, textMessage, pushKey)
                    .gcmAuth(GCM_SERVER_KEY)
                    .send();
            Log.p("Push sent");
            CN.callSerially(()->{
               responseContainer.removeAll();
               responseContainer.add(new SpanLabel("Push sent: "+result));
               responseContainer.getComponentForm().revalidateWithAnimationSafety();
            });
        });

    }

    @Override
    public void push(String s) {
        System.out.println("Push received "+s);
        CN.callSerially(()->{
            Dialog.show("Push Received", s, "OK", null);
        });
    }

    @Override
    public void registeredForPush(String deviceId) {
        Log.p("Registered for push "+deviceId);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                pushKey = Push.getPushKey();
                CN.callSerially(()->{
                    showPushForm();
                });
            }

        }, 4000l);
    }

    @Override
    public void pushRegistrationError(String error, int errorCode) {
        System.out.println("Push registration error "+error);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                CN.callSerially(()->{
                    Dialog.show("Push Error", "Msg:: "+error+" code "+errorCode, "OK", null);
                    showErrorForm();
                });
            }

        }, 4000l);
    }
}
