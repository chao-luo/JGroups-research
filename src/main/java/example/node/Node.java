package example.node;

import lombok.extern.slf4j.Slf4j;
import org.jgroups.*;
import org.jgroups.util.Util;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by chaoluo on 03/27/2017.
 */
@Slf4j
public class Node extends ReceiverAdapter {

    private static final String CONFIG_NAME = "tcp.xml";

    private static final String CLUSTER_NAME = "CC";

    private JChannel jChannel;

    /**
     * 以此作为节点间初始化的同步数据.
     */
    private Map<String, String> cacheData = new HashMap<String, String>();

    private ReentrantLock lock = new ReentrantLock();

    public Node() {
        try {
            File configFile = new ClassPathResource(CONFIG_NAME).getFile();
            jChannel = new JChannel(configFile);
            jChannel.setReceiver(this);
            jChannel.connect(CLUSTER_NAME);
            jChannel.getState(null, 50000);
        } catch (Exception e) {
            log.error("node is ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * <pre>
     * 发送消息给目标地址.
     * </pre>
     *
     * @param dest    为空表示发给所有节点.
     * @param textMsg 消息.
     */
    public void sendMsg(Address dest, Object textMsg) {
        Message msg = new Message(dest, textMsg);
        try {
            jChannel.send(msg);
        } catch (Exception e) {
            log.error("消息发送失败!", e);
            // 应自定异常,最好是自定义Exception类型!
            throw new RuntimeException("消息发送失败!", e);
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        //cacheData过大可能会造成节点的状态同步时间过长.
        lock.lock();
        try {
            Util.objectToStream(jChannel.getState(), new DataOutputStream(output));
        } catch (Exception e) {
            throw e;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void receive(Message msg) {
        //当前节点不接收自己发送到通道当中的消息.
        if (msg.getSrc().equals(jChannel.getAddress())) {
            return;
        }
        log.info(msg.getObject());
    }

    @Override
    public void setState(InputStream input) throws Exception {
        lock.lock();
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> cacheData = (Map<String, String>) Util.objectFromStream(new DataInputStream(input));
            this.cacheData.putAll(cacheData);
        } catch (Exception e) {
            log.error("从主节点同步状态到当前节点发生异常!", e);
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void viewAccepted(View view) {
        log.info("当前成员[" + this.jChannel.getAddressAsString() + "]");
        log.info("", view.getCreator());
        log.info("", view.getMembers());
        log.info("当前节点数据:" + cacheData);
    }

    /**
     * <pre>
     * 提供一个简单的初始化数据的方法.
     * </pre>
     */
    public void addData(String key, String val) {
        if (key != null && !key.isEmpty()) {
            cacheData.put(key, val);
        }
    }
}
