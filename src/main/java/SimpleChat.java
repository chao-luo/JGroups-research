import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by chaoluo on 03/26/2017.
 */
public class SimpleChat extends ReceiverAdapter {

    /**
     * helpful link: http://jgroups.org/tutorial4/index.html
     */
    JChannel channel;
    String user_name = System.getProperty("user.name", "n/a");

    public static void main(String[] args) throws Exception {
        new SimpleChat().start();
    }


    private void start() throws Exception {
        channel = new JChannel().setReceiver(this); // use the default config, udp.xml
        channel.connect("ChatCluster");
        eventLoop();
        channel.close();
    }

    private void eventLoop() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("> ");
                System.out.flush();
                String line = in.readLine().toLowerCase();
                if (line.startsWith("quit") || line.startsWith("exit"))
                    break;
                line = "[" + user_name + "] " + line;
                Message msg = new Message(null, line);
                channel.send(msg);
            } catch (Exception e) {
            }

        }
    }

    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    public void receive(Message msg) {
        System.out.println(msg.getSrc() + ": " + msg.getObject());
    }

}
