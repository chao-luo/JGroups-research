import lombok.extern.slf4j.Slf4j;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created by chaoluo on 03/26/2017.
 */
@Slf4j
public class JGroupTest {

    @Test
    public void testJGroup() throws Exception {
        log.info("test JGroup ...");
        File configFile = new ClassPathResource("udp.xml").getFile();
        JChannel channel=new JChannel(configFile.getAbsolutePath());
        channel.setReceiver(new ReceiverAdapter() {
            public void receive(Message msg) {
                System.out.println("received msg from " + msg.getSrc() + ": " + msg.getObject());
            }
        });
        channel.connect("MyCluster");
        channel.send(new Message(null, "hello world"));
        channel.close();
    }
}
