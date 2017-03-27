package example.node;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by chaoluo on 03/27/2017.
 */
public class Node2 {

    public static void main(String[] args) {
        Node node = new Node();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 使用控制台发送消息给Node1.
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String text = scanner.next();
            if ("exit".equals(text)) {
                break;
            }
            node.sendMsg(null,"hello " + text + ",node1!");
        }

    }
}
